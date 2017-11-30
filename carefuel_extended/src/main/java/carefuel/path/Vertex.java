package carefuel.path;

import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Vertex<E> implements Comparable<Vertex<E>> {

	private static final Logger log = LogManager.getLogger(Vertex.class);

	Graph<E> graph;
	private E value;
	private Double cost;

	// This is the maximal range where connections will be considered
	protected static double range;

	private PriorityQueue<Edge<E>> neighbours;

	/**
	 * Constructor also fetches neighbours depending on static range
	 * 
	 * @param value
	 */
	public Vertex(E value) {
		this.value = value;

		neighbours = new PriorityQueue<>(new EdgeComparator<E>() {
		});
	}

	public E getValue() {
		return this.value;
	}

	public void addNeighbours(List<Edge<E>> connections) {
		this.neighbours.addAll(connections);
	}

	public void addNeighbour(Edge<E> connection) {
		this.neighbours.add(connection);
	}

	public void clearNeighbours() {
		this.neighbours.clear();
	}

	public PriorityQueue<Edge<E>> getNeighbours() {
		return new PriorityQueue<Edge<E>>(
				neighbours.stream().filter(e -> e.getDistance() <= range).collect(Collectors.toList()));
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public Double getCost() {
		return this.cost;
	}

	@Override
	public int compareTo(Vertex<E> other) {
		return cost.compareTo(other.getCost());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Vertex)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Vertex other = (Vertex) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
