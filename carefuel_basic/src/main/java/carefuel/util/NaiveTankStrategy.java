package carefuel.util;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.model.GasStation;

/**
 * Function that computes the naive tanking strategy of tanking just what we
 * need to get to the next gas station at every gas station.
 *
 * @author jwall
 *
 */
public class NaiveTankStrategy {

	private static final Logger log = LogManager.getLogger(NaiveTankStrategy.class);

	private int capacity;
	private double gasConsumption;
	private double literGasPerKilometer;
	private double range;
	private List<GasStation> gasStations;
	private DecimalFormat df = new DecimalFormat("#0.00");

	/**
	 * Constructor of FixedPathAlgorithm
	 *
	 * @param gasStations
	 *            list of gas stations along the given route
	 * @param capacity
	 *            gas capacity
	 * @param gasConsumption
	 *            gas consumption per 100km
	 */
	public NaiveTankStrategy(List<GasStation> gasStations, int capacity, double gasConsumption) {
		this.setCapacity(capacity);
		this.setGasConsumption(gasConsumption);
		this.gasStations = gasStations;
		this.literGasPerKilometer = gasConsumption / 100;
		this.range = capacity * (1 / literGasPerKilometer);
	}

	/**
	 * Computes the neccessary amout of gas to get to the next gas station and
	 * fill the tank accordingly. The total price of the route is printed at the
	 * end.
	 */
	public void run() {
		List<Node> nodes = new LinkedList<Node>();

		for (GasStation g : gasStations) {
			Node n = new Node(g);
			nodes.add(n);
		}

		for (int i = 0; i < nodes.size() - 1; i++) {
			Node n = nodes.get(i);

			double fuelToBuy = literGasPerKilometer * distance(n.getGasStation(), nodes.get(i + 1).getGasStation());
			n.setFuelToBuy(fuelToBuy);
		}

		printRoutePrice(nodes);
	}

	/**
	 * Direct distance between two nodes in km computed by the great-circle
	 * distance
	 *
	 * @param n1
	 * @param n2
	 * @return
	 */
	private double distance(GasStation g1, GasStation g2) {
		double lat_a = Math.toRadians(g1.getLat());
		double lon_a = Math.toRadians(g1.getLon());
		double lat_b = Math.toRadians(g2.getLat());
		double lon_b = Math.toRadians(g2.getLon());

		return 6378.388 * Math
				.acos(Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a) * Math.cos(lat_b) * Math.cos(lon_b - lon_a));
	}

	private void printRoutePrice(List<Node> route) {
		double price = 0;
		for (Node n : route) {
			price += n.getFuelToBuy() * (n.getGasStation().getPredictedPrice() / 10.0);
		}
		log.info("\nNaive Price (if you fill just enough gas to get to next gas station): " + df.format(price / 100)
				+ " Eur");
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public double getGasConsumption() {
		return gasConsumption;
	}

	public void setGasConsumption(double gasConsumption) {
		this.gasConsumption = gasConsumption;
	}
}
