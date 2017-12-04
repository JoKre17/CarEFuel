package carefuel.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import carefuel.model.GasStation;

/**
 * The algorithms is the fixed path algorithm taken from "To Fill or not to
 * Fill: The Gas Station Problem" by Khuller et. al.(available at
 * https://dl.acm.org/citation.cfm?doid=1978782.1978791)
 */
public class FixedPathAlgorithm {

	private List<Node> gasStations;
	private int capacity;
	private double gasConsumption;
	private double literGasPerKilometer;
	private double range;
	private LinkedList<Node> nodes;
	private List<Node> breakPoints;
	private LinkedList<Node> slidingWindow;
	private MyPriorityQueue priorityQueue;

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
	public FixedPathAlgorithm(List<GasStation> gasStations, int capacity, double gasConsumption) {
		// this.setGasStations(gasStations);
		this.capacity = capacity;
		this.setGasConsumption(gasConsumption);
		this.nodes = new LinkedList<Node>();
		this.breakPoints = new ArrayList<Node>();
		this.slidingWindow = new LinkedList<Node>();
		this.priorityQueue = new MyPriorityQueue();

		this.literGasPerKilometer = gasConsumption / 100;
		this.range = capacity * (1 / literGasPerKilometer);
		System.out.println("Range (km): " + range);

		this.gasStations = new ArrayList<Node>();
		for (GasStation g : gasStations) {
			Node n = new Node(g);
			nodes.add(n);
			this.gasStations.add(n);
		}
	}

	/**
	 * 2-parted algorithm. First part: find break points, which are the cheapest
	 * gas stations that can be reached with one tank fill. Second part: Find
	 * best tanking behavior on the fixed path.
	 *
	 * @return list of nodes with their assigned amount of gas to fill at a gas
	 *         station.
	 */
	public List<Node> run() {
		// init lists
		double windowCapacity = range;
		double currentFill = 0;

		Node first = nodes.poll();
		slidingWindow.add(first);
		priorityQueue.add(first);
		first.setPrev(priorityQueue.getFirst());
		breakPoints.add(first);

		Node goal = nodes.getLast();

		// System.out.println("WindowCapacity: " + windowCapacity);

		while (!slidingWindow.isEmpty()) {
			if (!nodes.isEmpty()) {
				if (distance(slidingWindow.getFirst(), nodes.getFirst()) > windowCapacity - currentFill) {
					first = slidingWindow.getFirst();
					slidingWindow.remove(first);
					priorityQueue.remove(first);
					currentFill -= distance(first, slidingWindow.getFirst());
					first.setNext(priorityQueue.getFirst());
				} else {
					Node last = slidingWindow.getLast();
					Node newNode = nodes.poll();
					slidingWindow.add(newNode);
					priorityQueue.add(newNode);
					currentFill += distance(last, newNode);
					newNode.setPrev(priorityQueue.getFirst());
					if (newNode.equals(newNode.getPrev())) {
						breakPoints.add(newNode);
					}
				}
			} else {
				if (slidingWindow.size() > 1) {
					first = slidingWindow.getFirst();
					slidingWindow.remove(first);
					priorityQueue.remove(first);
					first.setNext(priorityQueue.getFirst());
				} else {
					first = slidingWindow.getFirst();
					slidingWindow.remove(first);
					priorityQueue.remove(first);
				}
			}
		}
		if (!breakPoints.contains(goal)) {
			breakPoints.add(goal);
		}

		// find path from break points
		for (int i = 0; i < breakPoints.size() - 1; i++) {
			driveToNext(breakPoints.get(i), breakPoints.get(i + 1));
		}

		DecimalFormat df = new DecimalFormat("#.00");

		// double sum = 0;
		System.out.println("------------------ Gasstations -----------------");
		for (int i = 0; i < gasStations.size() - 1; i++) {
			System.out.println(gasStations.get(i).getGasStation().getID() + " gas price: "
					+ gasStations.get(i).getGasStation().getPredictedPrice());
			System.out.println("\t" + df.format(distance(gasStations.get(i), gasStations.get(i + 1))));
			// sum += distance(gasStations.get(i), gasStations.get(i + 1));
		}
		System.out.println(gasStations.get(gasStations.size() - 1).getGasStation().getID());
		System.out.println("------------------------------------------------");
		// System.out.println("Sum: " + df.format(sum));

		/*
		 * System.out.println("\nBreakPoints: "); for (Node n : breakPoints) {
		 * System.out.println(n.getGasStation().getID()); }
		 */

		System.out.println("Distance between start and end: "
				+ df.format(indirectDistance(gasStations.get(0), gasStations.get(gasStations.size() - 1))));

		/*
		 * System.out.println("\nSolution: "); for (Node n = breakPoints.get(0);
		 * n.getNextOnBestPath() != null; n = n.getNextOnBestPath()) {
		 * System.out.println(n.getGasStation().getID() + "(" +
		 * n.getGasStation().getPredictedPrice() + ")");
		 * System.out.println("Tank: " + n.getGasInTank() + ", Fill: " +
		 * n.getFuelToBuy() + ", d: " + indirectDistance(n,
		 * n.getNextOnBestPath()) + ", liter necessary: " + indirectDistance(n,
		 * n.getNextOnBestPath()) * literGasPerKilometer); }
		 * System.out.println(breakPoints.get(breakPoints.size() -
		 * 1).getGasStation().getID() + "(" + breakPoints.get(breakPoints.size()
		 * - 1).getGasStation().getPredictedPrice() + ")");
		 */

		return gasStations;
	}

