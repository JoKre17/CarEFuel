package carefuel.path;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.DatabaseHandler;
import carefuel.controller.PricePredictor;
import carefuel.model.GasStation;

/**
 * PathFinder uses Database Handler to build a graph and if possible loads the
 * distances from a file This class is used to find the path from one GasStation
 * to another with getting the best path according to the heuristic
 * 
 * @author josef
 *
 */
public class PathFinder {

	private static final Logger log = LogManager.getLogger(PathFinder.class);

	private DatabaseHandler dbHandler;
	private PricePredictor pricePredictor;
	private Graph<GasStation> graph;

	private Function<Vertex<GasStation>, Number> heuristic;

	public PathFinder(DatabaseHandler dbHandler, PricePredictor pricePredictor) {

		this.dbHandler = dbHandler;
		this.pricePredictor = pricePredictor;

		// load the graph in background
		new Thread() {
			@Override
			public void run() {
				double startTime = System.currentTimeMillis();
				loadGraph();
				log.info("Loading graph completed in " + (System.currentTimeMillis() - startTime) / 1000.0
						+ " seconds.");
				// try to lose allocated RAM (3-4 GB)
				System.gc();
				System.runFinalization();
			}
		}.run();

	}

	/**
	 * loads the graph necessary for the pathfinding algorithmus
	 */
	private void loadGraph() {
		this.graph = loadGraphFromDatabase();
	}

