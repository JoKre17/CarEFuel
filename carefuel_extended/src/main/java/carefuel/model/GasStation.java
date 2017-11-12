package carefuel.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Entity class to represent the table GASSTATION in the database
 *
 * @author Wolfgang
 *
 */
@Entity
@Table(name = "gas_station")
public class GasStation {
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
	private int houseNumber;

	@Column(name = "post_code")
	private String postalCode;

	@Column(name = "city")
	private String city;

	@Column(name = "lat")
	private double latitude;

	@Column(name = "lng")
	private double longitude;

	@OneToMany
	@JoinColumn(name = "stid")
	private Set<GasStationPrices> gasStationPrices;

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
		return this.brand;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return this.city;
	}

	/**
	 * @return the gasStationPrices
	 */
	public Set<GasStationPrices> getGasStationPrices() {
		return this.gasStationPrices;
	}

	/**
	 * @return the houseNumber
	 */
	public int getHouseNumber() {
		return this.houseNumber;
	}

	/**
	 * @return the id
	 */
	public java.util.UUID getId() {
		return this.id;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return this.latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return this.longitude;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the postalCode
	 */
	public String getPostalCode() {
		return this.postalCode;
	}

	/**
	 * @return the streetName
	 */
	public String getStreetName() {
		return this.streetName;
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
	 * @param gasStationPrices
	 *            the gasStationPrices to set
	 */
	public void setGasStationPrices(Set<GasStationPrices> gasStationPrices) {
		this.gasStationPrices = gasStationPrices;
	}

	/**
	 * @param houseNumber
	 *            the houseNumber to set
	 */
	public void setHouseNumber(int houseNumber) {
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

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GasStation [id=" + this.id + ", name=" + this.name + ", brand=" + this.brand + ", streetName="
				+ this.streetName + ", houseNumber=" + this.houseNumber + ", postalCode=" + this.postalCode + ", city="
				+ this.city + ", latitude=" + this.latitude + ", longitude=" + this.longitude + ", gasStationPrices="
				+ this.gasStationPrices + "]";
	}

}
