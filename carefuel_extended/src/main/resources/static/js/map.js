
var apiKey = "AIzaSyAvS9-jUE8zO6234VVC5_O2GvkDLtN27K0";
var map;

function initMap() {
	map = new google.maps.Map(document.getElementById('map'), {
		center : {
			lat : 52.37052,
			lng : 9.73322
		},
		zoom : 8,
		disableDefaultUI: true
	});
}