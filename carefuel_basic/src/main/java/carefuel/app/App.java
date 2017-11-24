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

		// testLog();

		File file = new File(System.getProperty("user.dir") + "/resource/Bertha Benz Memorial Route.csv");

		Parser parser = new Parser(file);
		parser.parse();

		PathFinder pf = new PathFinder(parser.getGasStations(), parser.getCapacity());
		pf.computeBestPath();
	}

	/*
	 * private static void testLog() { // notice that log.debug also gets
	 * printed even though the root logger // is set to level info // this is
	 * due to the application logger configuration see @log4j2.xml
	 * log.info("Hello World!"); log.debug("Hello World!");
	 * log.error("Hello World!"); log.fatal("Hello World!"); }
	 */
}
