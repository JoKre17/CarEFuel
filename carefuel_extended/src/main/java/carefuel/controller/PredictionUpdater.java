package carefuel.controller;

import carefuel.model.GasStation;
import carefuel.model.GasStationPrice;
import carefuel.model.GasStationPricePrediction;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;


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
        this.pricePredictor = new PricePredictor();
    }

    @Override
    public void run() {
        // Run this thread indefinitely
        while(true){
            try {
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Socket serviceSocket = serverSocket.accept();

                // Fetch all gas stations from the database and update one after another
                Set<GasStation> gasStations = dbHandler.getAllGasStations();
                for(GasStation gasStation : gasStations){
                    Set<GasStationPricePrediction> predictions = pricePredictor.predictNextMonth(gasStation);
                    dbHandler.insertPricePredictions(predictions);
                    break;
                }

                serviceSocket.close();
                serverSocket.close();
            }
            catch (IOException e) {
                log.error(e);
            }
        }
    }
}
