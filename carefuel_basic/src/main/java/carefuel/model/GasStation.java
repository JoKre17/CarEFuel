package carefuel.model;

public class GasStation {

	private String arrivalDate;
	private int ID;
	private double predictedPrice;
	private double lon;
	private double lat;
	
	public GasStation(String arrivalDate, int ID, double lon, double lat) {
		this.setArrivalDate(arrivalDate);
		this.setID(ID);
		this.setLon(lon);
		this.setLat(lat);
	}

	public String getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(String arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public double getPredictedPrice() {
		return predictedPrice;
	}

	public void setPredictedPrice(double predictedPrice) {
		this.predictedPrice = predictedPrice;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}
	
}
