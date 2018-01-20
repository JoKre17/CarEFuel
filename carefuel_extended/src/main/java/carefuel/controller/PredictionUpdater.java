package carefuel.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
				log.info("Starting to update price predictions");

				// Fetch all gas stations from the database and update one after another
				List<UUID> gasStationUUIDs = dbHandler.getAllGasStationIDs().stream().collect(Collectors.toList());
				log.info("Calculating import date of database with dump file");
				dbHandler.updateMostRecentPriceDataDate();
				Date mostRecentDate = dbHandler.getMostRecentPriceDataDate();
				log.info("Database seems to be imported on " + mostRecentDate);

				// multi threading
				int cores = Runtime.getRuntime().availableProcessors();
				log.info("Parallelization with " + cores + " cores");

				List<Pair<Integer, Integer>> subListIndices = new ArrayList<>(cores);
				int currentIndex = 0;
				double stepSize = ((gasStationUUIDs.size() - 1) / ((double) cores));
				for (int i = 0; i < cores; i++) {
					int nextIndex = (int) ((i + 1) * stepSize);
					nextIndex = Math.min(nextIndex, gasStationUUIDs.size() - 1);
					subListIndices.add(Pair.of(currentIndex, nextIndex));
					currentIndex = nextIndex;

				}

				predictionWorkers.clear();
				double startTimestamp = System.currentTimeMillis();
				for (Pair<Integer, Integer> indices : subListIndices) {
					PredictionWorkerThread thread = new PredictionWorkerThread(gasStationUUIDs, indices.getLeft(),
							indices.getRight(), mostRecentDate, pricePredictor, dbHandler);
					predictionWorkers.add(thread);
					thread.start();
				}

				// hold TCP connection while Threads are working
				while (isRunning()) {
					try {
						// print Progress every minute
						Thread.sleep(60000);

						double currentProgress = getProgress();
						double currentTimestamp = System.currentTimeMillis();

						// predicted runtime left
						double progressLeft = 1.0 - currentProgress;
						double progressSpeedPerMillisecond = currentProgress / ((currentTimestamp - startTimestamp));
						double progressSpeedPerMinute = progressSpeedPerMillisecond * 60000;
						double predictedTimeInMinutesLeft = progressLeft / progressSpeedPerMinute;

						int hoursLeft = (int) (predictedTimeInMinutesLeft / 60.0);
						int minLeft = (int) (((predictedTimeInMinutesLeft / 60.0) - hoursLeft) * 60);

						log.info(String.format("Prediction Progress: %.2f %%", currentProgress * 100));
						log.info(String.format("Predicted time left %dh %2dmin", hoursLeft, minLeft));

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				log.info("Prediction Progress: 100 %");
				log.info("Predictions finished.");

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
	private Date mostRecentDate;

	private PricePredictor pricePredictor;
	private DatabaseHandler dbHandler;

	private double progress = 0;

	public PredictionWorkerThread(List<UUID> gasStationUUIDs, Integer startIndex, Integer endIndex, Date mostRecentDate,
			PricePredictor pricePredictor, DatabaseHandler dbHandler) {
		this.gasStationUUIDs = gasStationUUIDs;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.mostRecentDate = mostRecentDate;

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
			// update progress of thread for global progress printing
			progress = (double) (counter - startIndex) / (endIndex - startIndex);

			UUID gasStationUUID = gasStationUUIDs.get(counter);
			GasStation gasStation = dbHandler.getGasStation(gasStationUUID);
			Map<Fuel, List<Pair<Date, Integer>>> datePriceList = dbHandler.getGasStationPrices(gasStationUUID);

			Set<GasStationPricePrediction> firstRunPredictions = null;

			// 30 days
			double timeDiff = 30 * 1000 * 60 * 60 * 24;
			int dayDiff = 30;
			try {
				// timeDiff in milliseconds
				for(Fuel f : Fuel.values()) {
					if(datePriceList.get(f).size() > 0) {
						double timeDiffForFuelType = mostRecentDate.getTime() - datePriceList.get(f)
								.get(datePriceList.get(f).size() - 1).getLeft().getTime();
						if(timeDiffForFuelType < timeDiff) {
							timeDiff = timeDiffForFuelType;
						}
					}
				}

				// to seconds, to minutes, to hours, to days
				dayDiff = (int) (timeDiff / (1000 * 60 * 60 * 24));

				// > 30 Days...
				if (dayDiff > 30) {
					log.debug(gasStation.getId() + ": Skipping due to no prices in past 30 days.");
					continue;
				} else {
					if (dayDiff != 0) {
//						log.debug(gasStation.getId() + ": Need to predict " + (dayDiff + 30) + " days into future.");
					}
				}
				firstRunPredictions = pricePredictor.predictNextMonth(gasStation, datePriceList);

				// Catch prediction errors that are caused by missing data
			} catch (Exception e) {
				log.debug(e);

				// Catches if there is none or too less data for the gasStation
				log.warn("Little or none historical price data for gas station with id "
						+ gasStation.getId().toString());

				firstRunPredictions = createConstantPrediction(gasStation, mostRecentDate);
			}

			// calculate until 30 days into future
			if (dayDiff > 0) {
				// add predicted prices to historical data until the date, where the db was
				// updated
//				log.debug(gasStation.getId() + ": T+30 days done. Predict next " + dayDiff + " days");
				List<Pair<Date, Integer>> dieselData = datePriceList.get(Fuel.DIESEL);
				List<Pair<Date, Integer>> e10Data = datePriceList.get(Fuel.E10);
				List<Pair<Date, Integer>> e5Data = datePriceList.get(Fuel.E5);

				// iterate over sorted...
				firstRunPredictions.stream().sorted((a, b) -> a.getDate().compareTo(b.getDate())).forEach(pred -> {

					/*
					 * if last date of historic price data was until e.g. 10 days before
					 * mostRecentDate (last import date from dump file), then we also want to
					 * predict until 30 days into future FROM mostRecentDate.
					 * 
					 * So (mostRecentDate + 30 days) is the aim.
					 * 
					 * Therefore we have to add the predicted data of the next 10 days to the
					 * history data to predict the 30 days, after these 10 days.
					 */
					if (pred.getDate().getTime() < mostRecentDate.getTime()) {
						dieselData.add(Pair.of(pred.getDate(), pred.getDiesel()));
						e10Data.add(Pair.of(pred.getDate(), pred.getE10()));
						e5Data.add(Pair.of(pred.getDate(), pred.getE5()));
					}
				});

				Set<GasStationPricePrediction> secondRunPredictions = null;
				try {
					secondRunPredictions = pricePredictor.predictNextMonth(gasStation, datePriceList);
				} catch (Exception e) {
					log.debug(e);

					// Catches if there is none or too less data for the gasStation
					log.warn("Little or none historical price data for gas station with id "
							+ gasStation.getId().toString());

					secondRunPredictions = createConstantPrediction(gasStation, mostRecentDate);
				}

				// combine both predictions
				firstRunPredictions.addAll(secondRunPredictions);
			}

			// TODO dbHandler.deletePricePredictions(gasStation.getId())
			dbHandler.insertPricePredictions(firstRunPredictions);
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
