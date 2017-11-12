package carefuel.controller;

import java.util.Date;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import carefuel.model.GasStation;
import carefuel.model.GasStationPrices;

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

	private static final Logger log = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		log.info("Startup of CarEFuel_Extended at " + new Date().toString());
		SpringApplication.run(Main.class, args);

		DatabaseHandler databaseHandler = new DatabaseHandler();
		databaseHandler.setup();

		long timeBefore = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			GasStation gasStation = databaseHandler.getGasStation("00060632-708f-4444-8888-acdc00000001");
			Set<GasStationPrices> prices = gasStation.getGasStationPrices();
		}
		log.info((System.currentTimeMillis() - timeBefore) + "ms");
		databaseHandler.exit();
	}
}
