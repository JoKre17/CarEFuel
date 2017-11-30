package carefuel.path;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.DatabaseHandler;
import carefuel.model.GasStation;

public class PathFinder {

	private static final Logger log = LogManager.getLogger(PathFinder.class);

	private DatabaseHandler dbHandler;
	private Graph<GasStation> graph;

	private Function<Edge<GasStation>, Number> heuristic;

	private boolean isLoadableFromFile = false;
	private File pathFinderFile = new File(".");

	public PathFinder(DatabaseHandler dbHandler) {

		this.dbHandler = dbHandler;

		double startTime = System.currentTimeMillis();
		loadGraph();
		log.info("Loading graph completed in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");

	}

	private void loadGraph() {
		if (isLoadableFromFile) {
			this.graph = loadGraphFromFile(pathFinderFile);
		} else {
			this.graph = loadGraphFromDatabase();
		}
	}

	/**
	 * Loads graph from Database
	 * 
	 * @param f
	 * @return
	 */
	private Graph<GasStation> loadGraphFromFile(File f) {
		// TODO
		return null;
	}

	/**
	 * Loads the graph from database with fetching all GasStations
	 */
	private Graph<GasStation> loadGraphFromDatabase() {
		log.info("Loading Graph from database");

		log.info("Fetching all stations from database.");
		double fetchStartTime = System.currentTimeMillis();
		List<GasStation> allStations = new LinkedList<GasStation>(dbHandler.getAllGasStations());
		// TODO remove line below
		// allStations = allStations.subList(0, 100);
		// Map<UUID, Pair<Double, Double>> graphMap = new HashMap<>();
		int amountStations = allStations.size();
		log.warn("Using sublist: Size: " + allStations.size());
		double[][] graphMap = new double[amountStations][2];
		for (int i = 0; i < allStations.size(); i++) {
			graphMap[i] = new double[] { allStations.get(i).getLatitude(), allStations.get(i).getLongitude() };
		}

		log.info("Fetched " + allStations.size() + " stations.");
		log.info("Fetching all stations took " + (System.currentTimeMillis() - fetchStartTime) / 1000.0 + " seconds.");

		Short[][] distances = new Short[amountStations][amountStations];

		log.info("Building all vertices and edges for each station.");
		double buildStartTime = System.currentTimeMillis();

		int perc = 0;
		for (int i = 0; i < graphMap.length; i++) {

			double lat_a = graphMap[i][0];
			double lon_a = graphMap[i][1];

			Short[] neighbourDistances = new Short[amountStations];

			// copy symmetrical values
			for (int j = 0; j < i + 1; j++) {
				neighbourDistances[j] = distances[j][i];
			}

			// distance to itself is 0
			neighbourDistances[i] = 0;

			for (int j = i; j < graphMap.length; j++) {

				double lat_b = graphMap[j][0];
				double lon_b = graphMap[j][1];

				// Short distance = (short) (GasStation.computeDistanceToGasStation(lat_a,
				// lon_a, lat_b, lon_b));
				Short distance = (short) (Math.random() * 45000.0);
				neighbourDistances[j] = distance;

			}

			distances[i] = neighbourDistances;

			if (((int) ((double) (i) / amountStations * 100)) > perc) {
				log.info("Vertices: " + perc + " %");
				perc += 10;
			}

		}

		// fetch new copy => maybe ram gets cleared
		Graph<GasStation> graph = null;
		try {
			graph = new Graph<GasStation>(new LinkedList<GasStation>(allStations), distances);
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info("Building all vertices and edges took " + (System.currentTimeMillis() - buildStartTime) / 1000.0
				+ " seconds.");

		return graph;

	}

	public <E> Function<Edge<E>, Number> buildHeuristic(double x, Map<Vertex<E>, Double> costMap,
			Map<Vertex<E>, Double> heuristicMap) {

		return (edge) -> {
			if (costMap.containsKey(edge.getTo())) {
				double c = costMap.get(edge.getTo());
				double h = heuristicMap.get(edge.getTo());
				log.info("Cost: " + c + " Heur.: " + h);
				return c + h;
			} else {
				double cost = (costMap.get(edge.getFrom()) + edge.getValue(x));
				costMap.put(edge.getTo(), cost);
				return cost + heuristicMap.get(edge.getTo());
			}
		};
	}

	public List<Edge<GasStation>> explorativeAStar(GasStation start, GasStation end, double maxRange, double x) {

		// 0 < x < 1
		x = Math.max(0.0, Math.min(x, 1.0));

		graph.setMaxRange(maxRange);

		PriorityQueue<Vertex<GasStation>> next = new PriorityQueue<>(new VertexComparator<GasStation>() {
		});
		List<Vertex<GasStation>> closed = new ArrayList<>();

		// Build heuristic map without database
		Map<Vertex<GasStation>, Double> heuristicMap = new HashMap<>();
		Short[][] distances = graph.getDistances();
		List<GasStation> stations = graph.getVertices();
		for (int i = 0; i < stations.size(); i++) {
			heuristicMap.put(graph.createVertex(stations.get(i)), (double) distances[i][stations.indexOf(end)]);
		}

		Map<Vertex<GasStation>, Double> costMap = new HashMap<>();
		Map<Vertex<GasStation>, Vertex<GasStation>> predecessorMap = new HashMap<>();

		heuristic = buildHeuristic(x, costMap, heuristicMap);

		Vertex<GasStation> startNode = graph.createVertex(start);
		next.add(startNode);
		costMap.put(startNode, 0.0);

		while (!next.isEmpty()) {
			log.info("Iteration");
			log.info("Nodes visited: " + closed.size());
			log.info("End visited? " + predecessorMap.containsKey(graph.createVertex(end)));

			Vertex<GasStation> currentNode = next.poll();

			log.info("Näherungsheuristik: " + heuristic.apply(graph.getNeighbours(currentNode).peek()));

			if (currentNode.getValue().equals(end)) {
				break;
			}

			// For each neighbour of currentNode
			log.info("Looking at " + graph.getNeighbours(currentNode).size() + " neighbours");
			for (Edge<GasStation> e : graph.getNeighbours(currentNode)) {

				if (closed.contains(e.getTo())) {
					continue;
				}

				double e_tentative = costMap.get(e.getFrom()) + e.getValue(x);

				if (next.contains(e.getTo()) && e_tentative >= costMap.get(e.getTo())) {
					continue;
				}

				predecessorMap.put(e.getTo(), e.getFrom());

				costMap.put(e.getTo(), e_tentative);

				double e_heuristic = (double) heuristic.apply(e);
				e.getTo().setCost(e_heuristic);

				if (!next.contains(e.getTo())) {
					next.add(e.getTo());
				}

			}

			closed.add(currentNode);

		}

		// traverse the predecessor map from end node to start node
		List<Edge<GasStation>> path = new ArrayList<>();
		Vertex<GasStation> actualNode = graph.createVertex(end);

		Vertex<GasStation> pred = predecessorMap.get(actualNode);

		// in case of start == end, pred is null already null and path List stays empty
		while (pred != null) {

			for (Edge<GasStation> e : pred.getNeighbours()) {
				if (e.getTo().equals(actualNode)) {
					path.add(0, e);
					break;
				}
			}
			actualNode = pred;
			pred = predecessorMap.get(actualNode);
		}

		return path;
	}

}