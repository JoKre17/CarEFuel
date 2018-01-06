package carefuel.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.model.GasStation;

// TODO Klasse hat wenig Kommentare. Vielleicht noch etwas erweitern?

/**
 * The algorithms is the fixed path algorithm taken from "To Fill or not to
 * Fill: The Gas Station Problem" by Khuller et. al.(available at
 * https://dl.acm.org/citation.cfm?doid=1978782.1978791)
 */
public class FixedPathAlgorithm {

	private static final Logger log = LogManager.getLogger(FixedPathAlgorithm.class);

	private List<Node> gasStations;
	private int capacity;
	private double gasConsumption;
	private double literGasPerKilometer;
	private double range;
	private LinkedList<Node> nodes;
	private List<Node> breakPoints;
	private LinkedList<Node> slidingWindow;
	private MyPriorityQueue priorityQueue;
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
	public FixedPathAlgorithm(List<GasStation> gasStations, int capacity, double gasConsumption) {
		this.capacity = capacity;
		this.setGasConsumption(gasConsumption);
		this.nodes = new LinkedList<Node>();
		this.breakPoints = new ArrayList<Node>();
		this.slidingWindow = new LinkedList<Node>();
		this.priorityQueue = new MyPriorityQueue();

		this.literGasPerKilometer = gasConsumption / 100;
		this.range = capacity * (1 / literGasPerKilometer);
		// TODO System Out schon wieder! :D
		System.out.println("Range (km): " + range);

		this.gasStations = new ArrayList<Node>();
		for (GasStation g : gasStations) {
			Node n = new Node(g);
			nodes.add(n);
			this.gasStations.add(n);
		}
	}

	/**
	 * 2-parted algorithm. First part: find break points, which are the cheapest gas
	 * stations that can be reached with one tank fill. Second part: Find best
	 * tanking behavior on the fixed path.
	 *
	 * @return list of nodes with their assigned amount of gas to fill at a gas
	 *         station.
	 */
	public List<Node> run() {
		double windowCapacity = range;
		double currentFill = 0;

		// init lists
		Node first = nodes.poll();
		slidingWindow.add(first);
		priorityQueue.add(first);
		first.setPrev(priorityQueue.getFirst());
		breakPoints.add(first);

		Node goal = nodes.getLast();

		// find breaking points according to algorithms
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

		DecimalFormat df = new DecimalFormat("#0.00");
		double sum = 0;

		log.info("------------------ Gasstations -----------------");
		for (int i = 0; i < gasStations.size() - 1; i++) {
			if (gasStations.get(i).getFuelToBuy() > 0) {
				log.info(gasStations.get(i).getGasStation().getID() + " gas price: "
						+ gasStations.get(i).getGasStation().getPredictedPrice() + ", fill up "
						+ df.format(gasStations.get(i).getFuelToBuy()) + " liter");
			} else {
				log.info(gasStations.get(i).getGasStation().getID() + " gas price: "
						+ gasStations.get(i).getGasStation().getPredictedPrice());
			}
			log.info("\t" + df.format(distance(gasStations.get(i), gasStations.get(i + 1))));
			sum += distance(gasStations.get(i), gasStations.get(i + 1));
		}
		log.info(gasStations.get(gasStations.size() - 1).getGasStation().getID());
		log.info("------------------------------------------------");

		log.debug("Sum: " + df.format(sum));

		log.debug("\nBreakPoints: ");
		for (Node n : breakPoints) {
			log.debug(n.getGasStation().getID());
		}

		log.info("Distance between start and end: "
				+ df.format(indirectDistance(gasStations.get(0), gasStations.get(gasStations.size() - 1))));

		printRoutePrice(gasStations);

		return gasStations;
	}

	/**
	 * Method that is called to examine the best tanking behavior between two break
	 * points
	 *
	 * @param from
	 * @param to
	 */
	private void driveToNext(Node from, Node to) {
		log.debug("From: " + from.getGasStation().getID() + ", to: " + to.getGasStation().getID());
		log.debug("Distance: " + indirectDistance(from, to));
		if (indirectDistance(from, to) <= range) {
			// just fill enough to get to "to"
			double gasToBuy = 0.0;
			if (from.getGasInTank() < literGasPerKilometer * indirectDistance(from, to)) {
				gasToBuy = literGasPerKilometer * indirectDistance(from, to) - from.getGasInTank();
			}
			from.setFuelToBuy(gasToBuy);
			log.debug("Before - Gas In Tank: " + from.getGasInTank());
			from.setGasInTank(from.getGasInTank() + from.getFuelToBuy());
			from.setNextOnBestPath(to);
			log.debug("After - Gas In Tank: " + from.getGasInTank());
			to.setGasInTank(from.getGasInTank() - literGasPerKilometer * indirectDistance(from, to));
		} else {
			// fill the tank and drive to from.next
			from.setFuelToBuy(capacity - from.getGasInTank());
			log.debug("Before - Gas In Tank: " + from.getGasInTank());
			from.setGasInTank(from.getGasInTank() + from.getFuelToBuy());
			log.debug("After - Gas In Tank: " + from.getGasInTank());
			from.setNextOnBestPath(from.getNext());
			from.getNextOnBestPath().setGasInTank(
					from.getGasInTank() - literGasPerKilometer * indirectDistance(from, from.getNextOnBestPath()));
			driveToNext(from.getNext(), to);
		}
	}

	/**
	 * Direct distance between two nodes in km computed by the great-circle distance
	 *
	 * @param n1
	 * @param n2
	 * @return
	 */
	private double distance(Node n1, Node n2) {
		return (6378.388 * Math.acos(Math.sin(n1.getGasStation().getLat() * Math.sin(n2.getGasStation().getLat())
				+ Math.cos(n1.getGasStation().getLat() * Math.cos(n2.getGasStation().getLat()))
						* Math.cos(n2.getGasStation().getLon() - n1.getGasStation().getLon()))))
				/ 1000;
	}

	/**
	 * Computes the indirect distance between two not necessarily directly connected
	 * gas stations.
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

	private void printRoutePrice(List<Node> route) {
		double price = 0;
		for (Node n : route) {
			price += n.getFuelToBuy() * (n.getGasStation().getPredictedPrice() / 10.0);
		}
		log.info("\nPrice of the route is: " + df.format(price / 100) + " Eur");
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
