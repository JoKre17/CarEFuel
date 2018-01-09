package carefuel.tank;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.util.Pair;

import carefuel.controller.DatabaseHandler;
import carefuel.controller.Fuel;
import carefuel.model.GasStation;
import carefuel.path.Vertex;

public class TankStrategy {

	private static final Logger log = LogManager.getLogger(TankStrategy.class);

	private DatabaseHandler dbHandler;

	private int capacity;
	private double gasConsumption;
	private double literGasPerKilometer;
	private double range;
	private LinkedList<Node> nodes;
	private List<Node> breakPoints;
	private LinkedList<Node> slidingWindow;
	private MyPriorityQueue priorityQueue;
	List<Pair<GasStation, Double>> route;

	public TankStrategy(DatabaseHandler dbHandler) {
		this.dbHandler = dbHandler;
	}

	// Function that computes the tanking strategy for the given route. The
	// algorithms is an implementation of the
	// fixed-path gas station problem given in "to fill or not to fill" (accessible
	// at https://dl.acm.org/citation.cfm?id=1978791)
	public List<Node> computeTankStrategy(List<Vertex<GasStation>> path, Date startTime, double consumption,
			int tankLevel, int capacity, double range, float averageSpeed, Fuel gasType) {

		setup(path, startTime, consumption, tankLevel, capacity, range, averageSpeed, gasType);

		double windowCapacity = range;
		double currentFill = 0;

		// init lists
		Node first = nodes.poll();
		slidingWindow.add(first);
		priorityQueue.add(first);
		first.setPrev(priorityQueue.getFirst());
		breakPoints.add(first);

		Node goal = nodes.getLast();

		// find breaking points according to algorithm
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

		return nodes;
	}

	/**
	 * Method that is called to examine the best tanking behavior between two break
	 * points
	 *
	 * @param from
	 * @param to
	 */
	private void driveToNext(Node from, Node to) {
		log.debug("From: " + from.getValue().getId() + ", to: " + to.getValue().getId());
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
	 * Prepares all variables for the algorithm.
	 * 
	 * @param startTime
	 * @param consumption
	 * @param tankLevel
	 * @param capacity
	 * @param range
	 */
	private void setup(List<Vertex<GasStation>> path, Date startTime, double consumption, double tankLevel,
			int capacity, double range, float averageSpeed, Fuel gasType) {
		// variables
		this.range = range;
		this.capacity = capacity;
		this.gasConsumption = consumption;
		this.literGasPerKilometer = gasConsumption / 100;

		// initialize lists
		this.nodes = new LinkedList<Node>();
		this.breakPoints = new ArrayList<Node>();
		this.slidingWindow = new LinkedList<Node>();
		this.priorityQueue = new MyPriorityQueue();
		this.route = new ArrayList<Pair<GasStation, Double>>();

		// convert list
		List<GasStation> realPath = new ArrayList<GasStation>();
		for (Vertex<GasStation> v : path) {
			realPath.add(v.getValue());
		}

		// convert List<GasStation> to List<Node>
		for (GasStation g : realPath) {
			Node n = new Node(g);
			nodes.add(n);
		}

		// set tankLevel for the first gasStation
		nodes.getFirst().setGasInTank(tankLevel);

		// add arrival times to nodes
		Calendar calendar = Calendar.getInstance();

		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);

			if (i == 0) {
				n.setArrivalTime(startTime);
			} else {
				Date currentTime = nodes.get(i - 1).getArrivalTime();
				calendar.setTime(currentTime);
				int timeInMins = (int) ((indirectDistance(nodes.get(i - 1), n) / averageSpeed) * 60.0);
				calendar.add(Calendar.MINUTE, timeInMins);
				Date arrivalTime = calendar.getTime();
				n.setArrivalTime(arrivalTime);
			}
		}

		// add predicted prices to the nodes
		for (Node n : nodes) {
			long arrivalTimeLong = n.getArrivalTime().getTime();
			List<Pair<Date, Integer>> predictions = dbHandler.getPricePrediction(n.getValue().getId(), gasType);

			int pricePredictionInCentiCent = Collections.min(predictions, new Comparator<Pair<Date, Integer>>() {
				@Override
				public int compare(Pair<Date, Integer> d1, Pair<Date, Integer> d2) {
					long diff1 = Math.abs(d1.getFirst().getTime() - arrivalTimeLong);
					long diff2 = Math.abs(d2.getFirst().getTime() - arrivalTimeLong);
					return Long.compare(diff1, diff2);
				}
			}).getSecond();

			// Diesel : 1109 means 110.9 cent. Therefore 1109 is given in "centicent"
			double pricePrediction = pricePredictionInCentiCent / 10.0;
			n.setPredictedPrice(pricePrediction);
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
		return (6378.388 * Math.acos(Math.sin(n1.getValue().getLatitude() * Math.sin(n2.getValue().getLatitude())
				+ Math.cos(n1.getValue().getLatitude() * Math.cos(n2.getValue().getLatitude()))
						* Math.cos(n2.getValue().getLongitude() - n1.getValue().getLongitude()))))
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

		int indexOfN1 = nodes.indexOf(n1);
		int indexOfN2 = nodes.indexOf(n2);

		int firstIndex = (indexOfN1 <= indexOfN2) ? indexOfN1 : indexOfN2;
		int lastIndex = (indexOfN1 <= indexOfN2) ? indexOfN2 : indexOfN1;

		for (int i = firstIndex; i < lastIndex; i++) {
			distance += distance(nodes.get(i), nodes.get(i + 1));
		}
		return distance;
	}

}
