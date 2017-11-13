package carefuel.app;

import java.io.File;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.util.Parser;

/**
 * Hello world!
 *
 */
public class App {

	private static final Logger log = LogManager.getLogger(App.class);

	public static void main(String[] args) {
		log.info("Startup of CarEFuel_Basic at " + new Date().toString());

		testLog();
        
        File file = new File(System.getProperty("user.dir") + "/resource/Bertha Benz Memorial Route.csv");
        
        Parser parser = new Parser(file);
        parser.parse();
        
        //parser.getLonLat(24983);
	}

	private static void testLog() {
		// notice that log.debug also gets printed even though the root logger is set to level info
		// this is due to the application logger configuration see @log4j2.xml
		log.info("Hello World!");
		log.debug("Hello World!");
		log.error("Hello World!");
		log.fatal("Hello World!");
	}

}
