package carefuel.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import carefuel.model.GasStation;

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
		System.out.println("Reichweite: " + range);

		this.gasStations = new ArrayList<Node>();
		for (GasStation g : gasStations) {
			Node n = new Node(g);
			nodes.add(n);
			this.gasStations.add(n);
		}
	}

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

				// System.out.println("*********** Iteration ****************");
				// System.out.println("Nodes left: " + nodes.size());
				// System.out.println("Capacity: " + currentFill + "/" +
				// windowCapacity);
				// System.out.println("The priorityQueue contains: ");
				// for (Node n : priorityQueue) {
				// System.out.println(n.getGasStation().getID() + "(" +
				// n.getGasStation().getPredictedPrice() + ")");
				// }

				// System.out.println("\n Distance to next: " +
				// distance(slidingWindow.getFirst(), nodes.getFirst()));

				// System.out.println("Distance two first: " +
				// distance(slidingWindow.getFirst(), nodes.getFirst()));

				if (distance(slidingWindow.getFirst(), nodes.getFirst()) > windowCapacity - currentFill) {
					first = slidingWindow.getFirst();
					slidingWindow.remove(first);
					priorityQueue.remove(first);
					// System.out.println("***\n");
					// System.out.print("Removed " +
					// first.getGasStation().getID() + ", oldCapacity: " +
					// currentFill);
					currentFill -= distance(first, slidingWindow.getFirst());
					first.setNext(priorityQueue.getFirst());
					// System.out.print(", newCapacity: " + currentFill + "\n");
					// System.out.println("next of the removed is set to: " +
					// first.getNext().getGasStation().getID());
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
					// System.out.println("Added " +
					// newNode.getGasStation().getID() + "("
					// + newNode.getGasStation().getPredictedPrice() + ")");
					// System.out.println("Prev of " +
					// newNode.getGasStation().getID() + ": "
					// + newNode.getPrev().getGasStation().getID());
				}
			} else {

				// System.out.println("*********** Iteration ****************");
				// System.out.println("Nodes left: " + nodes.size());
				// System.out.println("Capacity: " + currentFill + "/" +
				// windowCapacity);
				// System.out.println("The priorityQueue contains: ");
				// for (Node n : priorityQueue) {
				// System.out.println(n.getGasStation().getID() + "(" +
				// n.getGasStation().getPredictedPrice() + ")");
				// }
				if (slidingWindow.size() > 1) {
					first = slidingWindow.getFirst();
					slidingWindow.remove(first);
					priorityQueue.remove(first);
					// currentFill -= distance(slidingWindow.getFirst(), first);
					first.setNext(priorityQueue.getFirst());
					// System.out.println("Dequeue: " +
					// first.getGasStation().getID() + "("
					// + first.getGasStation().getPredictedPrice() + ") ");
					// System.out.println("Next of " +
					// first.getGasStation().getID() + "("
					// + first.getGasStation().getPredictedPrice() + ") " + ": "
					// + first.getNext());
				} else {
					first = slidingWindow.getFirst();
					slidingWindow.remove(first);
					priorityQueue.remove(first);
					// System.out.println("Dequeue: " +
					// first.getGasStation().getID() + "("
					// + first.getGasStation().getPredictedPrice() + ") ");
				}
			}
		}
		// breakPoints.add(gasStations.get(gasStations.size() - 1));
		if (!breakPoints.contains(goal)) {
			breakPoints.add(goal);
		}

		// find path from break points
		for (int i = 0; i < breakPoints.size() - 1; i++) {
			driveToNext(breakPoints.get(i), breakPoints.get(i + 1));
		}

		double sum = 0;
		System.out.println("------------------ Nodes -----------------");
		for (int i = 0; i < gasStations.size() - 1; i++) {
			if (breakPoints.contains(gasStations.get(i))) {
				System.out.println(gasStations.get(i).getGasStation().getID() + "("
						+ gasStations.get(i).getGasStation().getPredictedPrice() + ")" + " ******");
			} else {
				System.out.println(gasStations.get(i).getGasStation().getID() + "("
						+ gasStations.get(i).getGasStation().getPredictedPrice() + ")");
			}
			System.out.println(distance(gasStations.get(i), gasStations.get(i + 1)));
			sum += distance(gasStations.get(i), gasStations.get(i + 1));
		}
		System.out.println(gasStations.get(gasStations.size() - 1).getGasStation().getID());
		System.out.println("------------------------------------------");
		System.out.println("Sum: " + sum);

		System.out.println("\nBreakPoints: ");
		for (Node n : breakPoints) {
			System.out.println(n.getGasStation().getID());
		}

		System.out.println("Distanz zwischen start und ende: "
				+ indirectDistance(gasStations.get(0), gasStations.get(gasStations.size() - 1)));

		System.out.println("\nSolution: ");
		for (Node n = breakPoints.get(0); n.getNextOnBestPath() != null; n = n.getNextOnBestPath()) {
			System.out.println(n.getGasStation().getID() + "(" + n.getGasStation().getPredictedPrice() + ")");
			System.out.println("Tank: " + n.getGasInTank() + ", Fill: " + n.getFuelToBuy() + ", d: "
					+ indirectDistance(n, n.getNextOnBestPath()) + ", liter necessary: "
					+ indirectDistance(n, n.getNextOnBestPath()) * literGasPerKilometer);
		}
		System.out.println(breakPoints.get(breakPoints.size() - 1).getGasStation().getID() + "("
				+ breakPoints.get(breakPoints.size() - 1).getGasStation().getPredictedPrice() + ")");

		return gasStations;
	}

	/**
	 * Funktion die für alle Breakpoints aufgerufen wird und den nächsten
	 * anzufahrenden Tankstopp, sowie die Menge an Benzin berechnet.
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
			from.setFuelToBuy(literGasPerKilometer * indirectDistance(from, to));
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
	 * Funktion die die Distanz zwischen zwei Tankstellen in KM berechnet.
	 * Großkreisentfernung.
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

	public double getGasConsumption() {
		return gasConsumption;
	}

	public void setGasConsumption(double gasConsumption) {
		this.gasConsumption = gasConsumption;
	}
}
