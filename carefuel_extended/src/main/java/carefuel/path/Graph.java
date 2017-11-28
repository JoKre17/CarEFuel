package carefuel.path;

import java.util.LinkedList;
import java.util.List;

public class Graph<E> {
	
	List<Vertex<E>> vertices;
	
	public Graph() {
		vertices = new LinkedList<>();
	}
	
	public void setVertices(List<Vertex<E>> vertices) {
		this.vertices = vertices;
	}
	
	public void addVertices(List<Vertex<E>> vertices) {
		this.vertices.addAll(vertices);
	}

	public void addVertex(Vertex<E> vertex) {
		this.vertices.add(vertex);
	}
	
	public List<Vertex<E>> getVertices() {
		return this.vertices;
	}

}
