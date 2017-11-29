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

	private static PathFinder pathFinder;

	public static void main(String[] args) {
		log.info("Startup of CarEFuel_Extended at " + new Date().toString());
		SpringApplication.run(Main.class, args);

		DatabaseHandler databaseHandler = new DatabaseHandler();
		databaseHandler.setup();

		List<GasStation> allStations = databaseHandler.getAllGasStations().stream().collect(Collectors.toList());
		log.info(allStations.stream().findFirst().get().toJSON().toString());

		int randomStart = (int) (Math.random() * (allStations.size() - 1));
		int randomEnd = (int) (Math.random() * (allStations.size() - 1));

		GasStation start = allStations.get(randomStart);
		GasStation end = allStations.get(randomEnd);
		double range = (3.0 / 5.6) * 100;

		pathFinder = new PathFinder(databaseHandler);
		log.info(pathFinder.explorativeAStar(start, end, range));

		// databaseHandler.test();
		// log.info(databaseHandler.getAllGasStations().stream().findFirst().get().toJSON().toString());

		// Sollte ggf raus, da die Spring Applikation ja noch läuft und die DB benötigt
		// wird
		databaseHandler.exit();
	}
}
