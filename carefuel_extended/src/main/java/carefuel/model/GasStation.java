package carefuel.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.transaction.Transactional;

import org.hibernate.annotations.Type;
import org.json.JSONObject;

/**
 * Entity class to represent the table GASSTATION in the database
 *
 * @author Wolfgang
 *
 */
@Entity
@Table(name = "gas_station")
public class GasStation implements Serializable {
	// private static final Logger log = LogManager.getLogger(Main.class);

	@Id // tells hibernate that this is the primary key
	@Type(type = "pg-uuid")
	@Column(name = "id", unique = true, updatable = false)
	private java.util.UUID id;

	@Column(name = "name")
	private String name;

	@Column(name = "brand")
	private String brand;

	@Column(name = "street")
	private String streetName;

	@Column(name = "house_number")
	private String houseNumber;

	@Column(name = "post_code")
	private String postalCode;

	@Column(name = "place")
	private String city;

	@Column(name = "lat")
	private double latitude;

	@Column(name = "lng")
	private double longitude;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "stid")
	private Set<GasStationPrice> gasStationPrices;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "stid")
	private Set<GasStationPricePrediction> gasStationPricePredictions;

	/**
	 * the default constructor is necessary for hibernate to get all gas stations
	 * from the database
	 */
	public GasStation() {

	}

	/**
	 * @return the brand
	 */
	public String getBrand() {
		return brand;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return the gasStationPricePredictions
	 */
	@Transactional
	public Set<GasStationPricePrediction> getGasStationPricePredictions() {
		return gasStationPricePredictions;
	}

	/**
	 * @return the gasStationPrices
	 */
	@Transactional
	public Set<GasStationPrice> getGasStationPrices() {
		return gasStationPrices;
	}

	/**
	 * @return the houseNumber
	 */
	public String getHouseNumber() {
		return houseNumber;
	}

	/**
	 * @return the id
	 */
	public java.util.UUID getId() {
		return id;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the postalCode
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * @return the streetName
	 */
	public String getStreetName() {
		return streetName;
	}

	/**
	 * @param brand
	 *            the brand to set
	 */
	public void setBrand(String brand) {
		this.brand = brand;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @param gasStationPricePredictions
	 *            the gasStationPricePredictions to set
	 */
	public void setGasStationPricePredictions(Set<GasStationPricePrediction> gasStationPricePredictions) {
		this.gasStationPricePredictions = gasStationPricePredictions;
	}

	/**
	 * @param gasStationPrices
	 *            the gasStationPrices to set
	 */
	public void setGasStationPrices(Set<GasStationPrice> gasStationPrices) {
		this.gasStationPrices = gasStationPrices;
	}

	/**
	 * @param houseNumber
	 *            the houseNumber to set
	 */
	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(java.util.UUID id) {
		this.id = id;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param postalCode
	 *            the postalCode to set
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * @param streetName
	 *            the streetName to set
	 */
	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	/**
	 * Computes distance in kilometers
	 *
	 * @param other
	 * @return
	 */
	public static double computeDistanceToGasStation(double lat_a, double lon_a, double lat_b, double lon_b) {
		lat_a = Math.toRadians(lat_a);
		lat_b = Math.toRadians(lat_b);
		lon_a = Math.toRadians(lon_a);
		lon_b = Math.toRadians(lon_b);

		return 6378.388 * Math
				.acos((Math.sin(lat_a) * Math.sin(lat_b)) + (Math.cos(lat_a) * Math.cos(lat_b) * Math.cos(lon_b - lon_a)));
	}

	/**
	 * returns the gas station in json format
	 *
	 * @return
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		json.put("id", getId());
		json.put("name", getName());
		json.put("brand", getBrand());
		json.put("street", getStreetName());
		json.put("houseNumer", getHouseNumber());
		json.put("postalCode", getPostalCode());
		json.put("city", getCity());
		json.put("lat", getLatitude());
		json.put("lng", getLongitude());

		return json;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GasStation [id=" + id + ", name=" + name + ", brand=" + brand + ", streetName="
				+ streetName + ", houseNumber=" + houseNumber + ", postalCode=" + postalCode + ", city="
				+ city + ", latitude=" + latitude + ", longitude=" + longitude + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof GasStation)) {
			return false;
		}
		GasStation other = (GasStation) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
