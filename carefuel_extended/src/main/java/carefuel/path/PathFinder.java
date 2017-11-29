package carefuel.path;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import carefuel.controller.DatabaseHandler;
import carefuel.model.GasStation;

public class PathFinder {

	private static final Logger log = LogManager.getLogger(PathFinder.class);

	private DatabaseHandler dbHandler;
	private Graph<GasStation> graph;

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
			loadGraphFromFile(pathFinderFile);
		} else {
			loadGraphFromDatabase();
		}
	}

	private void loadGraphFromFile(File f) {

	}

	/**
	 * Loads the graph from database with fetching all GasStations
	 */
	private void loadGraphFromDatabase() {
		log.info("Loading Graph from database");

		log.info("Fetching all stations from database.");
		double fetchStartTime = System.currentTimeMillis();
		List<GasStation> allStations = new LinkedList<GasStation>(dbHandler.getAllGasStations());
		// Map<UUID, Pair<Double, Double>> graphMap = new HashMap<>();
		int amountStations = allStations.size();
		double[][] graphMap = new double[amountStations][2];
		for (int i = 0; i < allStations.size(); i++) {
			graphMap[i] = new double[] { allStations.get(i).getLatitude(), allStations.get(i).getLongitude() };
		}

		log.info("Fetched " + allStations.size() + " stations.");
		log.info("Fetching all stations took " + (System.currentTimeMillis() - fetchStartTime) / 1000.0 + " seconds.");

		graph = new Graph<GasStation>();
		graph.setSize(amountStations);
		Short[][] distances = graph.getDistances();

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

				Short distance = (short) (computeDistanceToGasStation(lat_a, lon_a, lat_b, lon_b));
				neighbourDistances[j] = distance;

			}

			distances[i] = neighbourDistances;

			if (((int) ((double) (i) / amountStations * 100)) > perc) {
				log.info("Vertices: " + perc + " %");
				perc += 10;
			}

		}

		graph.setDistances(distances);
		graph.setVertices(allStations);

		log.info("Building all vertices and edges took " + (System.currentTimeMillis() - buildStartTime) / 1000.0
				+ " seconds.");

	}

	/**
	 * Computes distance in kilometers
	 * 
	 * @param other
	 * @return
	 */
	public double computeDistanceToGasStation(GasStation from, GasStation to) {
		return 6378.388
				* Math.acos(Math.sin(from.getLatitude()) * Math.sin(to.getLatitude()) + Math.cos(from.getLatitude())
						* Math.cos(to.getLatitude()) * Math.cos(to.getLongitude() - from.getLongitude()));
	}

	/**
	 * Computes distance in kilometers
	 * 
	 * @param other
	 * @return
	 */
	public double computeDistanceToGasStation(double lat_a, double lon_a, double lat_b, double lon_b) {
		return 6378.388 * Math
				.acos(Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a) * Math.cos(lat_b) * Math.cos(lon_b - lon_a));
	}

	public List<Edge<GasStation>> explorativeAStar(GasStation start, GasStation end, double maxRange) {

		graph.setMaxRange(maxRange);

		log.info(graph.getNeighbours(start).size());

		return null;
	}

}