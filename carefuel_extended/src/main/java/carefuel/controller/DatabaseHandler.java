package carefuel.controller;

import java.util.LinkedList;
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

	final static Logger log = LogManager.getLogger(DatabaseHandler.class);
	protected SessionFactory sessionFactory;

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
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		} catch (Exception ex) {
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	/**
	 * closes the session
	 */
	public void exit() {
		// code to close Hibernate Session factory
		sessionFactory.close();
	}

	/**
	 * GasStations will be created in the DB, if they are not already created
	 * 
	 * @param list
	 *            of GasStations
	 * 
	 */
	public void createGasStations(List<GasStation> list) {
		// code to save a gas station
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		for (GasStation gas : list) {
			session.saveOrUpdate(gas);
		}

		session.getTransaction().commit();
		session.close();
	}

	/**
	 * returns all GasStations in the database in a list
	 * 
	 * @return all GasStations
	 */
	@SuppressWarnings({ "unchecked" })
	public List<GasStation> getAllGasStations() {
		List<GasStation> gasStations = new LinkedList<>();

		Session session = sessionFactory.openSession();
		gasStations = session.createCriteria(GasStation.class).list();
		System.out.println(gasStations.get(0));
		return gasStations;
	}
}
