
var apiKey = AIzaSyB-CnojIMKDAWmZ6NjnPvrrXncOMKk3NXc;
var map;

function initMap() {
	map = new google.maps.Map(document.getElementById('map'), {
		center : {
			lat : 52.37052,
			lng : 9.73322
		},
		zoom : 8
	});
}