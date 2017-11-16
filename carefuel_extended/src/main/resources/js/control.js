const CONTROL_MAX_REL_WIDTH = 0.5;
const CONTROL_MAX_WIDTH = 300;

function submitButtonPressed() {
	var from = document.getElementById('from').value;
	var to = document.getElementById('to').value;
	var capacity = document.getElementById('capacity').value;
	var consumption = document.getElementById('consumption').value;
	
	if(capacity == "") {
		capacity = 3;
	}
	
	if(consumption == "") {
		consumption = 5.6;
	}
	
	var request = {'from': from, 'to': to, 'capacity': capacity, 'consumption': consumption};
	
	console.log(request);

}

function hideButtonPressed() {
	var container = document.getElementById("controlContainer");
	var style = window.getComputedStyle(container);
	
	var width = style.getPropertyValue('width');
	
	console.log();
	if(container.style.left == 0 || container.style.left == "0px" || container.style.left == "0%") {
		$(container).animate({left: "-"+ width}, {
			complete: function () {
				container.style.maxWidth = 'none';
				container.style.left = -CONTROL_MAX_REL_WIDTH * 100 + "%";
			}
		});
		
	} else {
		container.style.maxWidth = CONTROL_MAX_WIDTH + 'px';
		container.style.left = "-" + width;
		
		$(container).animate({left: "0%"});
	}

}

var window_width = $(window).width();

window.onresize = function(event) {
	var new_window_width = $(window).width();
	
	if(window_width < 800 && new_window_width > 800) {
		var container = document.getElementById("controlContainer");
		var style = window.getComputedStyle(container);
		
		container.style.maxWidth = CONTROL_MAX_WIDTH + 'px';
		container.style.left = "0%";
	}
	
	window_width = new_window_width;
};