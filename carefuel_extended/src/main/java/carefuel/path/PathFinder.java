package carefuel.path;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.DatabaseHandler;
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

	private boolean loadFromFileIsEnabled = false;
	private File distancesFile = new File("distances.csv");

	public PathFinder(DatabaseHandler dbHandler) {

		this.dbHandler = dbHandler;

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
		// if the distances were once already computed, don't compute them again but
		// load them from a file

		if (distancesFile.exists() && loadFromFileIsEnabled) {
			try {
				this.graph = loadGraphFromFile(distancesFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// if the graph is still not loaded due to an error or if it is not loadable
		// from file
		if (this.graph == null) {
			this.graph = loadGraphFromDatabase();
		}
	}

	private void writeGraphToFile(Float[][] distances) {
		// Write Distances to File
		PrintWriter writer;
		try {
			writer = new PrintWriter(distancesFile, "UTF-8");
			for (int i = 0; i < distances.length; i++) {
				String line = Arrays.stream(distances[i]).map(s -> s.toString()).collect(Collectors.joining(";"));
				writer.println(line);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Loads graph distances from file and the graph values from database
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	private Graph<GasStation> loadGraphFromFile(File distancesFile) throws IOException {

		log.info("Loading Graph from File and Database.");
		// still need to fetch the values of/for graph from database
		List<GasStation> allStations = new ArrayList<>(dbHandler.getAllGasStations());

		Float[][] distances;
		int perc = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(distancesFile))) {
			String[] split = br.readLine().split(";");
			int size = split.length;
			// evaluate the graph size by the amount of values in the first line in the file
			if (size != allStations.size()) {
				log.info("Size Mismatch. Cannot load Graph from file.");
				return null;
			}
			distances = new Float[size][size];
			int i = 0;
			// translate a semicolon separated line of distances to a distance array for
			// vertex i
			distances[i++] = Arrays.stream(split).map(s -> Float.valueOf(s)).toArray(Float[]::new);

			// read the rest of the file into distances
			for (String line; (line = br.readLine()) != null;) {
				split = line.split(";");
				if (split.length != size) {
					return null;
				}
				distances[i++] = Arrays.stream(split).map(s -> Float.valueOf(s)).toArray(Float[]::new);
				if (((int) ((double) (i) / size * 100)) > perc) {
					log.info("Vertices: " + perc + " %");
					perc += 10;
				}
			}
			if (((int) ((double) (i + 1) / size * 100)) > perc) {
				log.info("Vertices: " + perc + " %");
				perc += 10;
			}

			if (i != distances.length) {
				log.error("File has wrong amount of lines. No distance parsing possible!");
			}
		}

		try {
			return new Graph<GasStation>(allStations, distances);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
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

		Float[][] distances = new Float[amountStations][amountStations];

		log.info("Building all vertices and edges for each station.");
		double buildStartTime = System.currentTimeMillis();
		int perc = 0;
		// for each gasstation read lat lon
		for (int i = 0; i < graphMap.length; i++) {

			double lat_a = graphMap[i][0];
			double lon_a = graphMap[i][1];

			// distances from vertex i to all other vertices
			Float[] neighbourDistances = new Float[amountStations];

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

		if (loadFromFileIsEnabled) {
			writeGraphToFile(distances);
		}

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
	public List<Vertex<GasStation>> explorativeAStar(GasStation start, GasStation end, short maxRange,
			short averageSpeed, double x) {

		// 0 < x < 1
		x = Math.max(0.0, Math.min(x, 1.0));

		PriorityQueue<Vertex<GasStation>> open = new PriorityQueue<>(new VertexComparator<GasStation>());
		List<Vertex<GasStation>> closed = new ArrayList<>();

		// Build heuristic map without database
		Map<Vertex<GasStation>, Double> heuristicMap = new HashMap<>();
		Map<Vertex<GasStation>, Vertex<GasStation>> predecessorMap = new HashMap<>();

		// build heurstic, later fetch it from the database
		Float[][] distances = graph.getDistances();
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