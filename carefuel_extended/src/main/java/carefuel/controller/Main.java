package carefuel.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import carefuel.model.GasStation;
import carefuel.path.PathFinder;
import carefuel.path.Vertex;

/**
 *
 * Main is the entry class of the application. It starts the webserver (with
 * automatically embedded tomcat servlet container, which means this code can be
 * run as a standalone application) and searches for classes with the Controller
 * annotation in this package and should therefore use the RequestController
 * class for handling all requests.
 *
 */
@SpringBootApplication
public class Main {

	static {
		System.setProperty("file.encoding", "UTF-8");
	}

	private static final Logger log = LogManager.getLogger(Main.class);

	public static PathFinder pathFinder;

	public static void main(String[] args) {
		log.info("Startup of CarEFuel_Extended at " + new Date().toString());
		SpringApplication.run(Main.class, args);

		DatabaseHandler databaseHandler = new DatabaseHandler();
		databaseHandler.setup();
		
		PredictionUpdater p = new PredictionUpdater(databaseHandler);
		p.start();
	}

	/**
	 * Test EA* Algorithm
	 * 
	 * @param databaseHandler
	 */
	private static void testPathFinder(DatabaseHandler databaseHandler, PricePredictor pricePredictor) {

		List<GasStation> allStations = databaseHandler.getAllGasStations().stream().collect(Collectors.toList());
		int randomStart = (int) (Math.random() * (allStations.size() - 1));
		int randomEnd = (int) (Math.random() * (allStations.size() - 1));

		// define random start and end
		GasStation start = allStations.get(randomStart);
		GasStation end = allStations.get(randomEnd);
		log.info("Start: " + allStations.indexOf(start) + " : " + start.getId());
		log.info("End  : " + allStations.indexOf(end) + " : " + end.getId());

		// 3 [l] / 5.6 [l/100km] * 100.0 = x [km]
		short range = (short) ((3.0 / 5.6) * 100.0);
		// km/h
		short averageSpeed = 100;

		log.info("Assumed max U:" + range);
		log.info("Heuristical distance: " + GasStation.computeDistanceToGasStation(start.getLatitude(),
				start.getLongitude(), end.getLatitude(), end.getLongitude()));
		pathFinder = new PathFinder(databaseHandler, pricePredictor);
		double startTime = System.currentTimeMillis();
		List<Vertex<GasStation>> path = pathFinder.explorativeAStar(start.getId().toString(), end.getId().toString(), new Date(), range, averageSpeed, 0);
		log.info((System.currentTimeMillis() - startTime) / 1000.0);
		for (Vertex<GasStation> v : path) {
			log.info(v.getValue().getId() + ": GCost = " + v.getGCost() + " HCost = " + v.getHCost());
		}

		double estimatedDistance = GasStation.computeDistanceToGasStation(start.getLatitude(), start.getLongitude(),
				end.getLatitude(), end.getLongitude());

		if (path.size() != 0) {
			log.info(estimatedDistance + " should be less than " + path.get(path.size() - 1).getGCost());
		} else {
			log.info("No path could be calculated.");
		}

		// log.info(pathFinder.explorativeAStar(start, end, range, 1));
	}
}
