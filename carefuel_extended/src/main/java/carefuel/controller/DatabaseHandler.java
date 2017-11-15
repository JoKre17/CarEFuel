package carefuel.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.data.util.Pair;

import carefuel.model.GasStation;
import carefuel.model.GasStationPricePrediction;

/**
<<<<<<< HEAD
 * 
=======
 *
>>>>>>> 71fd5f6bfb2796d1afb20d921bd6915ae63393ba
 * This class manages the database containing all information about gas stations
 * and prices and offers functionality to request and update data.
 *
 */
public class DatabaseHandler {

<<<<<<< HEAD
	final static Logger log = LogManager.getLogger(DatabaseHandler.class);
=======
	private static final Logger log = LogManager.getLogger(Main.class);

>>>>>>> 71fd5f6bfb2796d1afb20d921bd6915ae63393ba
	protected SessionFactory sessionFactory;

	/**
	 * GasStations will be created in the DB, if they are not already created
	 *
	 * @param list
	 *            of GasStations
	 *
	 */
<<<<<<< HEAD
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
=======
	public void createGasStations(List<GasStation> list) {
		// code to save a gas station
		Session session = this.sessionFactory.openSession();
		session.beginTransaction();

		for (GasStation gas : list) {
			session.saveOrUpdate(gas);
>>>>>>> 71fd5f6bfb2796d1afb20d921bd6915ae63393ba
		}

		session.getTransaction().commit();
	}

	public void exit() {
		// code to close Hibernate Session factory
		this.sessionFactory.close();
	}

	/**
<<<<<<< HEAD
	 * GasStations will be created in the DB, if they are not already created
	 * 
	 * @param list
	 *            of GasStations
	 * 
=======
	 * return a gas station by id
	 *
	 * @param uuid
	 * @return
>>>>>>> 71fd5f6bfb2796d1afb20d921bd6915ae63393ba
	 */
	public GasStation getGasStation(String uuid) {
		GasStation gasStation;

		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
<<<<<<< HEAD

		for (GasStation gas : list) {
			session.saveOrUpdate(gas);
		}
=======
		org.hibernate.Query query = session
				.createQuery("from " + GasStation.class.getName() + " where id='" + uuid + "'");
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

		List<GasStationPricePrediction> temp = query.list();
>>>>>>> 71fd5f6bfb2796d1afb20d921bd6915ae63393ba

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

	// public TreeMap<Date, Double> getPricePrediction2(String id, Enum<Fuel> fuel)
	// {
	// TreeMap<Date, Double> prices = new TreeMap<>();
	//
	// GasStation gasStation = getGasStation(id);
	//
	// for (GasStationPricePrediction gspp :
	// gasStation.getGasStationPricePredictions()) {
	// prices.put(key, value)
	// }
	//
	// return prices;
	// }

	public void insertPricePredictions(List<GasStationPricePrediction> predictedPrices) {
		// TODO
		// truncate table
		// insert new objects
	}

	/**
<<<<<<< HEAD
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
=======
	 * loads configuration from hibernate.cfg.xml and sets up the session
	 */
	public void setup() {
		// code to load Hibernate Session factory
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure() // configures
																									// settings
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
>>>>>>> 71fd5f6bfb2796d1afb20d921bd6915ae63393ba
	}
}
