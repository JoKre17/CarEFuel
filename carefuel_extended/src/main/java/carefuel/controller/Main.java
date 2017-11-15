package carefuel.controller;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

<<<<<<< HEAD
import carefuel.model.GasStation;
import carefuel.util.InputParser;

/**
 * 
=======
/**
 *
>>>>>>> 71fd5f6bfb2796d1afb20d921bd6915ae63393ba
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

<<<<<<< HEAD
		InputParser inputParser = new InputParser();
		DatabaseHandler databaseHandler = new DatabaseHandler();

		List<GasStation> gasStationslist = inputParser
				.getAllGasStationsFromCSV(System.getProperty("user.dir") + "/../input_files/gasstations.csv");
		// databaseHandler.setup();
		// databaseHandler.createGasStations(gasStationslist);
		// databaseHandler.exit();
=======
		DatabaseHandler databaseHandler = new DatabaseHandler();
		databaseHandler.setup();

		long timeBefore = System.currentTimeMillis();
		log.info(databaseHandler.getPricePrediction("001975cc-d534-4819-ab35-8e88848c3096", Fuel.DIESEL).toString());
		log.info((System.currentTimeMillis() - timeBefore) + "ms");
		databaseHandler.exit();
>>>>>>> 71fd5f6bfb2796d1afb20d921bd6915ae63393ba
	}
}
