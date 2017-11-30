/**
 *
 */
package carefuel.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * @author wolfg
 *
 */
@Entity
@Table(name = "distances")
public class Distance implements Serializable {

	@Id
	@Type(type = "pg-uuid")
	@Column(name = "id_1", unique = true, updatable = false)
	private UUID id_1;

	@Id
	@Type(type = "pg-uuid")
	@Column(name = "id_2", unique = true, updatable = false)
	private UUID id_2;

	@Column(name = "distance")
	private double distance;

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return this.distance;
	}

	/**
	 * @return the id_1
	 */
	public UUID getId_1() {
		return this.id_1;
	}

	/**
	 * @return the id_2
	 */
	public UUID getId_2() {
		return this.id_2;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}

	/**
	 * @param id_1
	 *            the id_1 to set
	 */
	public void setId_1(UUID id_1) {
		this.id_1 = id_1;
	}

	/**
	 * @param id_2
	 *            the id_2 to set
	 */
	public void setId_2(UUID id_2) {
		this.id_2 = id_2;
	}

}
