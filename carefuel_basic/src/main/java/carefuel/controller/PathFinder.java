package carefuel.controller;

import java.util.List;

import carefuel.util.FixedPathAlgorithm;

import carefuel.model.GasStation;

public class PathFinder {

	private List<GasStation> gasStations;
	private int capacity;
	private final double gasConsumption = 5.6;
	
	public PathFinder(List<GasStation> gasStations, int capacity) {
		this.gasStations = gasStations;
		this.capacity = capacity;
	}
	
	public void computeBestPath() {
		FixedPathAlgorithm f = new FixedPathAlgorithm(gasStations, capacity, gasConsumption);
		f.run();
	}
}