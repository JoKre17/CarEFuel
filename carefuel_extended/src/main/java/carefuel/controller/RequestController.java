package carefuel.controller;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import carefuel.model.GasStation;
import carefuel.path.Vertex;
import carefuel.tank.Node;

/**
 *
 * Dummy class for handling requests.
 *
 * @TODO Add request handler functions
 *
 */
@RestController
@RequestMapping("rest/")
public class RequestController {

	private final static Logger log = LogManager.getLogger();

	DateFormatter df = new DateFormatter("dd.MM.yyyy HH:mm");

	@RequestMapping(value = "station/", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getGasStationById() {
		DatabaseHandler databaseHandler = new DatabaseHandler();
		JSONArray toReturn = new JSONArray();
		Set<GasStation> gasStations = databaseHandler.getAllGasStations();

		for (GasStation gasStation : gasStations) {
			toReturn.put(gasStation.toJSON());
		}

		return toReturn.toString();
	}

	@RequestMapping(value = "station/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getGasStationById(@PathVariable String id) {
		log.info(id);
		GasStation gasStation = Main.databaseHandler.getGasStation(id);

		return gasStation.toJSON().toString();
	}

	@RequestMapping(value = "path", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getPath(@RequestParam(value = "from", required = true) String fromId,
			@RequestParam(value = "to", required = true) String toId,
			@RequestParam(value = "startTime", required = true) String startTime,
			@RequestParam(value = "tankLevel", required = false) Integer tankLevel,
			@RequestParam(value = "capacity", required = true) Integer capacity,
			@RequestParam(value = "consumption", required = false) Double consumption,
			@RequestParam(value = "metric", required = false) Float metric,
			@RequestParam(value = "gasType", required = false) String gasTypeAsString) {

		Fuel gasType;

		if (tankLevel == null) {
			tankLevel = 0;
		}

		if (consumption == null) {
			consumption = 5.6;
		}

		// if metric is not given, select the shortest path
		if (metric == null) {
			metric = 0f;
		}

		if (gasTypeAsString == null) {
			gasType = Fuel.DIESEL;
		} else {
			try {
				gasType = Fuel.valueOf(gasTypeAsString.toUpperCase());
			} catch (java.lang.IllegalArgumentException e) {
				gasType = Fuel.DIESEL;
			}
		}

		float range = (float) ((capacity / consumption) * 100.0);

		log.debug("Path Request received");
		log.debug("from: " + fromId);
		log.debug("to: " + toId);
		log.debug("startTime: " + startTime);
		log.debug("tankLevel: " + tankLevel);
		log.debug("capacity: " + capacity);
		log.debug("consumption: " + consumption);
		log.debug("Max Range: " + range);
		log.debug("metric factor: " + metric);
		log.debug("gasType: " + gasType.toString());

		Date startTimeDate = new Date();
		try {
			startTimeDate = df.parse(startTime, Locale.GERMAN);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date today = c.getTime();

		if (startTimeDate.before(today)) {
			log.error("Requested start time before today: " + df.print(startTimeDate, Locale.GERMAN));
			return errorResponse(9002, "Requested start time before today").toString();
		}

		// km/h
		float averageSpeed = 100;

		if (Main.pathFinder == null) {
			return new JSONArray().toString();
		}

		List<Vertex<GasStation>> route;
		long time = System.currentTimeMillis();
		try {
			route = Main.pathFinder.explorativeAStar(fromId, toId, startTimeDate, tankLevel, gasType, range,
					averageSpeed, metric);
		} catch (Exception e) {
			log.error("Error while calculating route", e);
			return errorResponse(9001, "Unable to calculate route. Graph might not be loaded yet.").toString();
		}
		log.debug("Found path in " + ((System.currentTimeMillis()-time)/1000.0) + " seconds");

		for (int i = 0; i < route.size(); i++) {
			if (i == 0) {
				log.debug(route.get(i).getValue().getId());
			} else {
				GasStation from = route.get(i - 1).getValue();
				GasStation to = route.get(i).getValue();

				double distance = GasStation.computeDistanceToGasStation(from.getLatitude(), from.getLongitude(),
						to.getLatitude(), to.getLongitude());
				log.debug(route.get(i - 1).getValue().getId() + " == " + distance + " ==>"
						+ route.get(i).getValue().getId());
			}
		}
		
		// List that holds the liter-value of gas that should be tanked at the
		// corresponding gasStation
		List<Node> nodeRoute = Main.tankStrategy.computeTankStrategy(route, startTimeDate, consumption, tankLevel,
				capacity, range, averageSpeed, gasType);

		JSONArray path = new JSONArray();

		for (Node n : nodeRoute) {
			GasStation station = n.getValue();
			JSONObject stop = new JSONObject();

			JSONObject loc = new JSONObject();
			loc.put("lat", station.getLatitude());
			loc.put("lng", station.getLongitude());

			stop.put("id", station.getId().toString());
			stop.put("location", loc);

			stop.put("arrivalTime", n.getArrivalTime());
			stop.put("predictedPrice", n.getPredictedPrice());
			stop.put("fillAmount", n.getFuelToBuy());
			
			log.debug(station.getId() + " " + n.getFuelToBuy());

			path.put(stop);
		}

		/*
		 * for (Vertex<GasStation> v : route) { GasStation station = v.getValue();
		 * JSONObject stop = new JSONObject();
		 *
		 * JSONObject loc = new JSONObject(); loc.put("lat", station.getLatitude());
		 * loc.put("lng", station.getLongitude());
		 *
		 * stop.put("id", station.getId().toString()); stop.put("location", loc);
		 *
		 * path.put(stop); }
		 */

		return path.toString();
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	@ResponseBody
	private String getDownloadables() {
		File file = new File("download");

		List<File> accessibleFiles = new ArrayList<>();
		if (file.exists() && file.isDirectory()) {
			accessibleFiles = Arrays.asList(file.listFiles());
		}
		// @formatter:off
		String html = "<html>" + "<head>" + "<title>Carefuel Downloads</title>" + "</head>"
				+ "<body style=’background-color:#ccc’>" + "Downloadable Files:<br>";
		// @formatter:on
		for (File f : accessibleFiles) {
			log.debug(f.getName());
			html += "<span style='margin-left:10px'>" + "<a href='download/" + f.getName() + "'>" + f.getName()
					+ "</a><br>";
		}

		html += "</body>";

		return html;
	}

	@RequestMapping(value = "/download/{file}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	private FileSystemResource getFile(@PathVariable("file") String vmFileName) {
		File file = new File("download/" + vmFileName);
		log.info("Downloading " + file.getAbsolutePath());
		return new FileSystemResource(file);
	}

	private JSONObject errorResponse(int code, String message) {
		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("message", message);

		return json;
	}

}
