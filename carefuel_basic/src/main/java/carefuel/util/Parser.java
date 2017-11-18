package carefuel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import carefuel.controller.PricePredictor;
import carefuel.model.GasStation;

public class Parser {

	private File file;
	private int capacity;
	private List<GasStation> gasStations;

	public Parser(File file) {
		// to-do
		this.file = file;
	}

	public void parse() {
		System.out.println("************** Parser starts ***********************");

		String line = "";
		String[] entry;
		String splitBy = ";";

		gasStations = new ArrayList<GasStation>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			capacity = Integer.parseInt(reader.readLine());
			PricePredictor predictor = new PricePredictor();

			String maxPredictionDate = "";
			int n = 0;

			while ((line = reader.readLine()) != null) {

				entry = line.split(splitBy);

				if (n == 0) {
					maxPredictionDate = entry[0];
					n++;
				}

				// for (int i = 0; i < entry.length; i++) {
				// System.out.println("Entry " + i + ": " + entry[i]);
				// }

				Double[] tmp = getLonLat(Integer.parseInt(entry[1]));
				int predictedPrice = predictor.predictPrice(maxPredictionDate, entry[0], Integer.parseInt(entry[1]));
				// System.out.println(predictedPrice);
				GasStation station = new GasStation(entry[0], Integer.parseInt(entry[1]), tmp[0], tmp[1],
						predictedPrice);
				gasStations.add(station);
			}

			reader.close();
			System.out.println("************** Parser ends ***********************");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

					// System.out.println("Lon: " + entry[7]);
					// System.out.println("Lon: " + entry[8]);
					lonLat[0] = Double.parseDouble(entry[7]);
					lonLat[1] = Double.parseDouble(entry[8]);
					break;
				}

			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("******LONLAT: " + lonLat[0]);
		return lonLat;
	}

	public List<GasStation> getGasStations() {
		return gasStations;
	}

	public int getCapacity() {
		return this.capacity;
	}
}