package carefuel.path;

public class Edge<E> implements Comparable<Edge<E>> {

	private Vertex<E> from;
	private Vertex<E> to;

	private Double distance;
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

	public double getValue(double x) {
		return this.distance * (1.0 + (this.weight - 1.0) * x);
	}

	@Override
	public String toString() {
		return from + " => " + to;
	}

	@Override
	public int compareTo(Edge<E> other) {
		return distance.compareTo(other.getDistance());
	}

}
