package carefuel.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.format.datetime.DateFormatter;

import carefuel.model.GasStation;
import carefuel.path.PathFinder;
import carefuel.path.Vertex;
import carefuel.tank.Node;
import carefuel.tank.TankStrategy;

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
	public static TankStrategy tankStrategy;
	public static DatabaseHandler databaseHandler;

	public static void main(String[] args) {
		log.info("Startup of CarEFuel_Extended at " + new Date().toString());
		configureOutput();

		if (args.length >= 2) {
			if (args[0].equals("-gasPricesDir")) {
				System.setProperty("gasPricesDir", args[1]);
			}
		}

		SpringApplication.run(Main.class, args);

		databaseHandler = new DatabaseHandler();
		databaseHandler.setup();

		tankStrategy = new TankStrategy(databaseHandler);

		databaseHandler.getPricePredictionClosestToDate(databaseHandler.getAllGasStations().iterator().next().getId(),
				Fuel.DIESEL, new Date());

		// new Thread() {
		// @Override
		// public void run() {
		// log.info("The dump file was read on " +
		// databaseHandler.getMostRecentPriceDataDate());
		// }
		// }.start();

		// ### TEST JONAS ALGORITHMUS ###
		// Route: Josef-Heimat nach Nürnberg

		// GasStation s1 = new GasStation();
		// s1.setLatitude(52.706567);
		// s1.setLongitude(7.284129);
		// s1.setName("Meppen");
		// GasStation s2 = new GasStation();
		// s2.setLatitude(52.797222);
		// s2.setLongitude(7.865279);
		// s2.setName("Lastrup");
		// GasStation s3 = new GasStation();
		// s3.setLatitude(53.169574);
		// s3.setLongitude(8.262815);
		// s3.setName("Oldenburg");
		// GasStation s4 = new GasStation();
		// s4.setLatitude(53.595844);
		// s4.setLongitude(8.567314);
		// s4.setName("Bremerhaven");

		Vertex<GasStation> g1 = new Vertex<>(databaseHandler.getGasStation("dadfc9a7-3715-453c-af05-d1dc6354843e"));
		Vertex<GasStation> g2 = new Vertex<>(databaseHandler.getGasStation("308733a3-f3a6-4259-a10a-8a8e08efa94e"));
		Vertex<GasStation> g3 = new Vertex<>(databaseHandler.getGasStation("2cb609f7-cfaa-4d5e-92ad-77b593091dab"));
		Vertex<GasStation> g4 = new Vertex<>(databaseHandler.getGasStation("f8646cfb-fb24-479b-aa96-a5a29b76000b"));
		List<Vertex<GasStation>> route = new ArrayList<>();

		// Vertex<GasStation> g1 = new Vertex<>(s1);
		// Vertex<GasStation> g2 = new Vertex<>(s2);
		// Vertex<GasStation> g3 = new Vertex<>(s3);
		// Vertex<GasStation> g4 = new Vertex<>(s4);
		// List<Vertex<GasStation>> route = new ArrayList<>();

		route.add(g1);
		route.add(g2);
		route.add(g3);
		route.add(g4);

		DateFormatter df = new DateFormatter("dd.MM.yyyy HH:mm");
		Date startDate = null;
		try {
			startDate = df.parse("20.01.2018 19:01", Locale.GERMAN);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		List<Node> nodeRoute = Main.tankStrategy.computeTankStrategy(route, startDate, 7.0, 5, 15, 214.8, 100,
				Fuel.DIESEL);
		// ### TEST ENDE ###

		// PredictionUpdater p = new PredictionUpdater(databaseHandler);
		// p.start();
		//
		// pathFinder = new PathFinder(databaseHandler);
		// pathFinder.setup();
	}

	private static void configureOutput() {
		OutputStream errLogStream = new StandardOutputStream(Level.WARN);
		OutputStream outLogStream = new StandardOutputStream(Level.INFO);

		PrintStream errorPs = new PrintStream(errLogStream);
		PrintStream outPs = new PrintStream(outLogStream);

		System.setErr(errorPs);
		System.setOut(outPs);
	}
}

class StandardOutputStream extends OutputStream {

	// default Level is INFO
	private Level level = Level.INFO;
	private String buffer = "";
	private final int breakSymbol = '\n';

	public StandardOutputStream(Level level) {
		this.level = level;
	}

	@Override
	public final void write(int b) throws IOException {
		// the correct way of doing this would be using a buffer
		// to store characters until a newline is encountered,
		// this implementation is for illustration only
		if (b == breakSymbol) {
			String printString = "";
			if (buffer.length() > 0) {
				printString = buffer.substring(0, buffer.length() - 1);
			}
			LogManager.getLogger(StandardOutputStream.class).log(level, printString);
			buffer = "";
		} else {
			buffer += (char) b;
		}
	}
}
