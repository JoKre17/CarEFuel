var apiKey = "AIzaSyAvS9-jUE8zO6234VVC5_O2GvkDLtN27K0";
var map;
var directionsService;
var directionsDisplay;

/**
 * initialization method for the map
 * loads the api
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
	
	
	var from_autocomplete = new google.maps.places.Autocomplete(from_input);
    from_autocomplete.bindTo('bounds', map);
    
    var to_autocomplete = new google.maps.places.Autocomplete(to_input);
    to_autocomplete.bindTo('bounds', map);
    
    from_autocomplete.addListener('place_changed', function() {
    	console.log("changed!");
    	console.log(from_autocomplete.getPlace());
    });
    
    to_autocomplete.addListener('place_changed', function() {
    	console.log("changed!");
    	console.log(to_autocomplete.getPlace());
    });
	
	positions = [{
				lng: 9.5167,
				lat: 52.5167
			}, {
				lng: 9.701,
				lat: 52.326
			}, {
				lng: 9.791,
				lat: 52.426
			}, {
				lng: 9.8367,
				lat: 52.2167
			}
	]

	centerToUserPosition();
	calculateAndDisplayRoute(positions);
}

/**
 * centers the map to the users position
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
 * @param positions
 * 		positions as array of objects with lng and lat
 * @returns
 */
function displayMarkers(positions){
	for (pos in positions){
		
		position = positions[pos];
		console.log(position);
		
		var marker = new google.maps.Marker({
	        position: position,
	        map: map
	    });
	}
}

/**
 * draws a polyline between coordinates. 0-1, 1-2, 2-3...
 * @param positions
 * 		route in the right order
 * @returns
 */
function calculateAndDisplayRoute(positions) {

	var waypoints = [];
	
	for(var i=1; i<positions.length-1; i++){
		waypoints.push({
			location : positions[i],
			stopover : true
		})
	}
	
    directionsService.route({
      origin: positions[0],
      destination: positions[positions.length-1],
      waypoints: waypoints,
      optimizeWaypoints: true,
      travelMode: 'DRIVING'
    }, function(response, status) {
    	console.log(response);
      if (status === 'OK') {
        directionsDisplay.setDirections(response);
        var route = response.routes[0];
        var summaryPanel = document.getElementById('routeContainer');
        summaryPanel.innerHTML = '';
        
        // For each route, display summary information.
        summaryPanel.innerHTML += '<h4>Start</h4>';
        summaryPanel.innerHTML += route.legs[0].start_address;
        summaryPanel.innerHTML += '<br>';
        
        for (var i = 1; i < route.legs.length; i++) {
          summaryPanel.innerHTML += '<h5>Stop: ' + (i) + '</h5>';
          summaryPanel.innerHTML += route.legs[i].start_address + '<br>';
          summaryPanel.innerHTML += route.legs[i].distance.text + '<br>';
        }
        
        summaryPanel.innerHTML += '<h4>Ziel</h4>';
        summaryPanel.innerHTML += route.legs[route.legs.length-1].end_address;
        
      }
    });
  }