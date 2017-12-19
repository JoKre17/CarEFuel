package carefuel.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import carefuel.model.GasStation;
import carefuel.path.Vertex;

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

	private final static Logger log = LogManager.getLogger(RequestMapping.class);

	DateFormatter df = new DateFormatter("DD/MM/YYYY HH:mm");

	@RequestMapping(value = "station/", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public JSONArray getGasStationById() {
		DatabaseHandler databaseHandler = new DatabaseHandler();
		JSONArray toReturn = new JSONArray();
		Set<GasStation> gasStations = databaseHandler.getAllGasStations();

		for (GasStation gasStation : gasStations) {
			toReturn.put(gasStation.toJSON());
		}

		return toReturn;
	}

	@RequestMapping(value = "station/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public JSONObject getGasStationById(@PathVariable String id) {
		DatabaseHandler databaseHandler = new DatabaseHandler();

		GasStation gasStation = databaseHandler.getGasStation(id);

		return gasStation.toJSON();
	}

	@RequestMapping(value = "path", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getPath(@RequestParam(value = "from", required = true) String fromId,
			@RequestParam(value = "to", required = true) String toId,
			@RequestParam(value = "startTime", required = true) String startTime,
			@RequestParam(value = "capacity", required = false) Integer capacity,
			@RequestParam(value = "consumption", required = false) Double consumption) {

		log.info("Path Request received");
		log.info("from: " + fromId);
		log.info("to: " + toId);
		log.info("startTime: " + startTime);
		log.info("capacity: " + capacity);
		log.info("consumption: " + consumption);

		Date startTimeDate = new Date();
		try {
			startTimeDate = df.parse(startTime, Locale.GERMAN);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		float range = (float) ((capacity / consumption) * 100.0);

		// km/h
		float averageSpeed = 100;

		if (Main.pathFinder == null) {
			return new JSONArray().toString();
		}

		List<Vertex<GasStation>> route = Main.pathFinder.explorativeAStar(fromId, toId, startTimeDate, range,
				averageSpeed, 0.0F);

		JSONArray path = new JSONArray();

		for (Vertex<GasStation> v : route) {
			GasStation station = v.getValue();
			JSONObject stop = new JSONObject();

			JSONObject loc = new JSONObject();
			loc.put("lat", station.getLatitude());
			loc.put("lng", station.getLongitude());

			stop.put("id", station.getId().toString());
			stop.put("location", loc);

			path.put(stop);
		}

		return path.toString();
	}

}
