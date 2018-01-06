var apiKey = "AIzaSyAvS9-jUE8zO6234VVC5_O2GvkDLtN27K0";
var map;
var directionsService;
var directionsDisplay;
var from_autocomplete;
var to_autocomplete;
/**
 * initialization method for the map loads the api
 * 
 * @returns
 */
function initMap() {
	directionsService = new google.maps.DirectionsService;
	directionsDisplay = new google.maps.DirectionsRenderer;

	map = new google.maps.Map(document.getElementById('map'), {
		zoom : 8,
		disableDefaultUI : true
	});
	directionsDisplay.setMap(map);

	var from_input = document.getElementById('from');
	var to_input = document.getElementById('to');

	from_autocomplete = new google.maps.places.Autocomplete(from_input);
	from_autocomplete.bindTo('bounds', map);

	to_autocomplete = new google.maps.places.Autocomplete(to_input);
	to_autocomplete.bindTo('bounds', map);

	from_autocomplete.addListener('place_changed', function() {
		console.log("changed!");
		console.log(from_autocomplete.getPlace().geometry.location.lat());
	});

	to_autocomplete.addListener('place_changed', function() {
		console.log("changed!");
		console.log(to_autocomplete.getPlace());
	});

	positions = [ {
		lng : 9.5167,
		lat : 52.5167
	}, {
		lng : 9.701,
		lat : 52.326
	}, {
		lng : 9.791,
		lat : 52.426
	}, {
		lng : 9.8367,
		lat : 52.2167
	} ]

	centerToUserPosition();
	calculateAndDisplayRoute(positions);
}

/**
 * centers the map to the users position
 * 
 * @returns
 */
function centerToUserPosition() {
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(function(position) {
			var pos = {
				lat : position.coords.latitude,
				lng : position.coords.longitude
			};

			map.setCenter(pos);
			return pos;
		}, function() {
			handleLocationError(true, infoWindow, map.getCenter());
		});
	} else {
		// Browser doesn't support Geolocation
		handleLocationError(false, infoWindow, map.getCenter());
		return null;
	}
}

/**
 * displays markers on given positions
 * 
 * @param positions
 *            positions as array of objects with lng and lat
 * @returns
 */
function displayMarkers(positions) {
	for (pos in positions) {

		position = positions[pos];
		console.log(position);

		var marker = new google.maps.Marker({
			position : position,
			map : map
		});
	}
}

/**
 * draws a polyline between coordinates. 0-1, 1-2, 2-3...
 * 
 * @param positions
 *            route in the right order
 * @returns
 */
function calculateAndDisplayRoute(positions) {

	var waypoints = [];

	for (var i = 1; i < positions.length - 1; i++) {
		waypoints.push({
			location : positions[i],
			stopover : true
		})
	}

	directionsService.route({
		origin : positions[0],
		destination : positions[positions.length - 1],
		waypoints : waypoints,
		optimizeWaypoints : true,
		travelMode : 'DRIVING'
	}, function(response, status) {
		console.log(response);
		if (status === 'OK') {
			directionsDisplay.setDirections(response);
			var route = response.routes[0];
			var routeInformation = "";

			// For each route, display summary information.
			routeInformation += '<h4>Start</h4>';
			routeInformation += '<div class="address">' + route.legs[0].start_address + '</div>';
			
			for (var i = 0; i < route.legs.length; i++) {
				routeInformation += '<div class="rtable"><div class="rrow">';
				routeInformation += '<div class="distance">' + route.legs[i].distance.text + '</div>' + '<div class="arrow">&#8675;</div>';
				routeInformation += '</div></div>';
				
				console.log(routeInformation);
				if (i == (route.legs.length - 1)) {
					routeInformation += '<h4>Ziel</h4>';
				} else {
					routeInformation += '<h5>Stop: ' + (i+1) + '</h5>';
				}
				routeInformation += '<div class="address">' + route.legs[0].end_address + '</div>';
			}
			
			document.getElementById('routeContainer').innerHTML = routeInformation;
		}
	});
}