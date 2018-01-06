package carefuel.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.model.GasStation;
import carefuel.model.GasStationPricePrediction;


/**
 * This class can be used to update the price prediction entries of the local PSQL database. For that, a new object
 * needs to be created and the start() method needs to be invoked. A new thread is started that listens on port
 * 50001 for incoming TCP connections. When the historic prices are updated by the update script that is run on the
 * server frequently, it tries to open a connection to this application, indicating that it is finished. Then,
 * the predictions of all gas stations are updated, based on the newest gas station prices.
 *
 * @author nils
 */
public class PredictionUpdater extends Thread{
	private static final Logger log = LogManager.getLogger(Main.class);
	private static final int portNumber = 50001;

	private DatabaseHandler dbHandler;
	private PricePredictor pricePredictor;

	public PredictionUpdater(DatabaseHandler dbHandler){
		this.dbHandler = dbHandler;
		pricePredictor = new PricePredictor();
	}

	@Override
	public void run() {
		// Run this thread indefinitely
		while(true){
			try {
				ServerSocket serverSocket = new ServerSocket(portNumber);

				// Wait for incoming connection
				Socket serviceSocket = serverSocket.accept();

				// Fetch all gas stations from the database and update one after another
				Set<GasStation> gasStations = dbHandler.getAllGasStations();

				int counter = 0;
				for(GasStation gasStation : gasStations){
					System.out.println(counter++);

					Set<GasStationPricePrediction> predictions = null;
					// Catch prediction errors that are caused by missing or erroneous data
					try{
						predictions = pricePredictor.predictNextMonth(gasStation);
					} catch (Exception e){
						ArrayList<ArrayList<Pair<Date, Integer>>> prices = gasStation.getGasStationPrices();
						Date lastDate = prices.get(0).get(prices.get(0).size() - 1).getLeft();
						predictions = createConstantPrediction(gasStation, lastDate);
					}
					dbHandler.insertPricePredictions(predictions);
				}

				serviceSocket.close();
				serverSocket.close();
			}
			catch (IOException e) {
				log.error(e);
			}
		}
	}

	/**
	 * This function can be used to create a new price prediction of a given gas station
	 * containing only constant prices. That may be necessary in case the prediction
	 * process is erroneous.
	 * @param gasStation
	 * @param startDate
	 * @return
	 */
	private Set<GasStationPricePrediction> createConstantPrediction(GasStation gasStation,
			Date startDate){
		Set<GasStationPricePrediction> predictions = new HashSet<>();
		int e5 = 1250;
		int diesel = 1150;
		int e10 = 1300;

		Date currentDate = startDate;
		for(int i = 0; i < 372; ++i){
			GasStationPricePrediction prediction = new GasStationPricePrediction(gasStation,
					currentDate, e5, e10, diesel);
			predictions.add(prediction);
			currentDate = new Date(currentDate.getTime() + (2 * 3600 * 1000));
		}
		return predictions;
	}
}
