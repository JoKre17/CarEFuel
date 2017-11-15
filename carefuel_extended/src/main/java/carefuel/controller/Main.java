package carefuel.controller;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import carefuel.model.GasStation;
import carefuel.util.InputParser;

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
@ComponentScan(basePackages = { "carefuel.controller" })
// @ComponentScan("carefuel.configuration")
// @ControllerAdvice(basePackages = { "carefuel.frontend.controller" })
public class Main {

	private static final Logger log = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		log.info("Startup of CarEFuel_Extended at " + new Date().toString());

		SpringApplication.run(Main.class, args);

		InputParser inputParser = new InputParser();
		DatabaseHandler databaseHandler = new DatabaseHandler();

		List<GasStation> gasStationslist = inputParser
				.getAllGasStationsFromCSV(System.getProperty("user.dir") + "/../input_files/gasstations.csv");
		// databaseHandler.setup();
		// databaseHandler.createGasStations(gasStationslist);
		// databaseHandler.exit();
	}
}
