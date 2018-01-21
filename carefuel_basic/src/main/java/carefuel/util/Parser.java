package carefuel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.PricePredictor;
import carefuel.model.GasStation;

/**
 * CSV Parser that reads the given route and the gasStations.csv for the
 * coordinates of the gas stations.
 *
 * @author jwall
 *
 */
public class Parser {

	private static final Logger log = LogManager.getLogger(Parser.class);

	private File file;
	private int capacity;
	private List<GasStation> gasStations;
	private String predictionTimeStamp = "";

	/**
	 * Constructor of the Parser
	 *
	 * @param file
	 *            route-file that should be parsed.
	 */
	public Parser(File file) {
		this.file = file;
	}

	/**
	 * Actual parsing of the route-file. Extracting the gas station information.
	 */
	public void parseRoute() {
		String line = "";
		String[] entry;
		String splitBy = ";";

		gasStations = new ArrayList<GasStation>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			capacity = Integer.parseInt(reader.readLine());
			PricePredictor predictor = new PricePredictor();

			predictionTimeStamp = "";
			int n = 0;

			log.info("************** Parser starts ***********************");

			while ((line = reader.readLine()) != null) {

				entry = line.split(splitBy);

				if (n == 0) {
					predictionTimeStamp = entry[0];
					n++;
				}

				Double[] tmp = getLonLat(Integer.parseInt(entry[1]));
				int predictedPrice = predictor.predictPrice(predictionTimeStamp, entry[0], Integer.parseInt(entry[1]));
				GasStation station = new GasStation(entry[0], Integer.parseInt(entry[1]), tmp[0], tmp[1],
						predictedPrice);
				gasStations.add(station);

				log.info("*");
			}

			reader.close();
			log.info("************** Parser ends ***********************");
			log.info("\n##### route information safed to out/routes/" + file.getName() + " #####");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parses the gasStations File with the gas stations to predict prices for.
	 *
	 * @param file
	 *            File containing the gas stations, which priced shall be
	 *            predicted
	 */
	public void parseGasStationsToPredict(File file) {
		this.file = file;

		String line = "";
		String[] entry;
		String splitBy = ";";

		gasStations = new ArrayList<GasStation>();

		List<Pair<GasStation, String>> gasStationTimePairs = new ArrayList<Pair<GasStation, String>>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			PricePredictor predictor = new PricePredictor();

			log.info("************ Predicted Prices *****************");

			while ((line = reader.readLine()) != null) {

				entry = line.split(splitBy);

				int gasStationID = Integer.parseInt(entry[2]);
				int predictedPrice = predictor.predictPrice(entry[0], entry[1], gasStationID);
				gasStationTimePairs.add(new Pair<GasStation, String>(
						new GasStation(entry[1], gasStationID, 0.0, 0.0, predictedPrice), entry[0]));

				log.info(line + ";" + predictedPrice);
			}

			reader.close();
			savePredictedData(gasStationTimePairs);
			log.info("\n##### Predicted prices safed to out/pricePrediction/" + file.getName() + ".txt #####");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Function that parses the gasStations.csv for lon/lat coordinates of a gas
	 * station.
	 *
	 * @param gasStationID
	 *            ID of the gas station to look for
	 * @return
	 */
	public Double[] getLonLat(int gasStationID) {
		Double[] lonLat = { 0.0, 0.0 };

		String line = "";
		String[] entry;
		String splitBy = ";";

		try {
			// Default is the directory above
			String gasStationsDirectory = System.getProperty("user.dir") + "/resource/gasstations.csv/";
			if (System.getProperty("gasStationsDir") != null) {
				gasStationsDirectory = System.getProperty("gasStationsDir");
			}

			BufferedReader reader = new BufferedReader(new FileReader(new File(gasStationsDirectory)));

			while ((line = reader.readLine()) != null) {

				entry = line.split(splitBy);
				if (entry[0].equals(gasStationID + "")) {
					lonLat[0] = Double.parseDouble(entry[7]);
					lonLat[1] = Double.parseDouble(entry[8]);
					break;
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lonLat;
	}

	/**
	 * Save data predicted by the PricePredictor as .txt file to
	 * /out/pricePrediction/predictedPrices.txt
	 */
	private void savePredictedData(List<Pair<GasStation, String>> gasStationTimePairs) {
		try {

			// Default is the directory above
			String pricesOutDirectory = System.getProperty("user.dir") + "/out/pricePrediction/";
			if (System.getProperty("pricesOutDir") != null) {
				pricesOutDirectory = System.getProperty("pricesOutDir");
			}

			PrintWriter out = new PrintWriter(pricesOutDirectory + file.getName() + "-predictedPrices.txt");

			for (Pair<?, ?> p : gasStationTimePairs) {
				out.println(p.getValue() + ";" + ((GasStation) p.getKey()).getArrivalDate() + ";"
						+ ((GasStation) p.getKey()).getID() + ";" + ((GasStation) p.getKey()).getPredictedPrice());
			}
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return list of gas stations on the route
	 */
	public List<GasStation> getGasStations() {
		return gasStations;
	}

	/**
	 * @return tank capacity as specified by the route-file
	 */
	public int getCapacity() {
		return this.capacity;
	}
}