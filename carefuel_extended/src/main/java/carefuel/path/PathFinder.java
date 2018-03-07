package carefuel.path;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Function;
import java.util.stream.IntStream;

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
	private final int N_THREADS = Runtime.getRuntime().availableProcessors() * 4;
	private ExecutorService es = Executors.newFixedThreadPool(N_THREADS);

	public PathFinder(DatabaseHandler dbHandler) {

		this.dbHandler = dbHandler;

	}

	/**
	 * Setup the PathFinder. Basically only loading the graph
	 */
	public void setup() {
		log.info("Parallelization of A* Algorithm with " + N_THREADS);

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
		try {
			this.graph = loadGraphFromDatabase();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the graph from database with fetching all GasStations
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private Graph<GasStation> loadGraphFromDatabase() throws InterruptedException, ExecutionException {
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

		final int AMOUNT_STATIONS = graphMap.length;

		final int[] INDEX_SPLITTINGS = IntStream.rangeClosed(0, N_THREADS)
				.mapToDouble(i -> (i / (double) N_THREADS) * AMOUNT_STATIONS).mapToInt(d -> (int) d).toArray();
		CompletableFuture<?>[] completables = new CompletableFuture<?>[N_THREADS];
		final List<Double> processes = new ArrayList<>();
		IntStream.range(0, N_THREADS).forEach(i -> processes.add(0.0));

		// calculate one directional distances (1/2 of the matrix)
		for (int i = 0; i < (INDEX_SPLITTINGS.length - 1); i++) {
			final int index = i;
			log.debug(index + ": " + INDEX_SPLITTINGS[index] + " -> " + INDEX_SPLITTINGS[index + 1]);
			completables[index] = CompletableFuture.runAsync(() -> {
				IntStream.range(INDEX_SPLITTINGS[index], INDEX_SPLITTINGS[index + 1]).forEach(j -> {
					double lat_a = graphMap[j][0];
					double lon_a = graphMap[j][1];

					// distances from vertex i to all other vertices
					float[] neighbourDistances = distances[j];

					// distance to itself is 0
					neighbourDistances[j] = 0.0F;

					// distances which are not yet calculated
					for (int k = j; k < graphMap.length; k++) {

						double lat_b = graphMap[k][0];
						double lon_b = graphMap[k][1];

						neighbourDistances[k] = (float) (GasStation.computeDistanceToGasStation(lat_a, lon_a, lat_b,
								lon_b));

					}

					distances[j] = neighbourDistances;

					// log.info("Thread " + index + ": " + ((j - INDEX_SPLITTINGS[index]) / (double)
					// (INDEX_SPLITTINGS[index + 1] - INDEX_SPLITTINGS[index])));
					processes.set(index, (j - INDEX_SPLITTINGS[index])
							/ (double) (INDEX_SPLITTINGS[index + 1] - INDEX_SPLITTINGS[index]));
				});

			}, es);
		}

		// wait until all one directional distances are calculated and print process
		// every 10%
		int perc = 0;
		while (true) {
			double overallProcess = 0;
			for (double p : processes) {
				overallProcess += (p / N_THREADS);
			}

			// log.info(overallProcess);
			if ((overallProcess * 100) > perc) {
				log.info("Distances calculated:  " + perc + " %");
				perc += 10;
			}

			boolean allDone = true;
			for (CompletableFuture<?> f : completables) {
				if (!f.isDone()) {
					allDone = false;
					break;
				}
			}
			if (allDone) {
				break;
			}

			Thread.sleep(100);
		}
		log.info("Distances calculated: 100 %");

		// Copy symmetrical values
		for (int i = 0; i < (INDEX_SPLITTINGS.length - 1); i++) {
			final int index = i;
			// System.out.println(index + ": " + PERCENTAGE_MSG_STEP_INDICES_2[index] + " ->
			// "
			// + PERCENTAGE_MSG_STEP_INDICES_2[index + 1]);
			completables[index] = CompletableFuture.runAsync(() -> {
				IntStream.range(INDEX_SPLITTINGS[index], INDEX_SPLITTINGS[index + 1]).forEach(j -> {
					// distances from vertex i to all other vertices
					float[] neighbourDistances = distances[j];

					// copy symmetrical distances
					for (int k = 0; k < j + 1; k++) {
						neighbourDistances[k] = distances[k][j];
					}

					distances[j] = neighbourDistances;
				});

			}, es);
		}

		// wait until all symmetrical distances are copied and print process every 10%
		perc = 0;
		while (true) {
			double overallProcess = 0;
			for (double p : processes) {
				overallProcess += (p / N_THREADS);
			}

			if ((overallProcess * 100) > perc) {
				log.info("Distances copied:  " + perc + " %");
				perc += 10;
			}

			boolean allDone = true;
			for (CompletableFuture<?> f : completables) {
				if (!f.isDone()) {
					allDone = false;
					break;
				}
			}
			if (allDone) {
				break;
			}

			Thread.sleep(100);
		}
		log.info("Distances copied: 100 %");

		// fetch new copy => maybe ram gets cleared ?
		Graph<GasStation> graph = null;
		try {
			graph = new Graph<GasStation>(allStations, distances);
			graph.setExecutorService(es);
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
	 * Contains default A* heuristic: Absolute cost to get to VERTEX + assumed cost
	 * to get from VERTEX to GOAL
	 *
	 * @param heuristicMap
	 * @return
	 */
	public <E> Function<Vertex<E>, Number> buildHeuristic(Map<Vertex<E>, Double> heuristicMap,
			Map<Vertex<E>, Double> gCosts) {

		return (vertex) -> {
			return gCosts.get(vertex) + heuristicMap.get(vertex);
		};
	}

	/**
	 * Explorative A* Algorithm to solve the Gas Station Problem ("extended")
	 *
	 * The algorithmus is capable of using the predicted prices for the specific
	 * arrival time at each gas station including the selection of one of the three
	 * Fuel types (Diesel, E5, E10)
	 *
	 * Calculates the best path from start GasStation to end GasStation depending on
	 * maxRange (what GasStations are reachable from another GasStation), how fast
	 * the average travel speed is and the value x, specifying the bridge between
	 * the shortest and the cheapest path.
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

		// INITIATE ALGORITHM VARIABLES

		// Build heuristic map without database
		// ConcurrentMap<Vertex<GasStation>, Double> heuristicMap = new
		// ConcurrentHashMap<>();
		ConcurrentMap<Vertex<GasStation>, Date> arriveTimes = new ConcurrentHashMap<>();
		ConcurrentMap<Vertex<GasStation>, Vertex<GasStation>> predecessorMap = new ConcurrentHashMap<>();
		ConcurrentMap<Vertex<GasStation>, Double> gCosts = new ConcurrentHashMap<>();
		ConcurrentMap<Vertex<GasStation>, Double> hCosts = new ConcurrentHashMap<>();
		ConcurrentMap<Vertex<GasStation>, Double> commulatedCosts = new ConcurrentHashMap<>();
		ConcurrentMap<Vertex<GasStation>, List<Pair<Date, Integer>>> predictionPreservationMap = new ConcurrentHashMap<>();

		List<GasStation> allStations = graph.getValues();

		PriorityBlockingQueue<Vertex<GasStation>> open = new PriorityBlockingQueue<>((int) (allStations.size() / 2.0),
				new VertexComparator<GasStation>(commulatedCosts));
		List<Vertex<GasStation>> closed = new ArrayList<>();

		// find start and end GasStations
		GasStation start = allStations.stream().filter(s -> s.getId().toString().equals(startUUID)).findFirst().get();
		GasStation end = allStations.stream().filter(s -> s.getId().toString().equals(endUUID)).findFirst().get();

		// calculate the relevant dates (26 Hours from 2h before startTime until 24h
		// into future. Noone needs >24 hours through Germany)
		Calendar c = Calendar.getInstance();
		c.setTime(startTime);
		c.add(Calendar.HOUR_OF_DAY, -2);
		final Date EARLIEST_RELEVANT_DATE = c.getTime();
		c.setTime(startTime);
		c.add(Calendar.HOUR_OF_DAY, 24);
		final Date LATEST_RELEVANT_DATE = c.getTime();

		// final double DISTANCE_TO_DRIVE =
		// GasStation.computeDistanceBetweenGasStations(start, end);
		// final int MAX_ITERATIONS = (int) ((DISTANCE_TO_DRIVE / maxRange) * 5);
		// log.debug("Calculating maximal iterations: " + MAX_ITERATIONS);

		// 0 < x < 1
		final float bordered_x = (float) Math.max(0.0, Math.min(x, 1.0));

		// define distance to end gasStation when the algorithm straight drives to goal
		// double MIN_RANGE_TO_END = 0.0;
		// if (x > 0) {
		// // 1% of distance between start and end, minimum is 3 and capped at maxRange
		// MIN_RANGE_TO_END = Math.min(maxRange, Math.max(3.0, 0.01 *
		// DISTANCE_TO_DRIVE));
		// log.debug("Using advanced algorithm with x>0 until " + MIN_RANGE_TO_END + "
		// km to destination");
		// }

		float[][] distances = graph.getDistances();

		// log.info("Build heuristic values");
		double curTime = System.currentTimeMillis();

		// predictions for end GasStation for the heuristic
		List<Pair<Date, Integer>> predictions = dbHandler.getPricePredictionBetweenDates(end.getId(), gasType,
				EARLIEST_RELEVANT_DATE, LATEST_RELEVANT_DATE);

		final int AMOUNT_STATIONS = allStations.size();

		final int[] INDEX_SPLITTINGS = IntStream.rangeClosed(0, N_THREADS)
				.mapToDouble(i -> (i / (double) N_THREADS) * AMOUNT_STATIONS).mapToInt(d -> (int) d).toArray();
		CompletableFuture<?>[] completables = new CompletableFuture<?>[N_THREADS];

		final boolean X_GREATER_ZERO = bordered_x > 0;

		// parallel calculation of heuristical values from each station to endStation
		for (int i = 0; i < (INDEX_SPLITTINGS.length - 1); i++) {
			final int index = i;
			log.debug(index + ": " + INDEX_SPLITTINGS[index] + " -> " + INDEX_SPLITTINGS[index + 1]);
			completables[index] = CompletableFuture.runAsync(() -> {
				IntStream.range(INDEX_SPLITTINGS[index], INDEX_SPLITTINGS[index + 1]).forEach(j -> {
					// x > 0 means the fuel prices weight into the edges of the graph
					// therefore it is necessary to give the heuristic also weighted
					// values for each
					// station
					if (X_GREATER_ZERO) {
						long arrivalTimeLong = new Date(
								(long) (startTime.getTime() + distances[j][allStations.indexOf(end)] / averageSpeed))
										.getTime();

						// Diesel : 1109 means 1.109 euro. Therefore 1109 is given in
						// "centicent"
						int pricePredictionInCentiCent = Fuel.getDefaultPrice(gasType);
						if (predictions.size() > 0) {
							pricePredictionInCentiCent = Collections
									.min(predictions, new Comparator<Pair<Date, Integer>>() {
										@Override
										public int compare(Pair<Date, Integer> d1, Pair<Date, Integer> d2) {
											long diff1 = Math.abs(d1.getLeft().getTime() - arrivalTimeLong);
											long diff2 = Math.abs(d2.getLeft().getTime() - arrivalTimeLong);
											return Long.compare(diff1, diff2);
										}
									}).getRight();
						}

						double pricePredictionEuro = pricePredictionInCentiCent / 1000.0;

						double hValue = distances[j][allStations.indexOf(end)]
								* (1.0 + (pricePredictionEuro - 1.0) * bordered_x);
						hCosts.put(graph.getVertexByValue(allStations.get(j)), hValue);
					} else {
						hCosts.put(graph.getVertexByValue(allStations.get(j)),
								(double) distances[j][allStations.indexOf(end)]);
					}
				});

			}, es);
		}

		for (CompletableFuture<?> f : completables) {
			f.get();
		}

		log.info("Built heuristic in " + ((System.currentTimeMillis() - curTime) / 1000.0) + " s");

		// use default heuristic
		heuristic = buildHeuristic(hCosts, gCosts);

		// initiate the A Star algorithm
		Vertex<GasStation> startNode = graph.getVertexByValue(start);
		Vertex<GasStation> endNode = graph.getVertexByValue(end);
		gCosts.put(startNode, 0.0);
		// hCosts.put(startNode, heuristicMap.get(startNode));
		open.add(startNode);
		arriveTimes.put(startNode, startTime);

		Vertex<GasStation> currentNode = null;

		Calendar calendar = Calendar.getInstance();

		while (!open.isEmpty()) {
			log.debug("Iteration " + closed.size());
			// log.info(open.size() + " nodes left to discover.");

			// break due to too many calculations and therefore fail the algorithm
			// if (closed.size() > MAX_ITERATIONS) {
			// break;
			// }

			currentNode = open.poll();
			final Vertex<GasStation> finalCurrentNode = currentNode;

			double distanceToDest = GasStation.computeDistanceToGasStation(currentNode.getValue().getLatitude(),
					currentNode.getValue().getLongitude(), end.getLatitude(), end.getLongitude());
			log.debug("Distance to destination: " + distanceToDest);
			log.debug("Heuristic to destination: " + hCosts.get(currentNode));

			// found end node
			if (currentNode.getValue().equals(end)) {
				break;
			}

			// For each neighbour of currentNode

			// expand currentNode
			List<Edge<GasStation>> neighbours = graph.getNeighbours(currentNode, maxRange, gasType);
			log.debug("Looking at " + neighbours.size() + " neighbours.");

			// stop if close enough to prevent algorithm from too many calculations
			// if (distanceToDest < MIN_RANGE_TO_END) {
			// predecessorMap.put(graph.getVertexByValue(end), currentNode);
			// Edge<GasStation> edgeToEnd = null;
			// for (Edge<GasStation> e : neighbours) {
			// if (e.getTo().equals(endNode)) {
			// edgeToEnd = e;
			// break;
			// }
			// }
			//
			// // calculate arrival time at successor
			// Date currentTime = arriveTimes.get(currentNode);
			// calendar.setTime(currentTime);
			// int timeInMins = (int) ((edgeToEnd.getDistance() / averageSpeed) * 60.0);
			// calendar.add(Calendar.MINUTE, timeInMins);
			// Date arrivalTime = calendar.getTime();
			//
			// double pricePredictionInEuro = 0;
			// // predict price
			// if (bordered_x > 0) {
			// // get predicted prices for gasStation
			// double pricePredictionInCentiCent = dbHandler
			// .getPricePredictionClosestToDate(edgeToEnd.getTo().getValue().getId(),
			// gasType, arrivalTime)
			// .getRight();
			//
			// // Diesel : 1109 means 110.9 cent. Therefore 1109 is given
			// // in "centicent"
			// pricePredictionInEuro = pricePredictionInCentiCent / 1000.0;
			//
			// }
			// double g_tentative = gCosts.get(finalCurrentNode)
			// + edgeToEnd.getValue(bordered_x, pricePredictionInEuro);
			// gCosts.put(edgeToEnd.getTo(), g_tentative);
			// currentNode = endNode;
			// break;
			// }

			final int[] NEIGHBOUR_INDEX_SPLITTINGS = IntStream.rangeClosed(0, N_THREADS)
					.mapToDouble(i -> (i / (double) N_THREADS) * neighbours.size()).mapToInt(d -> (int) d).toArray();
			completables = new CompletableFuture<?>[N_THREADS];

			for (int i = 0; i < (NEIGHBOUR_INDEX_SPLITTINGS.length - 1); i++) {
				final int index = i;
				completables[index] = CompletableFuture.runAsync(() -> {
					IntStream.range(NEIGHBOUR_INDEX_SPLITTINGS[index], NEIGHBOUR_INDEX_SPLITTINGS[index + 1])
							.forEach(j -> {
								Edge<GasStation> e = neighbours.get(j);

								Vertex<GasStation> successor = e.getTo();
								// Vertex<GasStation> predecessor = e.getFrom();

								// if already visited
								if (closed.contains(successor)) {
									return;
								}

								// calculate arrival time at successor
								Date currentTime = arriveTimes.get(finalCurrentNode);
								calendar.setTime(currentTime);
								int timeInMins = (int) ((e.getDistance() / averageSpeed) * 60.0);
								calendar.add(Calendar.MINUTE, timeInMins);
								Date arrivalTime = calendar.getTime();
								arriveTimes.put(successor, arrivalTime);

								double pricePredictionInEuro = 0;
								// predict price
								if (bordered_x > 0) {
									// get predicted prices for gasStation
									List<Pair<Date, Integer>> pricePredictions;
									if (predictionPreservationMap.containsKey(successor)) {
										pricePredictions = predictionPreservationMap.get(successor);
									} else {
										pricePredictions = dbHandler.getPricePredictionBetweenDates(
												successor.getValue().getId(), gasType, EARLIEST_RELEVANT_DATE,
												LATEST_RELEVANT_DATE);
										predictionPreservationMap.put(successor, pricePredictions);
									}

									// double pricePredictionInCentiCent =
									// dbHandler.getPricePredictionClosestToDate(successor.getValue().getId(),
									// gasType, arrivalTime).getValue();
									double pricePredictionInCentiCent;
									if (pricePredictions.isEmpty()) {
										pricePredictionInCentiCent = Fuel.getDefaultPrice(gasType);
									} else {
										pricePredictionInCentiCent = Collections
												.min(pricePredictions, new Comparator<Pair<Date, Integer>>() {
													@Override
													public int compare(Pair<Date, Integer> d1, Pair<Date, Integer> d2) {
														long diff1 = Math
																.abs(d1.getLeft().getTime() - arrivalTime.getTime());
														long diff2 = Math
																.abs(d2.getLeft().getTime() - arrivalTime.getTime());
														return Long.compare(diff1, diff2);
													}
												}).getRight();
									}

									// Diesel : 1109 means 110.9 cent. Therefore 1109 is given
									// in "centicent"
									pricePredictionInEuro = pricePredictionInCentiCent / 1000.0;

								}

								// cost so far for path start -> successor (depending on x)
								double g_tentative = gCosts.get(finalCurrentNode)
										+ e.getValue(bordered_x, pricePredictionInEuro);

								// if there is already a cheaper connection to this successor
								// found
								if (open.contains(successor) && g_tentative >= gCosts.get(successor)) {
									return;
								}

								predecessorMap.put(successor, finalCurrentNode);

								// set absolute cost for path start -> successor
								gCosts.put(successor, g_tentative);

								double hCost = heuristic.apply(successor).doubleValue();
								commulatedCosts.put(successor, hCost);

								// heuristic cost used for priority queue
								// hCosts.put(successor, hCost);

								if (!open.contains(successor)) {
									open.add(successor);
								}
							});

				}, es);
			}

			for (CompletableFuture<?> f : completables) {
				f.get();
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

		log.debug("Getting path");
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