	/**
	 * Loads the graph from database with fetching all GasStations
	 */
	private Graph<GasStation> loadGraphFromDatabase() {
		log.info("Loading Graph from Database");

		log.info("Fetching all stations from database.");
		// transform gasstation positions into 2-dimensional shape(n,2) array.
		List<GasStation> allStations = new LinkedList<GasStation>(dbHandler.getAllGasStations());
		int amountStations = allStations.size();
		double[][] graphMap = new double[amountStations][2];
		for (int i = 0; i < allStations.size(); i++) {
			graphMap[i] = new double[] { allStations.get(i).getLatitude(), allStations.get(i).getLongitude() };
		}
		log.info("Fetched " + allStations.size() + " stations.");

		float[][] distances = new float[amountStations][amountStations];

		log.info("Building all vertices and edges for each station.");
		double buildStartTime = System.currentTimeMillis();
		int perc = 0;
		// for each gasstation read lat lon
		for (int i = 0; i < graphMap.length; i++) {

			double lat_a = graphMap[i][0];
			double lon_a = graphMap[i][1];

			// distances from vertex i to all other vertices
			float[] neighbourDistances = distances[i];

			// copy symmetrical values
			for (int j = 0; j < i + 1; j++) {
				neighbourDistances[j] = distances[j][i];
			}

			// distance to itself is 0
			neighbourDistances[i] = 0.0F;

			// distances which are not yet calculated
			for (int j = i; j < graphMap.length; j++) {

				double lat_b = graphMap[j][0];
				double lon_b = graphMap[j][1];

				neighbourDistances[j] = (float) (GasStation.computeDistanceToGasStation(lat_a, lon_a, lat_b, lon_b));

			}

			distances[i] = neighbourDistances;

			if (((int) ((double) (i) / amountStations * 100)) > perc) {
				log.info("Vertices: " + perc + " %");
				perc += 10;
			}
		}
		log.info("Vertices: " + perc + " %");

		// fetch new copy => maybe ram gets cleared ?
		Graph<GasStation> graph = null;
		try {
			graph = new Graph<GasStation>(new ArrayList<>(dbHandler.getAllGasStations()), distances);
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info("Building all vertices and edges took " + (System.currentTimeMillis() - buildStartTime) / 1000.0
				+ " seconds.");

		return graph;

	}

	/**
	 * So far fix heuristic not depending on anything else. Could be overridden
	 * later
	 * 
	 * Absolute cost so far to get to VERTEX + assumed cost to get from VERTEX to
	 * GOAL
	 * 
	 * @param heuristicMap
	 * @return
	 */
	public <E> Function<Vertex<E>, Number> buildHeuristic(Map<Vertex<E>, Double> heuristicMap) {

		return (vertex) -> {
			return vertex.getGCost() + heuristicMap.get(vertex);
		};
	}

	/**
	 * calculates the best path from start GasStation to end GasStation depending on
	 * maxRange (what GasStations are reachable from another GasStation), how fast
	 * the average travel speed is and the value x, specifying the bridge between
	 * 
	 * 0 => shortest path
	 * 
	 * 1 => cheapest path
	 * 
	 * between 0 and 1: best path?
	 * 
	 * @param start
	 * @param end
	 * @param maxRange
	 * @param averageSpeed
	 * @param x
	 * @return
	 */
	public List<Vertex<GasStation>> explorativeAStar(String startUUID, String endUUID, Date startTime, float maxRange,
			float averageSpeed, float x) {

		List<GasStation> allStations = graph.getValues();
		GasStation start = allStations.stream().filter(s -> s.getId().toString().equals(startUUID)).findFirst().get();
		GasStation end = allStations.stream().filter(s -> s.getId().toString().equals(endUUID)).findFirst().get();
		// find start and end GasStations
		
		// 0 < x < 1
		x = (float) Math.max(0.0, Math.min(x, 1.0));

		PriorityQueue<Vertex<GasStation>> open = new PriorityQueue<>(new VertexComparator<GasStation>());
		List<Vertex<GasStation>> closed = new ArrayList<>();

		// Build heuristic map without database
		Map<Vertex<GasStation>, Double> heuristicMap = new HashMap<>();
		Map<Vertex<GasStation>, Vertex<GasStation>> predecessorMap = new HashMap<>();

		// build heurstic, later fetch it from the database
		float[][] distances = graph.getDistances();
		List<GasStation> stations = graph.getValues();
		// log.info("Build heuristic values");
		for (int i = 0; i < stations.size(); i++) {
			heuristicMap.put(graph.getVertexByValue(stations.get(i)), (double) distances[i][stations.indexOf(end)]);
		}

		// use default heuristic
		heuristic = buildHeuristic(heuristicMap);

		// initiate the A Star algorithm
		Vertex<GasStation> startNode = graph.getVertexByValue(start);
		startNode.setGCost(0);
		startNode.setHCost(heuristicMap.get(startNode));
		open.add(startNode);

		Vertex<GasStation> currentNode = null;

		while (!open.isEmpty()) {
			// log.info("Iteration " + closed.size());
			// log.info(open.size() + " nodes left to discover.");

			currentNode = open.poll();

			// found end node
			if (currentNode.getValue().equals(end)) {
				break;
			}

			// For each neighbour of currentNode
			// expand currentNode
			PriorityQueue<Edge<GasStation>> neighbours = graph.getNeighbours(currentNode, maxRange);
			// log.info("Looking at " + neighbours.size() + " neighbours.");
			for (Edge<GasStation> e : neighbours) {

				Vertex<GasStation> successor = e.getTo();

				// if already visited
				if (closed.contains(successor)) {
					continue;
				}

//				pricePredictor.predictPrice(maxDateString, predictionDateString, gasStationID)
				
				// cost so far for path start -> successor (depending on x)
				double g_tentative = currentNode.getGCost() + e.getValue(x);

				// if there is already a cheaper connection to this successor found
				if (open.contains(successor) && g_tentative >= successor.getGCost()) {
					continue;
				}

				predecessorMap.put(successor, currentNode);

				// set absolute cost for path start -> successor
				successor.setGCost(g_tentative);

				double hCost = heuristic.apply(successor).doubleValue();

				// heuristic cost used for priority queue
				successor.setHCost(hCost);

				if (!open.contains(successor)) {
					open.add(successor);
				}

			}

			closed.add(currentNode);

		}

		log.info("Needed " + closed.size() + " iterations.");

		// traverse the predecessor map from end node to start node
		List<Vertex<GasStation>> path = new ArrayList<>();

		// in case of start == end, pred is already null and path List stays empty
		if (currentNode == null) {
			return path;
		}

		// log.info("Getting path");
		// add the end node to the path
		path.add(currentNode);
		Vertex<GasStation> pred = predecessorMap.get(currentNode);

		while (pred != null && !pred.getValue().equals(start)) {
			path.add(0, pred);
			pred = predecessorMap.get(pred);
		}
		if (pred.getValue().equals(start)) {
			path.add(0, pred);
		}

		// double driveDistance = path.get(path.size() - 1).getGCost();
		// log.info("Distance driven: " + driveDistance);
		// log.info("Distance direct: " + heuristicMap.get(startNode));

		return path;
	}

}