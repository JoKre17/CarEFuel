package carefuel.path;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Graph<E> {

	private static final Logger log = LogManager.getLogger(Graph.class);

	private List<E> vertices;
	private Short[][] distances;
	private int size;

	public Graph() {
		vertices = new LinkedList<>();

	}

	public void setSize(int size) {
		this.size = size;
		distances = new Short[size][size];
	}

	public int getSize() {
		return this.size;
	}

	public void setDistances(Short[][] distances) {
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

	public Short[][] getDistances() {
		return distances;
	}

	public List<Edge<E>> getNeighbours(E vertex) {
		log.info("Vertex #: " + vertices.indexOf(vertex));
		Short[] neighbourDistances = distances[vertices.indexOf(vertex)];

		List<Edge<E>> neighbours = new ArrayList<>();

		for (int i = 0; i < neighbourDistances.length; i++) {
			Short distance = neighbourDistances[i];

			if (distance <= Vertex.range) {
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
