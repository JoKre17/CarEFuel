package carefuel.app;

import java.io.File;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.PathFinder;
import carefuel.util.Parser;

/**
 *
 * App class containing the main-method. This class starts the parsing of the
 * given route and runs the path-finding algorithm on the parsed data.
 *
 * @author jwall
 *
 */
public class App {

	private static final Logger log = LogManager.getLogger(App.class);

	/**
	 *
	 * Main-method, the programs entry point.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		log.info("Startup of CarEFuel_Basic at " + new Date().toString());

		if (args.length >= 2) {
			if (args[0].equals("-gasPricesDir")) {
				System.setProperty("gasPricesDir", args[1]);
			}
		}

		// Assume that the user runs the tool from the directory the resource dir is in
		File file = new File(System.getProperty("user.dir") + "/resource/Bertha Benz Memorial Route.csv");

		Parser parser = new Parser(file);
		parser.parse();

		PathFinder pf = new PathFinder(parser.getGasStations(), parser.getCapacity());
		pf.computeBestPath();
	}

}
