package carefuel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import carefuel.controller.PricePredictor;
import carefuel.model.GasStation;
import javafx.util.Pair;

/**
 * CSV Parser that reads the given route and the gasStations.csv for the
 * coordinates of the gas stations.
 *
 * @author jwall
 *
 */
public class Parser {

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

			System.out.println("************** Parser starts ***********************");

			while ((line = reader.readLine()) != null) {

				entry = line.split(splitBy);

				if (n == 0) {
					predictionTimeStamp = entry[0];
					n++;
				}

				// for (int i = 0; i < entry.length; i++) {
				// System.out.println("Entry " + i + ": " + entry[i]);
				// }

				Double[] tmp = getLonLat(Integer.parseInt(entry[1]));
				int predictedPrice = predictor.predictPrice(predictionTimeStamp, entry[0], Integer.parseInt(entry[1]));
				// int predictedPrice = 0;
				// System.out.println(predictedPrice);
				GasStation station = new GasStation(entry[0], Integer.parseInt(entry[1]), tmp[0], tmp[1],
						predictedPrice);
				gasStations.add(station);

				System.out.println("*");
			}

			reader.close();
			System.out.println("************** Parser ends ***********************");
			// safePredictedData();
			System.out.println("\n##### Predicted prices safed to resource/predictedPrices.txt #####");
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

			System.out.println("************** Parser starts ***********************");

			while ((line = reader.readLine()) != null) {

				entry = line.split(splitBy);

				int gasStationID = Integer.parseInt(entry[2]);
				int predictedPrice = predictor.predictPrice(entry[0], entry[1], gasStationID);
				gasStationTimePairs.add(new Pair<GasStation, String>(
						new GasStation(entry[1], gasStationID, 0.0, 0.0, predictedPrice), entry[0]));

				System.out.println(line + ";" + predictedPrice);
			}

			reader.close();
			safePredictedData(gasStationTimePairs);
			System.out.println("\n##### Predicted prices safed to resource/predictedPrices.txt #####");
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
			BufferedReader reader = new BufferedReader(
					new FileReader(new File(System.getProperty("user.dir") + "/resource/gasstations.csv")));

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
	 * Safe data predicted by the PricePredictor as .txt file to
	 * /out/pricePrediction/predictedPrices.txt
	 */
	private void safePredictedData(List<Pair<GasStation, String>> gasStationTimePairs) {
		try {
			PrintWriter out = new PrintWriter(
					System.getProperty("user.dir") + "/out/pricePrediction/" + file.getName() + "-predictedPrices.txt");

			for (Pair p : gasStationTimePairs) {
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