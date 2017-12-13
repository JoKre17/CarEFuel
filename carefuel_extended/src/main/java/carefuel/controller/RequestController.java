package carefuel.controller;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import carefuel.model.GasStation;

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
	public String getPath(@RequestParam(value = "from", required = true) String from,
			@RequestParam(value = "to", required = true) String to,
			@RequestParam(value = "capacity", required = false) Integer capacity,
			@RequestParam(value = "consumption", required = false) Double consumption) {

		log.info("received path request");
		String response = "{\"from\":\"" + from + "\", \"to\":\"" + to + "\", \"capacity\":" + capacity
				+ ", \"consumption\":" + consumption + "}";
		log.info(response);
		return response;
	}

}
