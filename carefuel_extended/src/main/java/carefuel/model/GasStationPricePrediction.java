/**
 *
 */
package carefuel.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Wolfgang
 *
 */
@Entity
@Table(name = "gas_station_information_prediction")
public class GasStationPricePrediction {

	public static final String tableName = "gas_station_information_prediction";
	@ManyToOne
	@JoinColumn(name = "stid", insertable = true, updatable = false)
	private GasStation gasStation;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	@Column(name = "e5")
	private int e5;
	@Column(name = "e10")
	private int e10;

	@Column(name = "diesel")
	private int diesel;

	public GasStationPricePrediction() {

	}

	/**
	 * @param date
	 * @param e5
	 * @param e10
	 * @param diesel
	 */
	public GasStationPricePrediction(GasStation gasStation, Date date, int e5, int e10, int diesel) {
		super();
		this.gasStation = gasStation;
		this.date = date;
		this.e5 = e5;
		this.e10 = e10;
		this.diesel = diesel;
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
	public int getDiesel() {
		return this.diesel;
	}

	/**
	 * @return the e10
	 */
	public int getE10() {
		return this.e10;
	}

	/**
	 * @return the e5
	 */
	public int getE5() {
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
	public void setDiesel(int diesel) {
		this.diesel = diesel;
	}

	/**
	 * @param e10
	 *            the e10 to set
	 */
	public void setE10(int e10) {
		this.e10 = e10;
	}

	/**
	 * @param e5
	 *            the e5 to set
	 */
	public void setE5(int e5) {
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
		return "GasStationPricePrediction [id=" + this.id + ", date=" + this.date + ", e5=" + this.e5 + ", e10="
				+ this.e10 + ", diesel=" + this.diesel + "]";
	}
}
