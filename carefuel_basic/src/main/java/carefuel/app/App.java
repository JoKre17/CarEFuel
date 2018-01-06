package carefuel.app;

import java.io.File;
import java.util.Date;

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

	/**
	 *
	 * Main-method, the programs entry point.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		log.info("Startup of CarEFuel_Basic at " + new Date().toString());

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
		File routeFolder = new File(System.getProperty("user.dir") + "/resource/routes/");
		int i = 1;
		for (; (i - 1) < routeFolder.listFiles().length; i++) {
			File f = routeFolder.listFiles()[i - 1];
			log.info("[" + i + "] " + f.getName());
		}
		log.info("------------------ More options ----------------------");
		log.info("[" + i + "] Predict gasoline prices");
		log.info("[" + (i + 1) + "] Exit");

		String in = "";
		try {
			// So funktioniert es auch in Eclipse konsole und sollte auch in JAR
			// funktionieren
			in = String.valueOf(System.in.read());
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Wieso target folder? was muss da drin sein? Zu wenig Information!
			log.info("!!!   Please run the .jar in the target folder   !!!");
			System.exit(-1);

		}
		int n = in.matches("\\d+") ? Integer.parseInt(in) : -1;
		if (n == -1) {
			log.info("*** Please enter a valid nummber ***");
			// TODO Rekursion ist hier nicht sehr schön. Theoretisch sollte das sogar gar
			// nicht funktionieren!
			/*
			 * 1. Durchlauf: Buchstaben eintippen
			 * 
			 * 2. Durchlauf: Zahl eintippen (< i) returnt dann routeFolder... ABER wird
			 * nicht in einer Variable gespeichert
			 * 
			 * 3. n wird in der obersten Rekursionsebene weiterhin -1 sein
			 * 
			 * 4. Array Zugriff auf -1 -1 = -2 => Exception.
			 * 
			 * 
			 */
			getOperation();
		}

		// TODO n > i Was passiert...?
		// evaluation
		if (n == i) {
			return null;
			// Exit
		} else if (n == i + 1) {
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
		File folder = new File(System.getProperty("user.dir") + "/resource/pricePrediction/");
		int i = 1;
		for (; (i - 1) < folder.listFiles().length; i++) {
			File f = folder.listFiles()[i - 1];
			log.info("[" + i + "] " + f.getName());
		}
		log.info("[" + i + "] Exit");

		String in = "";

		try {
			// TODO Hier das gleiche Spiel wie oben. Bitte abändern!
			in = System.console().readLine();
		} catch (Exception e) {
			e.printStackTrace();
			log.info("!!!   Please run the .jar in the target folder   !!!");
			System.exit(-1);

		}

		int n = in.matches("\\d+") ? Integer.parseInt(in) : -1;
		if (n == -1) {
			log.info("*** Please enter a valid nummber ***");
			// TODO Gleiches Problem wie oben. Bitte abändern! (Keine Rekursion, wenn
			// möglich!)
			getFileToPredictPrices();
		}

		// TODO n > i? Was passiert....?
		// exit
		if (n == i) {
			System.exit(-1);
		}

		return folder.listFiles()[n - 1];
	}
}
