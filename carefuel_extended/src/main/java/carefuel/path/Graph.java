package carefuel.path;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Internal representation of a graph containing vertices and edges This graph
 * stores all values it's corresponding vertices with the distances between the
 * vertices
 * 
 * @author josef
 *
 * @param <E>
 */
public class Graph<E> {

	private static final Logger log = LogManager.getLogger(Graph.class);

	private List<E> values;
	private List<Vertex<E>> vertices;
	private Short[][] distances;
	private int size;

	/**
	 * Constructor takes a list of all values and a completed distance matrix
	 * 
	 * @param values
	 * @param distances
	 * @throws Exception
	 */
	public Graph(List<E> values, Short[][] distances) throws Exception {
		if (values.size() != distances.length) {
			throw new Exception("Dimensions of vertices and distances don't get along!");
		}

		this.values = values;
		this.vertices = new ArrayList<>();
		this.setDistances(distances);
	}

	/**
	 * 
	 * @param distances
	 */
	private void setDistances(Short[][] distances) {
		if (distances.length > 0 && distances.length == size) {
			if (distances.length != distances[0].length) {
				log.error("Dimension mismatch in distances: Shape (" + distances.length + "," + distances[0].length
						+ ")");
			}
		}
		this.distances = distances;

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

	/**
	 * Returns the neighbours of a vertex depending on the range given by the user
	 * 
	 * @param node
	 * @param maxRange
	 * @return
	 */
	public PriorityQueue<Edge<E>> getNeighbours(Vertex<E> node, short maxRange) {

		PriorityQueue<Edge<E>> neighbours = node.getNeighbours();
		// Try to use already computed neighbours
		if (!neighbours.isEmpty()) {
			// if max distance of already calculated distances is bigger than maxRange, we
			// can reuse it
			if (neighbours.stream().map(e -> e.getDistance()).max(Double::compareTo).get() > maxRange) {
				PriorityQueue<Edge<E>> p = new PriorityQueue<>(new EdgeComparator<>());
				p.addAll(neighbours.stream().filter(e -> e.getDistance() <= maxRange).collect(Collectors.toList()));
				return p;
			}
		}

		// distances to all neighbours
		Short[] neighbourDistances = distances[values.indexOf(node.getValue())];

		// for all vertices...
		for (int i = 0; i < neighbourDistances.length; i++) {
			Short distance = neighbourDistances[i];

			// only get those in range and not itself
			if (distance <= maxRange && distance != 0) {
				Edge<E> edge = new Edge<E>(node, getVertexByValue(values.get(i)));
				edge.setDistance(distance);

				// neighbours was already computed but maxRange was smaller back then, so the
				// edge is not in neighbours
				if (!neighbours.contains(edge)) {
					neighbours.add(edge);
				}
			}
		}

		return neighbours;
	}

	/**
	 * Creates a Vertex by value only if there is no vertex containing the same
	 * value
	 * 
	 * @param value
	 * @return
	 */
	public Vertex<E> getVertexByValue(E value) {
		Vertex<E> vertex = vertices.stream().filter(v -> v.getValue().equals(value)).findFirst().orElse(null);
		if (vertex == null) {
			vertex = new Vertex<E>(value);
			this.vertices.add(vertex);
		}
		return vertex;
	}

}
