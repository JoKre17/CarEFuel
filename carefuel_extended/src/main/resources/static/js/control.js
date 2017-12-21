const CONTROL_MAX_REL_WIDTH = 0.5;
const CONTROL_MAX_WIDTH = 300;
const TANKERKOENIG_API_KEY = "11111111-2222-3333-4444-000000000147";
const TANKERKOENIG_URL = "https://test2.tankerkoenig.de/json/list.php?";

function submitButtonPressed() {
	
	var from_temp = from_autocomplete.getPlace().geometry.location;
	var from = {
		lat: from_temp.lat(),
		lng: from_temp.lng()
	};
	var to_temp = to_autocomplete.getPlace().geometry.location;
	var to = {
			lat: to_temp.lat(),
			lng: to_temp.lng()
	};

	var startTime = $('#dateTimePicker').data('DateTimePicker').date()._i;
	var capacity = document.getElementById('capacity').value;
	var consumption = document.getElementById('consumption').value;
	
	if (capacity == "") {
		capacity = 3;
	}

	if (consumption == "") {
		consumption = 5.6;
	}
	
	var request = {
		'from' : getClosestGasStation(from),
		'to' : getClosestGasStation(to),
		'startTime' : startTime,
		'capacity' : capacity,
		'consumption' : consumption
	};
	
	var GET_Request_url = "rest/path?"
	for ( var key in request) {
		GET_Request_url += key + "=" + request[key] + "&"
	}
	GET_Request_url = GET_Request_url.substring(0, GET_Request_url.length - 1)
	request['request'] = GET_Request_url;

	console.log(request['request']);

	$.ajax({
		type : "GET",
		dataType : "json",
		url : request['request'],
		success : function(data) {
			
			// now draw the route
			positions = []
			start_pos = {
					lat: from.lat,
					lng: from.lng
			}
			
			end_pos = {
					lat: to.lat,
					lng: to.lng
			}
			
			positions.push(start_pos)
			
			for(var i in data) {
				console.log(data[i]);
				pos = {
						lat: data[i]['location']['lat'],
						lng: data[i]['location']['lng'] 
				}
				positions.push(pos)
			}
			
			positions.push(end_pos)
			
			calculateAndDisplayRoute(positions)
		},
		complete : function(xhr, textStatus) {
//			console.log(xhr.status);
		}
	});

}

/**
 * get closest gasStation to given coordinates
 * 
 * @param coords
 *            coordinates in lat and lng
 * @returns one gasStation
 */
function getClosestGasStation(coords){
	var rad = 5;
	var stations = [];
	while (stations.length == 0) {
		stations = getGasStationsByRadius(rad, coords);
		rad += 5;
	}
	
	if (stations.length == 1){
		return stations[0].id;
	} else {
		// iterate over list of stations and return the closest to the target
		
		// temporary
		return stations[0].id;
	}
}

/**
 * 
 * get request to tankerkoenig API to get all gasStations in a given radius
 * 
 * @param rad
 *            radius in km
 * @returns returns all gasStations in a given radius
 */
function getGasStationsByRadius(rad, coords){
	var request = {
			'lat' : coords.lat,
			'lng' : coords.lng,
			'rad' : rad,
			'type' : "all",
			'apikey' : TANKERKOENIG_API_KEY
		};
		var GET_Request_url = TANKERKOENIG_URL
		for ( var key in request) {
			GET_Request_url += key + "=" + request[key] + "&"
		}
		GET_Request_url = GET_Request_url.substring(0, GET_Request_url.length - 1)
		request['request'] = GET_Request_url;
	
		console.log(request);
		
		var toReturn = null;
	$.ajax({
		type : "GET",
		dataType : "json",
		url : request['request'],
		async : false,
		success : function(data) {
			toReturn = data.stations;
		},
		complete : function(xhr, textStatus) {
			console.log(xhr.status);
		}
	});
	
	return toReturn;
	
}

function hideButtonPressed() {
	var container = document.getElementById("controlContainer");
	var style = window.getComputedStyle(container);

	var width = style.getPropertyValue('width');

	console.log();
	if (container.style.left == 0 || container.style.left == "0px"
			|| container.style.left == "0%") {
		$(container).animate({
			left : "-" + width
		}, {
			complete : function() {
				container.style.maxWidth = 'none';
				container.style.left = -CONTROL_MAX_REL_WIDTH * 100 + "%";
			}
		});

		var $elem = $('#hide');
		// we use a pseudo object for the animation
		// (starts from `0` to `angle`), you can name it as you want
		$({
			deg : 0
		}).animate({
			deg : 180
		}, {
			duration : 350,
			step : function(now) {
				$elem.css({
					transform : 'rotate(' + now + 'deg)'
				});
			}
		});

	} else {
		container.style.maxWidth = CONTROL_MAX_WIDTH + 'px';
		container.style.left = "-" + width;

		$(container).animate({
			left : "0%"
		});

		var $elem = $('#hide');
		// we use a pseudo object for the animation
		// (starts from `0` to `angle`), you can name it as you want
		$({
			deg : 180
		}).animate({
			deg : 360
		}, {
			duration : 350,
			step : function(now) {
				$elem.css({
					transform : 'rotate(' + now + 'deg)'
				});
			}
		}, {
			complete : function() {
				$elem.css({
					transform : 'rotate(0deg)'
				});
			}
		});
	}

}

var window_width = $(window).width();

window.onresize = function(event) {
	var new_window_width = $(window).width();

	if (window_width < 800 && new_window_width > 800) {
		var container = document.getElementById("controlContainer");
		var style = window.getComputedStyle(container);

		container.style.maxWidth = CONTROL_MAX_WIDTH + 'px';
		container.style.left = "0%";
	}

	window_width = new_window_width;
};