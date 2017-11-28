package carefuel.path;

import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class Vertex<E> {

	private E value;
	
	// This is the maximal range where connections will be considered
	private static double range;
	
	private PriorityQueue<Edge<E>> connections;
	
	
	public Vertex(E value) {
		this.value = value;
		
		connections = new PriorityQueue<>();
	}
	
	public void setValue(E value) {
		this.value = value;
	}
	
	public E getValue() {
		return this.value;
	}
	
	public void addConnections(List<Edge<E>> connections) {
		this.connections.addAll(connections);
	}
	
	public void addConnection(Edge<E> connection) {
		this.connections.add(connection);
	}
	
	public void clearConnections() {
		this.connections.clear();
	}
	
	public List<Edge<E>> getConnections() {
		return connections.stream().filter(e -> e.getDistance() <= range).collect(Collectors.toList());
	}
	
	
	
}
