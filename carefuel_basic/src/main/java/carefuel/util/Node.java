package carefuel.util;

import carefuel.model.GasStation;

/**
 * Wrapper class the wrap the gas stations for the algorithm.
 *
 * @author jwall
 *
 */
public class Node implements Comparable<Node> {

	private Node next;
	private Node prev;
	private GasStation gasStation;
	private Node nextOnBestPath;
	private double fuelToBuy = 0;
	private double gasInTank = 0;

	/**
	 * Constructor of the Node. Nodes contain the data necessary for the
	 * algorithm.
	 *
	 * @param g
	 *            gas station
	 */
	public Node(GasStation g) {
		this.setGasStation(g);
	}

	/**
	 * @return next gas station
	 */
	public Node getNext() {
		return next;
	}

	/**
	 * @param next
	 *            new next
	 */
	public void setNext(Node next) {
		this.next = next;
	}

	/**
	 * @return previous gas station
	 */
	public Node getPrev() {
		return prev;
	}

	/**
	 * @param prev
	 *            new prev
	 */
	public void setPrev(Node prev) {
		this.prev = prev;
	}

	/**
	 * @return gas station
	 */
	public GasStation getGasStation() {
		return gasStation;
	}

	/**
	 * @param gasStation
	 *            new gas station
	 */
	public void setGasStation(GasStation gasStation) {
		this.gasStation = gasStation;
	}

	/**
	 * @return new node on the optimal path
	 */
	public Node getNextOnBestPath() {
		return nextOnBestPath;
	}

	/**
	 * @param nextOnBestPath
	 *            new best next node on best path
	 */
	public void setNextOnBestPath(Node nextOnBestPath) {
		this.nextOnBestPath = nextOnBestPath;
	}

	/**
	 * @return returns the amount of fuel to buy in liter
	 */
	public double getFuelToBuy() {
		return fuelToBuy;
	}

	/**
	 * @param fuelToBuy
	 *            new amount of fuel to buy
	 */
	public void setFuelToBuy(double fuelToBuy) {
		this.fuelToBuy = fuelToBuy;
	}

	/**
	 * @return amount of gas in the tank in liter
	 */
	public double getGasInTank() {
		return gasInTank;
	}

	/**
	 * @param gasInTank
	 *            updates the amount of gas in the tank
	 */
	public void setGasInTank(double gasInTank) {
		this.gasInTank = gasInTank;
	}

	/**
	 * CompareTo-Method that is used to order the nodes in the PriorityQueue.
	 * The Nodes are sorted ascending by their predicted price
	 */
	public int compareTo(Node arg0) {
		return (this.getGasStation().getPredictedPrice() >= arg0.getGasStation().getPredictedPrice() ? 1 : -1);
	}

}
