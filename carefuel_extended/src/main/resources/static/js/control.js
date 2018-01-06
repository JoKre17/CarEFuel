const CONTROL_MAX_REL_WIDTH = 0.5;
const CONTROL_MAX_WIDTH = 300;
const TANKERKOENIG_API_KEY = "11111111-2222-3333-4444-000000000147";
const TANKERKOENIG_URL = "https://test2.tankerkoenig.de/json/list.php?";

function submitButtonPressed() {
	
	$("#loadingScreen").fadeIn("slow");
	
	from_autocomplete.onerror = function(){
		$("#loadingScreen").fadeOut("slow");
	};
	
	to_autocomplete.onerror = function(){
		$("#loadingScreen").fadeOut("slow");
	};
	
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
	var tankLevel = document.getElementById('tankLevel').value;
	var capacity = document.getElementById('capacity').value;
	var consumption = document.getElementById('consumption').value;
	var metric = document.getElementById('metric').value;
	var gasType = document.getElementById("route-form").elements["gasType"].value
	
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
		'tankLevel' : tankLevel,
		'capacity' : capacity,
		'consumption' : consumption,
		'metric' : metric,
		'gasType' : gasType
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
			$("#loadingScreen").fadeOut("slow");
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

function hideSearchButtonPressed() {
	$("#controlContainer").slideToggle("fast");
}

function hideRouteButtonPressed() {
	$("#routeContainer").slideToggle("fast");
}


/**
 * On resizing to bigger screen, display all container that were unvisible in mobile view
 */
var window_width = $(window).width();
$(window).resize(function(event) {
	var new_window_width = $(window).width();
	
	if (window_width < 800 && new_window_width > 800) {
		
		$('#controlContainer').show();
		$('#routeContainer').show();
	}

	window_width = new_window_width;
});
