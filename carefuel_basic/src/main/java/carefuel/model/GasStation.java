package carefuel.model;

/**
 * GasStation class to model gas station objects.
 *
 * @author jwall
 *
 */
public class GasStation {

	private String arrivalDate;
	private int ID;
	private double predictedPrice;
	private double lon;
	private double lat;

	/**
	 * Constructor of the GasStation class.
	 *
	 * @param arrivalDate
	 *            estimated arrival time
	 * @param ID
	 *            unique ID of the gas station
	 * @param lon
	 *            lon coordinate of the gas station
	 * @param lat
	 *            lat coordinate of the gas station
	 * @param predictedPrice
	 *            price predicted by the PricePredictor
	 */
	public GasStation(String arrivalDate, int ID, double lon, double lat, int predictedPrice) {
		this.setArrivalDate(arrivalDate);
		this.setID(ID);
		this.setLon(lon);
		this.setLat(lat);
		this.setPredictedPrice(predictedPrice);
	}

	/**
	 * @return arrival date
	 */
	public String getArrivalDate() {
		return arrivalDate;
	}

	/**
	 * @param arrivalDate
	 *            arrival date
	 */
	public void setArrivalDate(String arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	/**
	 * @return ID of the gas station
	 */
	public int getID() {
		return ID;
	}

	/**
	 * @param iD
	 *            new ID of the gas station
	 */
	public void setID(int iD) {
		ID = iD;
	}

	/**
	 * @return predicted price for the gas station
	 */
	public double getPredictedPrice() {
		return predictedPrice;
	}

	/**
	 * @param predictedPrice
	 *            new predicted price
	 */
	public void setPredictedPrice(double predictedPrice) {
		this.predictedPrice = predictedPrice;
	}

	/**
	 * @return lon coordinate of the gas station
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @param lon
	 *            new lon coordinate of the gas station
	 */
	public void setLon(double lon) {
		this.lon = lon;
	}

	/**
	 * @return lat coordinate of the gas station
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @param lat
	 *            new lan coordinate of the gas station
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}
}
