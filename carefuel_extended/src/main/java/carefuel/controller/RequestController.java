package carefuel.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	// Insert request handler functions here

	@RequestMapping(value = "station/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public GasStation getGasStationById(@PathVariable long id) {
		// return "{station: {id: " + id + "}}";
		return new GasStation();
	}
}
