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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(distance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((id_1 == null) ? 0 : id_1.hashCode());
		result = prime * result + ((id_2 == null) ? 0 : id_2.hashCode());
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
		if (!(obj instanceof Distance)) {
			return false;
		}
		Distance other = (Distance) obj;
		if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance)) {
			return false;
		}
		if (id_1 == null) {
			if (other.id_1 != null) {
				return false;
			}
		} else if (!id_1.equals(other.id_1)) {
			return false;
		}
		if (id_2 == null) {
			if (other.id_2 != null) {
				return false;
			}
		} else if (!id_2.equals(other.id_2)) {
			return false;
		}
		return true;
	}

}
