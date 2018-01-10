package carefuel.app;

import java.io.File;
import java.util.Date;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.PathFinder;
import carefuel.util.Evaluator;
import carefuel.util.Parser;

// TODO Überlegen, ob die "Tests" Packages entfernt werden sollten

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

	private static String routesDirectory;

	private static String pricePredictionDirectory;

	private static Scanner sc = new Scanner(System.in);

	/**
	 *
	 * Main-method, the programs entry point.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		log.info("Startup of CarEFuel_Basic at " + new Date().toString());

		// Allow setting special paths for in and output folders

		// System.setProperty("gasPricesDir", path);
		// System.setProperty("gasStationsDir", path);
		// System.setProperty("routesDir");
		// System.setProperty("pricesOutDir", path);
		// System.setProperty("routesOutDir");
		// System.setProperty("pricePredictionDir", path);

		// Set directory for the pricePrediction input
		pricePredictionDirectory = System.getProperty("user.dir") + "/resource/pricePrediction/";
		if (System.getProperty("pricePredictionDir") != null) {
			pricePredictionDirectory = System.getProperty("pricePredictionDir");
		}

		// Set directory for the routes input
		routesDirectory = System.getProperty("user.dir") + "/resource/routes/";
		if (System.getProperty("routesDir") != null) {
			pricePredictionDirectory = System.getProperty("routesDir");
		}

		while (true) {
			File file = getOperation();

			if (file == null) {
				// Predict gasoline prices
				Parser parser = new Parser(null);
				parser.parseGasStationsToPredict(getFileToPredictPrices());
			} else {
				// calculate beste filling strategy
				Parser parser = new Parser(file);
				parser.parseRoute();

				PathFinder pf = new PathFinder(parser.getGasStations(), parser.getCapacity(), file.getName());
				pf.computeBestPath();

				Evaluator ev = new Evaluator(parser.getGasStations());
				ev.evaluate();
			}
		}
	}

	/**
	 * Function that allows the user to choose a file via console interaction and
	 * returns the choosen route file or returns null if the user selected to
	 * predict gasoline prices.
	 *
	 * @return file to parse
	 */
	private static File getOperation() {
		log.info("\n\n************ WELCOME TO CAREFUEL *******************");
		log.info("\nSelect the route: ");
		File routeFolder = new File(routesDirectory);
		int i = 1;
		for (; (i - 1) < routeFolder.listFiles().length; i++) {
			File f = routeFolder.listFiles()[i - 1];
			log.info("[" + i + "] " + f.getName());
		}
		log.info("----------------- More options ---------------------");
		log.info("[" + i + "] Predict gasoline prices");
		log.info("[" + (i + 1) + "] Exit");

		String in = "";
		try {
			in = String.valueOf(sc.nextLine());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("!!!   Please run the .jar in the carefuel_basic/target folder   !!!");
			log.info("Progam exited");
			System.exit(-1);
		}
		int n = in.matches("\\d+") ? Integer.parseInt(in) : -1;
		if (n < 1 || n > (i + 1)) {
			log.info("*** Please enter a number in the valid range ***");
			getOperation();
		}
		if (n == -1) {
			log.info("*** Please enter a valid nummber ***");
			getOperation();
		}

		// evaluation
		if (n == i) {
			return null;
			// Exit
		} else if (n == i + 1) {
			log.info("Progam exited");
			System.exit(-1);
		}

		return routeFolder.listFiles()[n - 1];
	}

	/**
	 * Function that allows the user to choose a file via console interaction and
	 * returns the choosen gasoline price file.
	 *
	 * @return file to parse
	 */
	private static File getFileToPredictPrices() {
		log.info("\n\nSelect the file to predict prices for: ");
		File folder = new File(pricePredictionDirectory);
		int i = 1;
		for (; (i - 1) < folder.listFiles().length; i++) {
			File f = folder.listFiles()[i - 1];
			log.info("[" + i + "] " + f.getName());
		}
		log.info("[" + i + "] Exit");

		String in = "";

		try {
			in = String.valueOf(sc.nextLine());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("!!!   Please run the .jar in the carefuel_basic/target folder   !!!");
			log.info("Progam exited");
			System.exit(-1);
		}

		int n = in.matches("\\d+") ? Integer.parseInt(in) : -1;
		if (n < 1 || n > i) {
			log.info("*** Please enter a number in the valid range ***");
			getFileToPredictPrices();
		}
		if (n == -1) {
			log.info("*** Please enter a valid nummber ***");
			getFileToPredictPrices();
		}

		// exit
		if (n == i) {
			log.info("Progam exited");
			System.exit(-1);

		}

		return folder.listFiles()[n - 1];
	}
}
