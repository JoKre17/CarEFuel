package carefuel.controller;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import carefuel.model.GasStation;
import carefuel.util.FixedPathAlgorithm;
import carefuel.util.Node;

/**
 *
 * This class uses the given route-data and runs the FixedPathAlgorithm with it.
 *
 * @author jwall
 *
 */
public class PathFinder {

	private List<GasStation> gasStations;
	private List<Node> bestPath;
	private int capacity;

	private final double gasConsumption = 5.6;
	private String fileName;

	/**
	 * Constructor of the Path finder.
	 *
	 * @param gasStations
	 *            List of GasStations which will be visited
	 * @param capacity
	 *            maximum gasoline tank capacity of the vehicle
	 * @param fileName
	 *            prefix of the file, where the calculated path is saved
	 */
	public PathFinder(List<GasStation> gasStations, int capacity, String fileName) {
		this.gasStations = gasStations;
		this.capacity = capacity;
		this.fileName = fileName;
	}

	/**
	 * This function starts the computation of the best path and saves it to a
	 * .txt file. The algorithms is the fixed path algorithm taken from "To Fill
	 * or not to Fill: The Gas Station Problem" by Khuller et. al.(available at
	 * https://dl.acm.org/citation.cfm?doid=1978782.1978791)
	 */
	public void computeBestPath() {
		FixedPathAlgorithm f = new FixedPathAlgorithm(gasStations, capacity, gasConsumption);
		bestPath = f.run();
		if (bestPath != null) {
			savePath();
		}
	}

	/**
	 * Function used to save the computed path to a plain text file (.txt). The
	 * file is located at /resource/solution.txt
	 */
	private void savePath() {
		try {

			// Default is the directory above
			String routesOutDirectory = System.getProperty("user.dir") + "/out/routes/";
			if (System.getProperty("routesDir") != null) {
				routesOutDirectory = System.getProperty("routesOutDir");
			}

			PrintWriter out = new PrintWriter(routesOutDirectory + fileName + "-solution.txt");

			for (Node n : bestPath) {
				out.println(n.getGasStation().getArrivalDate() + ";" + n.getGasStation().getID() + ";"
						+ n.getGasStation().getPredictedPrice() + ";" + n.getFuelToBuy());
			}
			out.flush();
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}