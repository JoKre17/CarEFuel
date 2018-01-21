package carefuel.path;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.DatabaseHandler;
import carefuel.controller.Fuel;
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

	private Graph<GasStation> graph;

	private Function<Vertex<GasStation>, Number> heuristic;

	public PathFinder(DatabaseHandler dbHandler) {

		this.dbHandler = dbHandler;

	}

	/**
	 * Setup the PathFinder. Basically only loading the graph
	 */
	public void setup() {
		// load the graph in background
		double startTime = System.currentTimeMillis();
		loadGraph();
		log.info("Loading graph completed in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds.");
		System.gc();
		System.runFinalization();
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
		// transform gasstation positions into 2-dimensional shape(n,2) array
		// containing
		// Lon Lat of each gas_station
		List<GasStation> allStations = new LinkedList<GasStation>(dbHandler.getAllGasStations());
		int amountStations = allStations.size();
		double[][] graphMap = new double[amountStations][2];
		for (int i = 0; i < allStations.size(); i++) {
			graphMap[i] = new double[] { allStations.get(i).getLatitude(), allStations.get(i).getLongitude() };
		}
		log.info("Fetched " + allStations.size() + " stations.");

		// contains all distances from each gasStation to every other gasStation
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
			// needs more RAM but makes faster recalls of neighbours
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

			// prints the actual progress every 10%
			if (((int) ((double) (i) / amountStations * 100)) > perc) {
				log.info("Vertices: " + perc + " %");
				perc += 10;
			}
		}
		log.info("Vertices: " + perc + " %");

		// fetch new copy => maybe ram gets cleared ?
		Graph<GasStation> graph = null;
		try {
			graph = new Graph<GasStation>(allStations, distances);
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
	 * Contains default A* heuristic: Absolute cost to get to VERTEX + assumed
	 * cost to get from VERTEX to GOAL
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
	 * Explorative A* Algorithm to solve the Gas Station Problem ("extended")
	 *
	 * The algorithmus is capable of using the predicted prices for the specific
	 * arrival time at each gas station including the selection of one of the
	 * three Fuel types (Diesel, E5, E10)
	 *
	 * Calculates the best path from start GasStation to end GasStation
	 * depending on maxRange (what GasStations are reachable from another
	 * GasStation), how fast the average travel speed is and the value x,
	 * specifying the bridge between the shortest and the cheapest path.
	 *
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
	 * @throws Exception
	 */
	public List<Vertex<GasStation>> explorativeAStar(String startUUID, String endUUID, Date startTime, int tankLevel,
			Fuel gasType, float maxRange, float averageSpeed, float x) throws Exception {

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
		Map<Vertex<GasStation>, Date> arriveTimes = new HashMap<>();
		Map<Vertex<GasStation>, Vertex<GasStation>> predecessorMap = new HashMap<>();

		// build heurstic, later fetch it from the database
		float[][] distances = graph.getDistances();
		List<GasStation> stations = graph.getValues();
		// log.info("Build heuristic values");
		double curTime = System.currentTimeMillis();

		List<Pair<Date, Integer>> predictions = dbHandler.getPricePrediction(end.getId(), gasType);
		for (int i = 0; i < stations.size(); i++) {
			// x > 0 means the fuel prices weight into the edges of the graph
			// therefore it is necessary to give the heuristic also weighted
			// values for each
			// station
			if (x > 0) {
				long arrivalTimeLong = new Date(
						(long) (startTime.getTime() + distances[i][stations.indexOf(end)] / averageSpeed)).getTime();

				// Diesel : 1109 means 1.109 euro. Therefore 1109 is given in
				// "centicent"
				int pricePredictionInCentiCent = Collections.min(predictions, new Comparator<Pair<Date, Integer>>() {
					@Override
					public int compare(Pair<Date, Integer> d1, Pair<Date, Integer> d2) {
						long diff1 = Math.abs(d1.getLeft().getTime() - arrivalTimeLong);
						long diff2 = Math.abs(d2.getLeft().getTime() - arrivalTimeLong);
						return Long.compare(diff1, diff2);
					}
				}).getRight();

				double pricePredictionEuro = pricePredictionInCentiCent / 1000.0;

				double hValue = distances[i][stations.indexOf(end)] * (1.0 + (pricePredictionEuro - 1.0) * x);
				heuristicMap.put(graph.getVertexByValue(stations.get(i)), hValue);
			} else {
				heuristicMap.put(graph.getVertexByValue(stations.get(i)), (double) distances[i][stations.indexOf(end)]);
			}
		}
		log.info("Built heuristic in " + ((System.currentTimeMillis() - curTime) / 1000.0) + " s");

		// use default heuristic
		heuristic = buildHeuristic(heuristicMap);

		// initiate the A Star algorithm
		Vertex<GasStation> startNode = graph.getVertexByValue(start);
		startNode.setGCost(0);
		startNode.setHCost(heuristicMap.get(startNode));
		open.add(startNode);
		arriveTimes.put(startNode, startTime);

		Vertex<GasStation> currentNode = null;

		Calendar calendar = Calendar.getInstance();

		while (!open.isEmpty()) {
			log.debug("Iteration " + closed.size());
			// log.info(open.size() + " nodes left to discover.");

			currentNode = open.poll();

			double distanceToDest = GasStation.computeDistanceToGasStation(currentNode.getValue().getLatitude(),
					currentNode.getValue().getLongitude(), end.getLatitude(), end.getLongitude());
			log.debug("Distance to destination: " + distanceToDest);

			// found end node
			if (currentNode.getValue().equals(end)) {
				break;
			}

			// For each neighbour of currentNode
			// expand currentNode
			PriorityQueue<Edge<GasStation>> neighbours = graph.getNeighbours(currentNode, maxRange, gasType);
			// log.debug("Looking at " + neighbours.size() + " neighbours.");
			for (Edge<GasStation> e : neighbours) {

				Vertex<GasStation> successor = e.getTo();

				// if already visited
				if (closed.contains(successor)) {
					continue;
				}

				// calculate arrival time at successor
				Date currentTime = arriveTimes.get(currentNode);
				calendar.setTime(currentTime);
				int timeInMins = (int) ((e.getDistance() / averageSpeed) * 60.0);
				calendar.add(Calendar.MINUTE, timeInMins);
				Date arrivalTime = calendar.getTime();
				arriveTimes.put(successor, arrivalTime);

				double pricePredictionInEuro = 0;
				// predict price
				if (x > 0) {
					// get predicted prices for gasStation
					double pricePredictionInCentiCent = dbHandler
							.getPricePredictionClosestToDate(successor.getValue().getId(), gasType, arrivalTime)
							.getRight();

					// Diesel : 1109 means 110.9 cent. Therefore 1109 is given
					// in "centicent"
					pricePredictionInEuro = pricePredictionInCentiCent / 1000.0;

				}
				e.setWeight(pricePredictionInEuro);

				// cost so far for path start -> successor (depending on x)
				double g_tentative = currentNode.getGCost() + e.getValue(x);

				// if there is already a cheaper connection to this successor
				// found
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

		log.debug("Needed " + closed.size() + " iterations.");

		// traverse the predecessor map from end node to start node
		List<Vertex<GasStation>> path = new ArrayList<>();

		// in case of start == end, pred is already null and path List stays
		// empty
		if (currentNode == null) {
			log.debug("currentNode is null");
			return path;
		}

		// log.debug("Getting path");
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