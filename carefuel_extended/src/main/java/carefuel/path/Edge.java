package carefuel.path;

public class Edge<E> implements Comparable<E>{
	
	private E from;
	private E to;
	
	private double distance;
	private double weight;
	
	public Edge(E from, E to) {
		this.from = from;
		this.to = to;
		
		weight = 1;
	}
	
	public E getFrom() {
		return from;
	}
	
	public E getTo() {
		return to;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public double getDistance() {
		return this.distance;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public double getWeight() {
		return this.weight;
	}
	
	public double getValue() {
		return this.distance * this.weight;
	}

	@Override
	public int compareTo(Object arg0) {
		if(arg0 instanceof Edge) {
			@SuppressWarnings("unchecked")
			Edge<E> other = (Edge<E>) arg0;
			
			if(this.getValue() < other.getValue()) {
				return 1;
			} else {
				return -1;
			}
		}
		
		return 0;
	}
	
	

}
