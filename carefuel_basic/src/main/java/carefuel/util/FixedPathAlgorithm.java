package carefuel.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import carefuel.model.GasStation;

public class FixedPathAlgorithm {

	private List<GasStation> gasStations;
	private int capacity;
	private double gasConsumption;
	private double literGasPerKilometer;
	private double range;
	private List<Node> nodes;
	private List<Node> breakPoints;
	private LinkedList<Node> slidingWindow;
	private Queue<Node> priorityQueue;

	public FixedPathAlgorithm(List<GasStation> gasStations, int capacity, double gasConsumption) {
		this.setGasStations(gasStations);
		this.capacity = capacity;
		this.setGasConsumption(gasConsumption);
		this.nodes = new ArrayList<Node>();
		this.breakPoints = new ArrayList<Node>();
		this.slidingWindow = new LinkedList<Node>();
		this.priorityQueue = new PriorityQueue<Node>();

		this.literGasPerKilometer = gasConsumption / 100;
		this.range = capacity * (1 / literGasPerKilometer);
		System.out.println("Reichweite: " + range);

		for (GasStation g : gasStations) {
			nodes.add(new Node(g));
		}
	}

	public void run() {
		// find break points
		slidingWindow.add(nodes.get(0));
		priorityQueue.add(nodes.get(0));
		double windowCapacity = range;
		double currentFill = 0;
		
		System.out.println("Capacity: " + windowCapacity);

		System.out.println("------------------------------------------ Nodes-----------------");
		for (Node n : nodes) {
			System.out.println(n.getGasStation().getID());
		}
		
		
		for (int i = 0; !slidingWindow.isEmpty(); i++) {
			System.out.println("*********** Iteration " + i + " ****************");
			System.out.println("Capacity: " + currentFill + "/" + windowCapacity);
			/*System.out.println("The slidingwindow contains: ");
			for (Node n : slidingWindow) {
				System.out.println(n.getGasStation().getID() + ", ");
			}*/
			System.out.println("The priorityQueue contains: ");
			for (Node n : priorityQueue) {
				System.out.println(n.getGasStation().getID() + "(" + n.getGasStation().getPredictedPrice() + ")");
			}
			
			
			System.out.println("\n Distance to next: " + (distance(nodes.get(i), nodes.get(i + 1))));
			
			if (windowCapacity - currentFill - (distance(nodes.get(i), nodes.get(i + 1))) < 0) {
				Node n = slidingWindow.getLast();
				slidingWindow.remove(n);
				n.setNext(priorityQueue.poll());
				System.out.println("***\n");
				System.out.print("Removed " + n.getGasStation().getID() + 
								   ", oldCapacity: " + currentFill);  
				currentFill -= distance(n, slidingWindow.get(slidingWindow.size() - 2));
				System.out.print(", newCapacity: " + currentFill + "\n");
				System.out.println("next of the removed is set to: " + n.getNext().getGasStation().getID());
			}
			
			
			if (windowCapacity - currentFill - (distance(nodes.get(i), nodes.get(i + 1))) >= 0) {
				slidingWindow.add(nodes.get(i + 1));
				priorityQueue.add(nodes.get(i + 1));
				currentFill += distance(nodes.get(i), nodes.get(i + 1));
				nodes.get(i + 1).setPrev(priorityQueue.peek());
				if (nodes.get(i + 1).getPrev().equals(nodes.get(i + 1))) {
					breakPoints.add(nodes.get(i + 1));
				}
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
						* Math.cos(n2.getGasStation().getLon() - n1.getGasStation().getLon()))) / 1000);
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
