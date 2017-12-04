package carefuel.app;

import java.io.File;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.PathFinder;
import carefuel.util.Evaluator;
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

		// File file = new File(System.getProperty("user.dir") +
		// "/resource/routes/Bertha Benz Memorial Route.csv");

		while (true) {
			File file = getOperation();

			if (file == null) {
				// evaluate gasoline prices
			} else {
				// calculate beste filling strategy
				Parser parser = new Parser(file);
				parser.parse();

				PathFinder pf = new PathFinder(parser.getGasStations(), parser.getCapacity(), file.getName());
				pf.computeBestPath();

				Evaluator ev = new Evaluator(parser.getGasStations());
				ev.evaluate();
			}
		}
	}

	private static File getOperation() {
		System.out.println("Select the route: ");
		File routeFolder = new File(System.getProperty("user.dir") + "/resource/routes/");
		int i = 0;
		for (; i < routeFolder.listFiles().length; i++) {
			File f = routeFolder.listFiles()[i];
			System.out.println("[" + i + "] " + f.getName());
		}
		System.out.println("[" + i + "] Evaluate gasoline prices");

		String in = "";
		try {
			in = System.console().readLine();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("!!!   Please run the .jar in the target folder   !!!");
			System.exit(-1);

		}

		int n = in.matches("\\d+") ? Integer.parseInt(in) : -1;
		if (n == -1) {
			System.out.println("*** Please enter a valid nummber ***");
			getOperation();
		}

		// evaluation
		if (n > i) {
			return null;
		}

		return routeFolder.listFiles()[n];
	}

	/*
	 * private static void testLog() { // notice that log.debug also gets
	 * printed even though the root logger // is set to level info // this is
	 * due to the application logger configuration see @log4j2.xml
	 * log.info("Hello World!"); log.debug("Hello World!");
	 * log.error("Hello World!"); log.fatal("Hello World!"); }
	 */
}
