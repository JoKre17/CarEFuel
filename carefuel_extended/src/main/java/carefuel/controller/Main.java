package carefuel.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import carefuel.path.PathFinder;
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

		// new Thread() {
		// @Override
		// public void run() {
		// log.info("The dump file was read on " +
		// databaseHandler.getMostRecentPriceDataDate());
		// }
		// }.start();

		PredictionUpdater p = new PredictionUpdater(databaseHandler);
		p.start();

		pathFinder = new PathFinder(databaseHandler);
		pathFinder.setup();

		tankStrategy = new TankStrategy(databaseHandler);
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
			String printString = "\n";
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
