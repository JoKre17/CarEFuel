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

	private Function<Vertex<GasStation>, Number> heuristic;

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
		int amountStations = allStations.size();
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

				Short distance = (short) (GasStation.computeDistanceToGasStation(lat_a, lon_a, lat_b, lon_b));
				// Short distance = (short) (Math.random() * Short.MAX_VALUE);
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

	public <E> Function<Vertex<E>, Number> buildHeuristic(double x, Map<Vertex<E>, Double> costMap,
			Map<Vertex<E>, Double> heuristicMap, Map<Vertex<GasStation>, Vertex<GasStation>> predecessorMap) {

		return (vertex) -> {
			return vertex.getGCost() + heuristicMap.get(vertex);
		};
	}

	public List<Vertex<GasStation>> explorativeAStar(GasStation start, GasStation end, double maxRange, double x) {

		// 0 < x < 1
		x = Math.max(0.0, Math.min(x, 1.0));

		graph.setMaxRange(maxRange);

		PriorityQueue<Vertex<GasStation>> open = new PriorityQueue<>(new VertexComparator<GasStation>() {
		});
		List<Vertex<GasStation>> closed = new ArrayList<>();

		// Build heuristic map without database
		Map<Vertex<GasStation>, Double> heuristicMap = new HashMap<>();
		Short[][] distances = graph.getDistances();
		List<GasStation> stations = graph.getValues();
		for (int i = 0; i < stations.size(); i++) {
			heuristicMap.put(graph.createVertex(stations.get(i)), (double) distances[i][stations.indexOf(end)]);
		}

		Map<Vertex<GasStation>, Double> costMap = new HashMap<>();
		Map<Vertex<GasStation>, Vertex<GasStation>> predecessorMap = new HashMap<>();

		heuristic = buildHeuristic(x, costMap, heuristicMap, predecessorMap);

		Vertex<GasStation> startNode = graph.createVertex(start);
		startNode.setGCost(0);
		startNode.setHCost(heuristicMap.get(startNode));

		// add start node to open list
		open.add(startNode);

		Vertex<GasStation> currentNode = null;

		while (!open.isEmpty()) {
			log.info("Iteration " + closed.size());
			log.info(open.size() + " nodes left to discover.");

			currentNode = open.poll();

			// found end node
			if (currentNode.getValue().equals(end)) {
				break;
			}

			String out = "";
			for (Vertex<GasStation> v : open) {
				out += v.getHCost() + " ";
			}
			log.info(out);

			// For each neighbour of currentNode
			// expand currentNode
			PriorityQueue<Edge<GasStation>> neighbours = graph.getNeighbours(currentNode);
			log.info("Looking at " + neighbours.size() + " neighbours.");
			for (Edge<GasStation> e : neighbours) {

				Vertex<GasStation> successor = e.getTo();

				if (closed.contains(successor)) {
					continue;
				}

				double g_tentative = currentNode.getGCost() + e.getValue(x);

				// if there is already a cheaper connection to this successor
				if (open.contains(successor) && g_tentative >= successor.getGCost()) {
					continue;
				}

				predecessorMap.put(successor, currentNode);

				successor.setGCost(g_tentative);

				double hCost = heuristic.apply(successor).doubleValue();
				successor.setHCost(hCost);

				if (!open.contains(successor)) {
					open.add(successor);
				}

			}

			closed.add(currentNode);

		}

		// traverse the predecessor map from end node to start node
		List<Vertex<GasStation>> path = new ArrayList<>();

		if (currentNode == null) {
			return path;
		}

		Vertex<GasStation> pred = predecessorMap.get(currentNode);

		// in case of start == end, pred is null already null and path List stays empty
		while (pred != null) {
			path.add(0, pred);
			pred = predecessorMap.get(pred);
		}

		double driveDistance = path.get(path.size() - 1).getGCost();
		log.info("Distance driven: " + driveDistance);
		log.info("Distance direct: " + heuristicMap.get(startNode));

		for (Vertex<GasStation> v : path) {
			log.info(v.getValue().getId() + ": GCost = " + v.getGCost() + " HCost = " + v.getHCost());
		}

		return path;
	}

}