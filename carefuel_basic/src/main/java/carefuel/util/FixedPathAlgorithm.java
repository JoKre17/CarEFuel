package carefuel.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import carefuel.model.GasStation;

public class FixedPathAlgorithm {

	private List<GasStation> gasStations;
	private int capacity;
	private double gasConsumption;
	private double literGasPerKilometer;
	private double range;
	private LinkedList<Node> nodes;
	private List<Node> breakPoints;
	private LinkedList<Node> slidingWindow;
	// private Queue<Node> priorityQueue;
	private MyPriorityQueue priorityQueue;

	public FixedPathAlgorithm(List<GasStation> gasStations, int capacity, double gasConsumption) {
		this.setGasStations(gasStations);
		this.capacity = capacity;
		this.setGasConsumption(gasConsumption);
		this.nodes = new LinkedList<Node>();
		this.breakPoints = new ArrayList<Node>();
		this.slidingWindow = new LinkedList<Node>();
		// this.priorityQueue = new PriorityQueue<Node>();
		this.priorityQueue = new MyPriorityQueue();

		this.literGasPerKilometer = gasConsumption / 100;
		this.range = capacity * (1 / literGasPerKilometer);
		System.out.println("Reichweite: " + range);

		for (GasStation g : gasStations) {
			nodes.add(new Node(g));
		}
	}

	public void run() {
		
		//init lists
		double windowCapacity = range;
		double currentFill = 0;
		
		Node first = nodes.poll();
		slidingWindow.add(first);
		priorityQueue.add(first);
		first.setPrev(priorityQueue.getFirst());
		
		System.out.println("Capacity: " + windowCapacity);

		System.out.println("------------------------------------------ Nodes-----------------");
		for (Node n : nodes) {
			System.out.println(n.getGasStation().getID());
		}
		
		while(!slidingWindow.isEmpty()) {
			if (!nodes.isEmpty()) {
				
				System.out.println("*********** Iteration ****************");
				System.out.println("Nodes left: " + nodes.size());
				System.out.println("Capacity: " + currentFill + "/" + windowCapacity);
				System.out.println("The priorityQueue contains: ");
				for (Node n : priorityQueue) {
					System.out.println(n.getGasStation().getID() + "(" + n.getGasStation().getPredictedPrice() + ")");
				}

				System.out.println("\n Distance to next: " + distance(slidingWindow.getFirst(), nodes.getFirst()));


				if (distance(slidingWindow.getFirst(), nodes.getFirst()) > windowCapacity - currentFill) {
					first = slidingWindow.getFirst();
					slidingWindow.remove(first);
					priorityQueue.remove(first);
					System.out.println("***\n");
					System.out.print("Removed " + first.getGasStation().getID() + ", oldCapacity: " + currentFill);
					currentFill -= distance(first, slidingWindow.getFirst());
					first.setNext(priorityQueue.getFirst());
					System.out.print(", newCapacity: " + currentFill + "\n");
					System.out.println("next of the removed is set to: " + first.getNext().getGasStation().getID());
				}
				else {
					Node last = slidingWindow.getLast();
					Node newNode = nodes.poll();
					slidingWindow.add(newNode);
					priorityQueue.add(newNode);
					currentFill += distance(last, newNode);
					newNode.setPrev(priorityQueue.getFirst());
					System.out.println("Added " + newNode.getGasStation().getID() + "(" + newNode.getGasStation().getPredictedPrice() + ")");
					System.out.println("Prev of " + newNode.getGasStation().getID() + ": "
							+ newNode.getPrev().getGasStation().getID());
				}
			}
			else {
				first = slidingWindow.getFirst();
				slidingWindow.remove(first);
				priorityQueue.remove(first);
				//currentFill -= distance(slidingWindow.getFirst(), first);
				//first.setNext(priorityQueue.getFirst());
			}
		}
		
		// find path from break points
		for (Node n : breakPoints) {
			driveToNext(n, null);
		}
	}

	/**
	 * Funktion die für alle Breakpoints aufgerufen wird und den nächsten
	 * anzufahrenden Tankstopp, sowie die Menge an Benzin berechnet.
	 * 
	 * @param from
	 * @param to
	 */
	private void driveToNext(Node from, Node to) {
		if (distance(from, to) <= range) {
			// just fill enough to get to "to"
			from.setFuelToBuy(literGasPerKilometer * distance(from, to));
			from.setNextOnBestPath(to);
		} else {
			// fill the tank and drive to from.next
			from.setFuelToBuy(capacity - from.getGasInTank());
			from.setNextOnBestPath(from.getNext());
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
						* Math.cos(n2.getGasStation().getLon() - n1.getGasStation().getLon())))
				/ 1000);
	}

	public List<GasStation> getGasStations() {
		return gasStations;
	}

	public void setGasStations(List<GasStation> gasStations) {
		this.gasStations = gasStations;
	}

	public double getGasConsumption() {
		return gasConsumption;
	}

	public void setGasConsumption(double gasConsumption) {
		this.gasConsumption = gasConsumption;
	}
}
