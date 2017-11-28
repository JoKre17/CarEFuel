package carefuel.path;

public class Vertex<E> {

	Graph graph;
	private E value;
	
	// This is the maximal range where connections will be considered
	protected static double range;
	
//	private PriorityQueue<Edge<E>> neighbours;
	
	
	public Vertex(E value) {
		this.value = value;
		
//		neighbours = new PriorityQueue<>();
	}
	
	public void setValue(E value) {
		this.value = value;
	}
	
	public E getValue() {
		return this.value;
	}
	
//	public void addNeighbours(List<Edge<E>> connections) {
//		this.neighbours.addAll(connections);
//	}
//	
//	public void addNeighbour(Edge<E> connection) {
//		this.neighbours.add(connection);
//	}
//	
//	public void clearNeighbours() {
//		this.neighbours.clear();
//	}
	
//	public List<Edge<E>> getNeighbours() {
//		return neighbours.stream().filter(e -> e.getDistance() <= range).collect(Collectors.toList());
//	}
	
	
	
}
