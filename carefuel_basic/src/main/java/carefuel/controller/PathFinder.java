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
	 * @param capacity
	 */
	public PathFinder(List<GasStation> gasStations, int capacity, String fileName) {
		this.gasStations = gasStations;
		this.capacity = capacity;
		this.fileName = fileName;
	}

	/**
	 * This function starts the computation of the best path and safe it to a
	 * .txt file. The algorithms is the fixed path algorithm taken from "To Fill
	 * or not to Fill: The Gas Station Problem" by Khuller et. al.(available at
	 * https://dl.acm.org/citation.cfm?doid=1978782.1978791)
	 */
	public void computeBestPath() {
		FixedPathAlgorithm f = new FixedPathAlgorithm(gasStations, capacity, gasConsumption);
		bestPath = f.run();
		safePath();
	}

	/**
	 * Function used to safe the computed path to a plain text file (.txt). The
	 * file is located at /resource/solution.txt
	 */
	private void safePath() {
		try {
			PrintWriter out = new PrintWriter(
					System.getProperty("user.dir") + "/out/routes/" + fileName + "-solution.txt");

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