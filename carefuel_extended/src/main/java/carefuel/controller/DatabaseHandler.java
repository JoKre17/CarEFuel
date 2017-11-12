package carefuel.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import carefuel.model.GasStation;

/**
 *
 * This class manages the database containing all information about gas stations
 * and prices and offers functionality to request and update data.
 *
 */
public class DatabaseHandler {

	private static final Logger log = LogManager.getLogger(Main.class);

	protected SessionFactory sessionFactory;

	/**
	 * GasStations will be created in the DB, if they are not already created
	 *
	 * @param list
	 *            of GasStations
	 *
	 */
	public void createGasStations(List<GasStation> list) {
		// code to save a gas station
		Session session = this.sessionFactory.openSession();
		session.beginTransaction();

		for (GasStation gas : list) {
			session.saveOrUpdate(gas);
		}

		session.getTransaction().commit();
	}

	/**
	 * closes the session
	 */
	public void exit() {
		// code to close Hibernate Session factory
		this.sessionFactory.close();
	}

	/**
	 * return a gas station by id
	 *
	 * @param uuid
	 * @return
	 */
	public GasStation getGasStation(String uuid) {
		GasStation gasStation;

		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		org.hibernate.Query query = session
				.createQuery("from " + GasStation.class.getName() + " where id='" + uuid + "'");
		gasStation = (GasStation) query.uniqueResult();

		session.getTransaction().commit();
		return gasStation;
	}

	/**
	 * loads configuration from hibernate.cfg.xml and sets up the session
	 */
	public void setup() {
		// code to load Hibernate Session factory
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure() // configures settings
																									// from
																									// hibernate.cfg.xml
				.build();
		try {
			this.sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
			this.sessionFactory.openSession();
		} catch (Exception ex) {
			ex.printStackTrace();
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}
}
