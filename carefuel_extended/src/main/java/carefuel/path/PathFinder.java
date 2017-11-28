package carefuel.path;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
		log.info("Loading graph completed in " + (System.currentTimeMillis()-startTime)/1000.0 + " seconds.");
		
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
		
		this.graph = new Graph<GasStation>();

		log.info("Fetching all stations from database.");
		double fetchStartTime = System.currentTimeMillis();
		Set<GasStation> allStations = dbHandler.getAllGasStations();
		log.info("Fetched " + allStations.size() + " stations.");
		log.info("Fetching all stations took " + (System.currentTimeMillis()-fetchStartTime)/1000.0 + " seconds.");

		List<Vertex<GasStation>> allVertices = new LinkedList<>();

		log.info("Building all vertices and edges for each station.");
		double buildStartTime = System.currentTimeMillis();
		for (Iterator<GasStation> iter = allStations.iterator(); iter.hasNext();) {
			GasStation from = iter.next();
			Vertex<GasStation> vertex = new Vertex<GasStation>(from);

			for (Iterator<GasStation> connectionIter = allStations.iterator(); connectionIter.hasNext();) {
				GasStation to = connectionIter.next();

				if (from.equals(to)) {
					continue;
				}

				Edge<GasStation> connection = new Edge<>(from, to);
				vertex.addConnection(connection);
			}

			allVertices.add(vertex);
		}
		log.info("Building all vertices and edges took " + (System.currentTimeMillis()-buildStartTime)/1000.0 + " seconds.");

		this.graph.addVertices(allVertices);
	}

}
