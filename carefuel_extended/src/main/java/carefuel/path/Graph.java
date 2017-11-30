package carefuel.path;

import java.util.List;
import java.util.PriorityQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Graph<E> {

	private static final Logger log = LogManager.getLogger(Graph.class);

	private List<E> vertices;
	private Short[][] distances;
	private int size;

	public Graph(List<E> vertices, Short[][] distances) throws Exception {
		if (vertices.size() != distances.length) {
			throw new Exception("Dimensions of vertices and distances don't get along!");
		}

		this.vertices = vertices;
		this.setDistances(distances);
	}

	private void setDistances(Short[][] distances) {
		if (distances.length > 0) {
			if (distances.length != distances[0].length) {
				log.error("Dimension mismatch in distances: Shape (" + distances.length + "," + distances[0].length
						+ ")");
			}
		}
		this.distances = distances;
		this.size = distances.length;

	}

	private void setSize(int size) {
		this.size = size;
		distances = new Short[size][size];
	}

	public int getSize() {
		return this.size;
	}

	public List<E> getVertices() {
		return this.vertices;
	}

	public Short[][] getDistances() {
		return distances;
	}

	public PriorityQueue<Edge<E>> getNeighbours(Vertex<E> node) {

		PriorityQueue<Edge<E>> neighbours = node.getNeighbours();
		if (!neighbours.isEmpty()) {
			return neighbours;
		}

		Short[] neighbourDistances = distances[vertices.indexOf(node.getValue())];

		for (int i = 0; i < neighbourDistances.length; i++) {
			Short distance = neighbourDistances[i];

			if (distance <= Vertex.range) {
				Edge<E> edge = new Edge<E>(node, createVertex(vertices.get(i)));
				edge.setDistance(distance);
				// TODO set weight of edge to the price
				edge.setWeight(1);
				neighbours.add(edge);
			}
		}

		return neighbours;
	}

	public void setMaxRange(double maxRange) {
		Vertex.range = maxRange;
	}

	public Vertex<E> createVertex(E value) {
		return new Vertex<E>(value);
	}

}
