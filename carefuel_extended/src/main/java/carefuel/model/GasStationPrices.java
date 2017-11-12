/**
 *
 */
package carefuel.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Wolfgang
 *
 */
@Entity
@Table(name = "gas_station_information_history")
public class GasStationPrices {

	@ManyToOne
	private GasStation gasStation;
	@Id
	@Column(name = "id")
	private long id;
	@Temporal(TemporalType.DATE)
	private Date date;
	@Column(name = "e5")
	private double e5;
	@Column(name = "e10")
	private double e10;
	@Column(name = "diesel")
	private double diesel;

	public GasStationPrices() {

	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * @return the diesel
	 */
	public double getDiesel() {
		return this.diesel;
	}

	/**
	 * @return the e10
	 */
	public double getE10() {
		return this.e10;
	}

	/**
	 * @return the e5
	 */
	public double getE5() {
		return this.e5;
	}

	/**
	 * @return the gasStation
	 */
	public GasStation getGasStation() {
		return this.gasStation;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @param diesel
	 *            the diesel to set
	 */
	public void setDiesel(double diesel) {
		this.diesel = diesel;
	}

	/**
	 * @param e10
	 *            the e10 to set
	 */
	public void setE10(double e10) {
		this.e10 = e10;
	}

	/**
	 * @param e5
	 *            the e5 to set
	 */
	public void setE5(double e5) {
		this.e5 = e5;
	}

	/**
	 * @param gasStation
	 *            the gasStation to set
	 */
	public void setGasStation(GasStation gasStation) {
		this.gasStation = gasStation;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GasStationPrices [gasStation=" + this.gasStation + ", id=" + this.id + ", date=" + this.date + ", e5="
				+ this.e5 + ", e10=" + this.e10 + ", diesel=" + this.diesel + "]";
	}

}
