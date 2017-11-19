package carefuel.controller;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import carefuel.model.GasStation;
import carefuel.util.FixedPathAlgorithm;
import carefuel.util.Node;

public class PathFinder {

	private List<GasStation> gasStations;
	private List<Node> bestPath;
	private int capacity;
	private final double gasConsumption = 5.6;

	public PathFinder(List<GasStation> gasStations, int capacity) {
		this.gasStations = gasStations;
		this.capacity = capacity;
	}

	public void computeBestPath() {
		FixedPathAlgorithm f = new FixedPathAlgorithm(gasStations, capacity, gasConsumption);
		bestPath = f.run();
		safePath();
	}

	private void safePath() {
		try {
			PrintWriter out = new PrintWriter(System.getProperty("user.dir") + "/resource/solution.txt");

			for (Node n : bestPath) {
				out.println(n.getGasStation().getArrivalDate() + ";" + n.getGasStation().getID() + ";"
						+ n.getGasStation().getPredictedPrice() + ";" + n.getFuelToBuy());
			}
			out.flush();
			out.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}