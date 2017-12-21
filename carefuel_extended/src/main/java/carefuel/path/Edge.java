package carefuel.path;

/**
 * Generic Edge class defining the connection between two Vertices
 * 
 * @author josef
 *
 * @param <E>
 */
public class Edge<E> implements Comparable<Edge<E>> {

	private Vertex<E> from;
	private Vertex<E> to;

	// distance is the real edge value
	private Double distance;
	// weight is the multiplier for the distance
	private double weight;

	public Edge(Vertex<E> from, Vertex<E> to) {
		this.from = from;
		this.to = to;

		weight = 1;
	}

	public Vertex<E> getFrom() {
		return from;
	}

	public Vertex<E> getTo() {
		return to;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public Double getDistance() {
		return this.distance;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return this.weight;
	}

	/**
	 * returns the cost of this edge depending on x x should be between 0 and 1, so
	 * that in case of 0 the function returns only the distance and in case of 1 it
	 * applies the full weight on the edge
	 * 
	 * @param x
	 * @return
	 */
	public double getValue(double x) {
		return this.distance * (1.0 + (this.weight - 1.0) * x);
	}

	@Override
	public String toString() {
		return from + " => " + to;
	}

	/**
	 * Used by EdgeComparator
	 */
	@Override
	public int compareTo(Edge<E> other) {
		return distance.compareTo(other.getDistance());
	}

}
