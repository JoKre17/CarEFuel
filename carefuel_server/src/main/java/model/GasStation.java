package model;

import javax.persistence.*;

/**
 * Entity class to represent the table GASSTATION in the database
 * @author Wolfgang
 *
 */
@Entity
@Table(name = "GASSTATION")
public class GasStation {
	private long id;
	private String name;
	private String brand;
	private String streetName;
	private int houseNumber;
	private String postalCode;
	private String city;
	private double latitude;
	private double longitude;
	
	/**
	 * the default constructor is necessary for hibernate to get all gas stations from the database
	 */
	public GasStation() {
		
	}
	
	/**
	 * constructor to be used after gathering data
	 * @param id
	 * @param name
	 * @param brand
	 * @param streetName
	 * @param houseNumber
	 * @param postalCode
	 * @param city
	 * @param latitude
	 * @param longitude
	 */
	public GasStation(long id, String name, String brand, String streetName, int houseNumber,
			String postalCode, String city, double latitude, double longitude) {
		setId(id);
		setName(name);
		setBrand(brand);
		setStreetName(streetName);
		setHouseNumber(houseNumber);
		setPostalCode(postalCode);
		setCity(city);
		setLatitude(latitude);
		setLongitude(longitude);
	}
	
	@Id //tells hibernate that this is the primary key
	@Column(name = "ID") //maps to the database table
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getStreetName() {
		return streetName;
	}
	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}
	public int getHouseNumber() {
		return houseNumber;
	}
	public void setHouseNumber(int houseNumber) {
		this.houseNumber = houseNumber;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "GasStation [id=" + id + ", name=" + name + ", brand=" + brand + ", streetName=" + streetName
				+ ", houseNumber=" + houseNumber + ", postalCode=" + postalCode + ", city=" + city + ", latitude="
				+ latitude + ", longitude=" + longitude + "]";
	}
}
