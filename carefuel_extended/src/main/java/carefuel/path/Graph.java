package carefuel.path;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.Fuel;

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
	private float[][] distances;
	private int size;
	
	private ExecutorService es;
	private int N_THREADS;

	/**
	 * Constructor takes a list of all values and a completed distance matrix
	 *
	 * @param values
	 * @param distances
	 * @throws Exception
	 */
	public Graph(List<E> values, float[][] distances) throws Exception {
		if (values.size() != distances.length) {
			throw new Exception("Dimensions of vertices and distances don't get along!");
		}

		this.values = values;
		this.vertices = new ArrayList<>();
		this.setDistances(distances);
	}
	
	public void setExecutorService(ExecutorService es) {
		this.es = es;
		N_THREADS = Runtime.getRuntime().availableProcessors() * 4;
	}

	/**
	 *
	 * @param distances
	 */
	private void setDistances(float[][] distances) {
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

	public float[][] getDistances() {
		return distances;
	}
	
	private <E> PriorityBlockingQueue<E> clone(PriorityBlockingQueue<E> priorityQueue) {
		PriorityBlockingQueue<E> answer = new PriorityBlockingQueue<>();
		
		for(E e : priorityQueue) {
			answer.add(e);
		}
		
	    return answer;
	}

	/**
	 * Returns the neighbours of a vertex depending on the range given by the
	 * user
	 *
	 * @param node
	 * @param maxRange
	 * @param fuel
	 *            used to weight the edge distance with default fuel price
	 * @return
	 */
	synchronized public List<Edge<E>> getNeighbours(Vertex<E> node, float maxRange, Fuel fuel) {

		PriorityBlockingQueue<Edge<E>> neighbours = node.getNeighbours();
		// Try to use already computed neighbours
		if (!neighbours.isEmpty()) {
			// if max distance of already calculated distances is bigger than
			// maxRange, we
			// can reuse it
			if (neighbours.stream().map(e -> e.getDistance()).max(Double::compareTo).get() >= maxRange) {
				PriorityBlockingQueue<Edge<E>> p = new PriorityBlockingQueue<>(neighbours.size(), new EdgeComparator<>());
				p.addAll(neighbours.stream().filter(e -> e.getDistance() <= maxRange).collect(Collectors.toList()));
				return p.stream().collect(Collectors.toList());
			}
		}
		
		// distances to all neighbours
		float[] neighbourDistances = distances[values.indexOf(node.getValue())];

		// for all vertices...
		final int AMOUNT_DISTANCES = neighbourDistances.length;

		final int[] INDEX_SPLITTINGS = IntStream.rangeClosed(0, N_THREADS)
				.mapToDouble(i -> (i / (double) N_THREADS) * AMOUNT_DISTANCES).mapToInt(d -> (int) d).toArray();
		CompletableFuture<?>[] completables = new CompletableFuture<?>[N_THREADS];
		
		for(int i = 0; i < (INDEX_SPLITTINGS.length - 1); i++) {
			final int index = i;
			completables[index] = CompletableFuture.runAsync(() -> {
				IntStream.range(INDEX_SPLITTINGS[index], INDEX_SPLITTINGS[index + 1]).forEach(j -> {
					Float distance = neighbourDistances[j];

					// only get those in range and not itself
					if (distance <= maxRange) {
						Vertex<E> to = getVertexByValue(values.get(j));
						if (node.equals(to)) {
							return;
						}
						Edge<E> edge = new Edge<E>(node, to);
						edge.setDistance(distance);

						// neighbours was already computed but maxRange was smaller back
						// then, so the
						// edge is not in neighbours
						if (!neighbours.contains(edge)) {
							neighbours.add(edge);
						}
					}
				});
			}, es);
		}
		
		for(CompletableFuture<?> f : completables) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		return clone(neighbours).stream().collect(Collectors.toList());
	}

	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	/**
	 * Creates a Vertex by value only if there is no vertex containing the same
	 * value
	 *
	 * @param value
	 * @return
	 */
	public Vertex<E> getVertexByValue(E value) {
		lock.readLock().lock();
		Vertex<E> vertex = vertices.stream().filter(v -> v.getValue().equals(value)).findFirst().orElse(null);
		lock.readLock().unlock();
		if (vertex == null) {
			vertex = new Vertex<E>(value);
			lock.writeLock().lock();
			this.vertices.add(vertex);
			lock.writeLock().unlock();
		}
		return vertex;
	}

}
