package carefuel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.app.App;
import carefuel.model.GasStation;

/**
 * Class to evaluate the predicted gasoline prices.
 *
 *
 * @author jwall
 *
 */
public class Evaluator {

	private static final Logger log = LogManager.getLogger(App.class);
	
	private List<GasStation> gasStations;

	/**
	 * Constructor of the Evaluator
	 */
	public Evaluator(List<GasStation> gasStations) {
		this.gasStations = gasStations;
	}

	/**
	 * Method that starts the evaluation of the gasoline prices and outputs it
	 * to the console. Evaluation means the the mean difference between the
	 * predicted and the actual gasoline price.
	 */
	public void evaluate() {
		boolean outputEnabled = false;
		if (true) {
			log.info("\n----------------- Evaluation -------------------");
		}
		double sum = 0;

		if (outputEnabled) {
			log.info("Gas Station Analysis: ");
		}
		for (GasStation g : gasStations) {

			double diff = Math.abs(g.getPredictedPrice() - getRealPriceOfGasStation(g.getID(), g.getArrivalDate()));
			if (outputEnabled) {
				log.info("Predicted: " + g.getPredictedPrice());
				log.info("Actual: " + getRealPriceOfGasStation(g.getID(), g.getArrivalDate()));
				log.info("Difference: " + diff + "\n");
			}
			sum += diff;
		}

		double meanDiff = sum / gasStations.size();

		System.out.println("\nMean difference: " + meanDiff / 10);
	}

	/**
	 * Method that finds the actual gasoline price of a gas station to the given
	 * time. Therefore the gasStations.csv is parsed.
	 */
	private int getRealPriceOfGasStation(int gasStationID, String time) {
		int price = 0;
		String line = "";
		String[] entry;
		String timeString;
		String priceString;

		try {
			Date arrivalTime = parseDateString(time);

			BufferedReader reader = new BufferedReader(new FileReader(
					new File(System.getProperty("user.dir") + "/../input_files/gasprices/" + gasStationID + ".csv")));

			Date lastDate = parseDateString(reader.readLine().split(";")[0]);

			while ((line = reader.readLine()) != null) {

				entry = line.split(";");
				timeString = entry[0];
				Date timeStringDate = parseDateString(timeString);
				priceString = entry[1];
				if (lastDate.before(arrivalTime) && timeStringDate.after(arrivalTime)) {
					price = Integer.parseInt(priceString);
					break;
				}
				lastDate = timeStringDate;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return price;
	}

	/**
	 * This function takes a string representing a single date and time and
	 * parses it into a java Date object. The string is expected to be in the
	 * format "yyyy-MM-dd HH:mm:ssz". It is important to notice that the offset
	 * from the GMT, indicated by the formatter z, is expected to be in the
	 * format +02, as it is in the data base and CSV files and not +0200 as it
	 * would be commonly used.
	 *
	 * @throws ParseException
	 */
	private Date parseDateString(String dateString) throws ParseException {
		// Append neccessary zeros at the end of the String
		dateString += "00";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz");
		Date date = dateFormat.parse(dateString);
		return date;
	}
}
