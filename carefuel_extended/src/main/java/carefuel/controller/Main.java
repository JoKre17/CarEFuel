package carefuel.controller;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import carefuel.path.PathFinder;

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

		if (args.length >= 2) {
			if (args[0].equals("-gasPricesDir")) {
				System.setProperty("gasPricesDir", args[1]);
			}
		}

		SpringApplication.run(Main.class, args);

		DatabaseHandler databaseHandler = new DatabaseHandler();
		databaseHandler.setup();

		PredictionUpdater p = new PredictionUpdater(databaseHandler);
		p.start();

		pathFinder = new PathFinder(databaseHandler);
	}
}
