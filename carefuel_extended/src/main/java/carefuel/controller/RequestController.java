package carefuel.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	@RequestMapping(value = "station/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public GasStation getGasStationById(@PathVariable long id) {
		// return "{station: {id: " + id + "}}";
		return new GasStation();
	}

	@RequestMapping(value = "path", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getGasStationById(@RequestParam(value = "from", required = true) String from,
			@RequestParam(value = "to", required = true) String to,
			@RequestParam(value = "capacity", required = false) Integer capacity,
			@RequestParam(value = "consumption", required = false) Double consumption) {
		// return "{station: {id: " + id + "}}";
		log.info("received path request");
		String response = "{\"from\":\"" + from + "\", \"to\":\"" + to + "\", \"capacity\":" + capacity
				+ ", \"consumption\":" + consumption + "}";
		log.info(response);
		return response;
	}

}
