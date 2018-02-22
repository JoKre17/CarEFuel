package carefuel.path;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Generic Vertex class for the Graph class
 * 
 * @author josef
 *
 * @param <E>
 */
public class Vertex<E> {

	// private static final Logger log = LogManager.getLogger(Vertex.class);

	private E value;

	// these values need to be outsourced if multiple paths should be calculated
	// cost from specific start vertex to this vertex
//	private Double gCost;
	// estimated cost from this vertex to end vertex
//	private Double hCost;

	private PriorityBlockingQueue<Edge<E>> neighbours;

	/**
	 * Constructor also fetches neighbours depending on static range
	 * 
	 * @param value
	 */
	public Vertex(E value) {
		this.value = value;

		neighbours = new PriorityBlockingQueue<>(500, new EdgeComparator<E>());
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

	public PriorityBlockingQueue<Edge<E>> getNeighbours() {
		return neighbours;
	}

//	public void setHCost(double hCost) {
//		this.hCost = hCost;
//	}
//
//	public Double getHCost() {
//		return this.hCost;
//	}
//
//	public void setGCost(double gCost) {
//		this.gCost = gCost;
//	}
//
//	public Double getGCost() {
//		return this.gCost;
//	}
//
//	@Override
//	public String toString() {
//		return gCost.toString();
//	}

//	/**
//	 * Used by VertexComparator (PriorityQueue in PathFinder)
//	 */
//	@Override
//	public int compareTo(Vertex<E> other) {
//		return hCost.compareTo(other.getHCost());
//	}

	/**
	 * Used for HashMap to find equal elements
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * Also used for HashMap to find equal elements (.equals is only called if 2
	 * objects have the same hashCode)
	 */
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
