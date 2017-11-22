
function routeContainerPressed() {
	var window_width = $(window).width();
	if(window_width > 800) {
		return;
	}
	
	var container = document.getElementById("routeContainer");
	var style = window.getComputedStyle(container);
	
	var top = style.getPropertyValue('top');
	
	// if route Counter is displayed fullscreen
	if(container.style.top == 0 || container.style.top == "0px" || container.style.top == "0%") {
		$(container).animate({top: "85%"});
	} else {
		$(container).animate({top: "0%"});
	}

}

var window_width = $(window).width();

window.onresize = function(event) {
	var new_window_width = $(window).width();
	
	if(window_width < 800 && new_window_width > 800) {
		var container = document.getElementById("routeContainer");
		var style = window.getComputedStyle(container);
		
		container.style.top = '0';
	}
	
	if(window_width > 800 && new_window_width < 800) {
		var container = document.getElementById("routeContainer");
		var style = window.getComputedStyle(container);
		
		container.style.top = '85%';
	}
	
	window_width = new_window_width;
};