	/**
	 * Function that is called to examine the best tanking behavior between two
	 * break points
	 *
	 * @param from
	 * @param to
	 */
	private void driveToNext(Node from, Node to) {
		// System.out.println("From: " + from.getGasStation().getID() + ", to: "
		// + to.getGasStation().getID());
		// System.out.println("Distance: " + indirectDistance(from, to));
		if (indirectDistance(from, to) <= range) {
			// just fill enough to get to "to"
			double gasToBuy = 0.0;
			if (from.getGasInTank() < literGasPerKilometer * indirectDistance(from, to)) {
				gasToBuy = literGasPerKilometer * indirectDistance(from, to) - from.getGasInTank();
			}
			from.setFuelToBuy(gasToBuy);
			// System.out.println("Before - Gas In Tank: " +
			// from.getGasInTank());
			from.setGasInTank(from.getGasInTank() + from.getFuelToBuy());
			from.setNextOnBestPath(to);
			// System.out.println("After - Gas In Tank: " +
			// from.getGasInTank());
			to.setGasInTank(from.getGasInTank() - literGasPerKilometer * indirectDistance(from, to));
		} else {
			// fill the tank and drive to from.next
			from.setFuelToBuy(capacity - from.getGasInTank());
			// System.out.println("Before - Gas In Tank: " +
			// from.getGasInTank());
			from.setGasInTank(from.getGasInTank() + from.getFuelToBuy());
			// System.out.println("After - Gas In Tank: " +
			// from.getGasInTank());
			from.setNextOnBestPath(from.getNext());
			from.getNextOnBestPath().setGasInTank(
					from.getGasInTank() - literGasPerKilometer * indirectDistance(from, from.getNextOnBestPath()));
			driveToNext(from.getNext(), to);
		}
	}

	/**
	 * Direct distance between two nodes in km computed by the great-circle
	 * distance
	 *
	 * @param n1
	 * @param n2
	 * @return
	 */
	private double distance(Node n1, Node n2) {
		// berechne Distanz
		return (6378.388 * Math.acos(Math.sin(n1.getGasStation().getLat() * Math.sin(n2.getGasStation().getLat())
				+ Math.cos(n1.getGasStation().getLat() * Math.cos(n2.getGasStation().getLat()))
						* Math.cos(n2.getGasStation().getLon() - n1.getGasStation().getLon()))))
				/ 1000;
	}

	/**
	 * Computes the indirect distance between two not necessarily directly
	 * connected gas stations.
	 *
	 * @param n1
	 * @param n2
	 * @return
	 */
	private double indirectDistance(Node n1, Node n2) {
		double distance = 0;

		int indexOfN1 = gasStations.indexOf(n1);
		int indexOfN2 = gasStations.indexOf(n2);

		int firstIndex = (indexOfN1 <= indexOfN2) ? indexOfN1 : indexOfN2;
		int lastIndex = (indexOfN1 <= indexOfN2) ? indexOfN2 : indexOfN1;

		for (int i = firstIndex; i < lastIndex; i++) {
			distance += distance(gasStations.get(i), gasStations.get(i + 1));
		}
		return distance;
	}

	/**
	 * @return gas consumption
	 */
	public double getGasConsumption() {
		return gasConsumption;
	}

	/**
	 * @param gasConsumption
	 *            new gas consumption for the algorithm
	 */
	public void setGasConsumption(double gasConsumption) {
		this.gasConsumption = gasConsumption;
	}
}
