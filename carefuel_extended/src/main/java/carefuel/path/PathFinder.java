package carefuel.path;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

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
		List<GasStation> allStations = dbHandler.getAllGasStations().stream().collect(Collectors.toList());
		log.info("Fetched " + allStations.size() + " stations.");
		log.info("Fetching all stations took " + (System.currentTimeMillis() - fetchStartTime) / 1000.0 + " seconds.");

		graph = new Graph<GasStation>(allStations.size());
		double[][] distances = graph.getDistances();

		log.info("Building all vertices and edges for each station.");
		double buildStartTime = System.currentTimeMillis();

		GasStation[] allStationsArray = allStations.toArray(new GasStation[1]);

		int perc = 0;
		for (int i = 0; i < allStationsArray.length; i++) {
//			GasStation from = allStationsArray[i];
			
			double[] neighbourDistances = new double[allStations.size()];

			for(int j = 0; j < allStationsArray.length; j++) {
				if(i == j) {
					continue;
				}
				
				double distance = allStationsArray[i].computeDistanceToGasStation(allStationsArray[j]);
				neighbourDistances[j] = distance;
			}
			
			distances[i] = neighbourDistances;
			
			if (((int) ((double) (i) / allStations.size() * 100)) > perc) {
				log.info("Vertices: " + perc + " %");
				perc++;
			}
		}
		
		graph.setDistances(distances);
		graph.setVertices(allStations);

		log.info("Building all vertices and edges took " + (System.currentTimeMillis() - buildStartTime) / 1000.0
				+ " seconds.");

	}

	public List<Edge<GasStation>> explorativeAStar(GasStation start, GasStation end, double maxRange) {

		graph.setMaxRange(maxRange);

		log.info(graph.getNeighbours(start).size());

		return null;
	}

}