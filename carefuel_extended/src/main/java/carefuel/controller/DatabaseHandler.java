package carefuel.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.data.util.Pair;

import carefuel.model.GasStation;
import carefuel.model.GasStationPricePrediction;

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

	public void exit() {
		// code to close Hibernate Session factory
		this.sessionFactory.close();
	}

	@SuppressWarnings("unchecked")
	public Set<GasStation> getAllGasStations() {
		Set<GasStation> gasStations = new HashSet<>();

		Session session = this.sessionFactory.openSession();
		session.beginTransaction();

		Query query = session.createQuery("from " + GasStation.class.getSimpleName());
		gasStations = (Set<GasStation>) query.list().stream().collect(Collectors.toSet());

		session.getTransaction().commit();

		return gasStations;
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
		Query query = session.createQuery("from " + GasStation.class.getSimpleName() + " where id='" + uuid + "'");
		gasStation = (GasStation) query.uniqueResult();

		session.getTransaction().commit();
		return gasStation;
	}

	/**
	 * returns a sorted list of pairs that map dates to prices of a specified
	 * fueltype
	 *
	 * @param id
	 * @param fuel
	 * @return
	 */
	public List<Pair<Date, Integer>> getPricePrediction(String id, Enum<Fuel> fuel) {
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();

		org.hibernate.Query query = session
				.createQuery("from " + GasStationPricePrediction.class.getSimpleName() + " where stid='" + id + "'");

		@SuppressWarnings("unchecked")
		List<GasStationPricePrediction> temp = query.list();

		session.getTransaction().commit();

		List<Pair<Date, Integer>> toReturn = new ArrayList<>();

		if (fuel.equals(Fuel.DIESEL)) {
			toReturn = temp.stream().map(x -> Pair.of(x.getDate(), x.getDiesel())).collect(Collectors.toList());
		} else if (fuel.equals(Fuel.E5)) {
			toReturn = temp.stream().map(x -> Pair.of(x.getDate(), x.getE5())).collect(Collectors.toList());
		} else if (fuel.equals(Fuel.E10)) {
			toReturn = temp.stream().map(x -> Pair.of(x.getDate(), x.getE10())).collect(Collectors.toList());
		}

		Collections.sort(toReturn, new Comparator<Pair<Date, Integer>>() {
			@Override
			public int compare(Pair<Date, Integer> o1, Pair<Date, Integer> o2) {
				return o1.getFirst().compareTo(o2.getFirst());
			}
		});

		return toReturn;
	}

	/**
	 * truncates the whole prediction table and inserts all predictions of the set
	 * of predictions
	 *
	 * @param predictedPrices
	 *            all predicted prices of all gas stations
	 */
	public void insertPricePredictions(Set<GasStationPricePrediction> predictedPrices) {

		try {
			truncateTable(GasStationPricePrediction.tableName);

			// insert new predictions
			Session session = this.sessionFactory.getCurrentSession();
			session.beginTransaction();
			for (GasStationPricePrediction predicted : predictedPrices) {
				session.save(predicted);
			}
			session.getTransaction().commit();
		} catch (NullPointerException e) {
			log.error("NullPointerException");
			log.error("No predicted Prices to insert, refusing!");
		}

	}

	/**
	 * loads configuration from hibernate.cfg.xml and sets up the session
	 */
	public void setup() {
		// code to load Hibernate Session factory
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
		try {
			this.sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
			this.sessionFactory.openSession();
		} catch (Exception ex) {
			log.error("Error setting up the database connection!");
			log.error(ex);
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	/**
	 * Truncates a database table
	 *
	 * @param tableName
	 *            table to truncate
	 */
	private void truncateTable(String tableName) {
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session.createSQLQuery("truncate " + tableName);
		query.executeUpdate();
		session.getTransaction().commit();
	}
}
