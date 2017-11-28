package carefuel.path;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Graph<E> {
	
	private List<E> vertices;
	private double[][] distances;
	
	public Graph(int size) {
		vertices = new LinkedList<>();
		
		distances = new double[size][size];
	}
	
	public void setDistances(double[][] distances) {
		this.distances = distances;
	}
	
	public void setVertices(List<E> vertices) {
		this.vertices = vertices;
	}
	
	public void addVertices(List<E> vertices) {
		this.vertices.addAll(vertices);
	}

	public void addVertex(E vertex) {
		this.vertices.add(vertex);
	}
	
	public List<E> getVertices() {
		return this.vertices;
	}
	
	public double[][] getDistances() {
		return distances;
	}
	
	public List<Edge<E>> getNeighbours(E vertex) {
		double[] neighbourDistances = distances[vertices.indexOf(vertex)];
		
		List<Edge<E>> neighbours = new ArrayList<>();
		
		for(int i = 0; i < neighbourDistances.length; i++) {
			double distance = neighbourDistances[i];
			
			if(distance <= Vertex.range) {
				Edge<E> edge = new Edge<E>(vertex, vertices.get(i));
				edge.setDistance(distance);
				neighbours.add(edge);
			}
		}
		
		return neighbours;
	}
	
	public void setMaxRange(double maxRange) {
		Vertex.range = maxRange;
	}
	
}
