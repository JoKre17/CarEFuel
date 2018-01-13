package carefuel.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.model.GasStation;
import carefuel.model.GasStationPricePrediction;

/**
 * This class can be used to update the price prediction entries of the local
 * PSQL database. For that, a new object needs to be created and the start()
 * method needs to be invoked. A new thread is started that listens on port
 * 50001 for incoming TCP connections. When the historic prices are updated by
 * the update script that is run on the server frequently, it tries to open a
 * connection to this application, indicating that it is finished. Then, the
 * predictions of all gas stations are updated, based on the newest gas station
 * prices.
 *
 * @author nils
 */
public class PredictionUpdater extends Thread {
	private static final Logger log = LogManager.getLogger(PredictionUpdater.class);
	private static final int portNumber = 50001;

	private DatabaseHandler dbHandler;
	private PricePredictor pricePredictor;

	private List<PredictionWorkerThread> predictionWorkers = new ArrayList<>();

	public PredictionUpdater(DatabaseHandler dbHandler) {
		this.dbHandler = dbHandler;
		pricePredictor = new PricePredictor();
	}

	@Override
	public void run() {
		// Run this thread indefinitely
		while (true) {
			try {
				ServerSocket serverSocket = new ServerSocket(portNumber);

				// Wait for incoming connection
				Socket serviceSocket = serverSocket.accept();

				// Fetch all gas stations from the database and update one after another
				List<UUID> gasStationUUIDs = dbHandler.getAllGasStationIDs().stream().collect(Collectors.toList());

				// multi threading
				int cores = Runtime.getRuntime().availableProcessors();
				log.info("Parallelization with " + cores + " cores");

				List<Pair<Integer, Integer>> subListIndices = new ArrayList<>(cores);
				int currentIndex = 0;
				double stepSize = ((gasStationUUIDs.size() - 1) / ((double) cores));
				log.info("Stepsize: " + stepSize);
				for (int i = 0; i < cores; i++) {
					int nextIndex = (int) ((i + 1) * stepSize);
					nextIndex = Math.min(nextIndex, gasStationUUIDs.size() - 1);
					subListIndices.add(Pair.of(currentIndex, nextIndex));
					currentIndex = nextIndex;

				}
				log.info("Split at " + 0);
				for (Pair<Integer, Integer> pair : subListIndices) {
					log.info(pair.getLeft() + " to " + pair.getRight());
				}

				predictionWorkers.clear();
				double lastTimestamp = System.currentTimeMillis() / 1000.0;
				for (Pair<Integer, Integer> indices : subListIndices) {
					PredictionWorkerThread thread = new PredictionWorkerThread(gasStationUUIDs, indices.getLeft(),
							indices.getRight(), pricePredictor, dbHandler);
					predictionWorkers.add(thread);
					thread.start();
				}

				// hold TCP connection while Threads are working
				while (isRunning()) {
					double lastProgress = 0;
					try {
						// print Progress every minute

						double currentProgress = getProgress();
						double currentTimestamp = System.currentTimeMillis() / 1000.0;
						log.info(String.format("Prediction Progress: %.2f %%", currentProgress * 100));

						// predicted runtime left
						double progressLeft = 1 - currentProgress;
						double progressSpeedPerSecond = (currentProgress - lastProgress)
								/ (currentTimestamp - lastTimestamp);

						double predictedTimeInMinutesLeft = progressLeft / (progressSpeedPerSecond * 60000);
						int hoursLeft = (int) (predictedTimeInMinutesLeft / 60.0);
						int minLeft = (int) ((predictedTimeInMinutesLeft / 60.0) - hoursLeft);
						log.info(String.format("Predicted time left %d:%d", hoursLeft, minLeft));

						lastProgress = currentProgress;
						lastTimestamp = currentTimestamp;
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				serviceSocket.close();
				serverSocket.close();
			} catch (IOException e) {
				log.error(e);
			}
		}
	}

	/**
	 * Returns true, if the PredictionUpdater is actual updating the Predictions
	 * 
	 * @return
	 */
	public boolean isRunning() {
		for (Thread t : predictionWorkers) {
			if (t.isAlive()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns 1 if the last update is finished and between 0 and 1, if there is an
	 * Update in Progress
	 * 
	 * @return
	 */
	public double getProgress() {
		double progressSum = 0;

		for (PredictionWorkerThread t : predictionWorkers) {
			if (t.isAlive()) {
				progressSum += t.getProgress();
			} else {
				// assume, the thread is finished
				progressSum += 1;
			}
		}

		if (predictionWorkers.size() != 0) {
			return progressSum / predictionWorkers.size();
		} else {
			return 1;
		}
	}
}

class PredictionWorkerThread extends Thread {

	private Logger log = LogManager.getLogger(PredictionWorkerThread.class);

	private List<UUID> gasStationUUIDs;
	private Integer startIndex = 0;
	private Integer endIndex = 0;

	private PricePredictor pricePredictor;
	private DatabaseHandler dbHandler;

	private double progress = 0;

	public PredictionWorkerThread(List<UUID> gasStationUUIDs, Integer startIndex, Integer endIndex,
			PricePredictor pricePredictor, DatabaseHandler dbHandler) {
		this.gasStationUUIDs = gasStationUUIDs;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.pricePredictor = pricePredictor;
		this.dbHandler = dbHandler;

		this.setDaemon(true);
	}

	public double getProgress() {
		return this.progress;
	}

	@Override
	public void run() {

		for (int counter = startIndex; counter < endIndex; counter++) {
			UUID gasStationUUID = gasStationUUIDs.get(counter);
			GasStation gasStation = dbHandler.getGasStation(gasStationUUID);
			List<List<Pair<Date, Integer>>> datePriceList = dbHandler.getGasStationPrices(gasStationUUID);

			Set<GasStationPricePrediction> predictions = null;
			// Catch prediction errors that are caused by missing or erroneous data
			try {
				predictions = pricePredictor.predictNextMonth(gasStation, datePriceList);
			} catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
				// TODO Clarify, why there are gasStations without prices in database

				// Catches if there is none or too less data for the gasStation
				log.warn("Little or none historical price data for gas station with id "
						+ gasStation.getId().toString());
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				
				//
				c.set(2017, 11, 26, 0, 0, 0);
				Date today = c.getTime();
				predictions = createConstantPrediction(gasStation, today);
			}
			dbHandler.insertPricePredictions(predictions);

			progress = (double) (counter - startIndex) / (endIndex - startIndex);
		}
	}

	/**
	 * This function can be used to create a new price prediction of a given gas
	 * station containing only constant prices. That may be necessary in case the
	 * prediction process is erroneous.
	 * 
	 * @param gasStation
	 * @param startDate
	 * @return
	 */
	protected Set<GasStationPricePrediction> createConstantPrediction(GasStation gasStation, Date startDate) {
		Set<GasStationPricePrediction> predictions = new HashSet<>();
		int e5 = 1250;
		int diesel = 1150;
		int e10 = 1300;

		Date currentDate = startDate;
		for (int i = 0; i < 372; ++i) {
			GasStationPricePrediction prediction = new GasStationPricePrediction(gasStation, currentDate, e5, e10,
					diesel);
			predictions.add(prediction);
			currentDate = new Date(currentDate.getTime() + (2 * 3600 * 1000));
		}
		return predictions;
	}

}
