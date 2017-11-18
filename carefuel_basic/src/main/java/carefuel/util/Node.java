package carefuel.util;

import carefuel.model.GasStation;

public class Node implements Comparable<Node> {

	private Node next;
	private Node prev;
	private GasStation gasStation;
	private Node nextOnBestPath;
	private double fuelToBuy = 0;
	private double gasInTank = 0;

	public Node(GasStation g) {
		this.setGasStation(g);
	}

	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}

	public Node getPrev() {
		return prev;
	}

	public void setPrev(Node prev) {
		this.prev = prev;
	}

	public GasStation getGasStation() {
		return gasStation;
	}

	public void setGasStation(GasStation gasStation) {
		this.gasStation = gasStation;
	}

	public Node getNextOnBestPath() {
		return nextOnBestPath;
	}

	public void setNextOnBestPath(Node nextOnBestPath) {
		this.nextOnBestPath = nextOnBestPath;
	}

	public double getFuelToBuy() {
		return fuelToBuy;
	}

	public void setFuelToBuy(double fuelToBuy) {
		this.fuelToBuy = fuelToBuy;
	}

	public double getGasInTank() {
		return gasInTank;
	}

	public void setGasInTank(double gasInTank) {
		this.gasInTank = gasInTank;
	}

	public int compareTo(Node arg0) {
		// TODO Auto-generated method stub
		return (this.getGasStation().getPredictedPrice() >= arg0.getGasStation().getPredictedPrice() ? 1 : -1);
	}

}
