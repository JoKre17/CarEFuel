package carefuel.path;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Graph<E> {

	private static final Logger log = LogManager.getLogger(Graph.class);

	private List<E> values;
	private List<Vertex<E>> vertices;
	private Short[][] distances;
	private int size;

	public Graph(List<E> values, Short[][] distances) throws Exception {
		if (values.size() != distances.length) {
			throw new Exception("Dimensions of vertices and distances don't get along!");
		}

		this.values = values;
		this.vertices = new ArrayList<>();
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
	
	public void setMaxRange(double maxRange) {
		Vertex.range = maxRange;
	}

	public int getSize() {
		return this.size;
	}

	public List<E> getValues() {
		return this.values;
	}
	
	public List<Vertex<E>> getVertices() {
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

		Short[] neighbourDistances = distances[values.indexOf(node.getValue())];

		for (int i = 0; i < neighbourDistances.length; i++) {
			Short distance = neighbourDistances[i];

			if (distance <= Vertex.range) {
				Edge<E> edge = new Edge<E>(node, createVertex(values.get(i)));
				edge.setDistance(distance);
				// TODO set weight of edge to the price
				edge.setWeight(1);
				neighbours.add(edge);
			}
		}

		return neighbours;
	}

	public Vertex<E> createVertex(E value) {
		Vertex<E> node = getVertexByValue(value);
		if(node == null) {
			node = new Vertex<E>(value);
			this.vertices.add(node);
		}
		return node;
	}
	
	public Vertex<E> getVertexByValue(E value) {
		Vertex<E> vertex = vertices.stream().filter(v -> v.getValue().equals(value)).findFirst().orElse(null);
		
		return vertex;
	}
	
}
