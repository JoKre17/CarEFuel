package carefuel.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import carefuel.model.GasStation;
import carefuel.model.GasStationPrice;
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
		session.close();

		return gasStations;
	}

	/**
	 * retrieves all IDs of gasStations. Used to avoid memory overflow while
	 * iterating over all gasStations with lazy relation loading
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<UUID> getAllGasStationIDs() {
		Set<UUID> gasStationIDs = new HashSet<>();

		Session session = this.sessionFactory.openSession();
		session.beginTransaction();

		Query query = session.createQuery("select id from " + GasStation.class.getSimpleName());
		gasStationIDs = (Set<UUID>) query.list().stream().collect(Collectors.toSet());
		session.getTransaction().commit();
		session.close();

		return gasStationIDs;
	}

	/**
	 * return a gas station by id
	 *
	 * @param uuid
	 * @return
	 */
	public GasStation getGasStation(String uuid) {
		GasStation gasStation;

		Session session = this.sessionFactory.openSession();
		session.beginTransaction();
		Query query = session.createQuery("from " + GasStation.class.getSimpleName() + " where id='" + uuid + "'");
		gasStation = (GasStation) query.uniqueResult();
		session.getTransaction().commit();
		session.close();

		return gasStation;
	}

	/**
	 * return a gas station by id
	 *
	 * @param uuid
	 * @return
	 */
	public GasStation getGasStation(UUID uuid) {
		GasStation gasStation;

		Session session = this.sessionFactory.openSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from " + GasStation.class.getSimpleName() + " where id='" + uuid.toString() + "'");
		gasStation = (GasStation) query.uniqueResult();
		session.getTransaction().commit();
		session.close();

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
	public List<Pair<Date, Integer>> getPricePrediction(UUID uuid, Enum<Fuel> fuel) {
		Session session = this.sessionFactory.openSession();
		session.beginTransaction();

		org.hibernate.Query query = session.createQuery(
				"from " + GasStationPricePrediction.class.getSimpleName() + " where stid='" + uuid.toString() + "'");

		@SuppressWarnings("unchecked")
		List<GasStationPricePrediction> temp = query.list();
		session.getTransaction().commit();
		session.close();

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
				return o1.getLeft().compareTo(o2.getLeft());
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
			// insert new predictions
			Session session = this.sessionFactory.openSession();
			session.beginTransaction();
			for (GasStationPricePrediction predicted : predictedPrices) {
				session.save(predicted);
			}
			session.getTransaction().commit();
			session.close();
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
		} finally {
			this.sessionFactory.getCurrentSession().close();
		}
	}

	/**
	 * Truncates a database table
	 *
	 * @param tableName
	 *            table to truncate
	 */
	public void truncateTable(String tableName) {
		Session session = this.sessionFactory.openSession();
		session.beginTransaction();
		Query query = session.createSQLQuery("truncate " + tableName);
		query.executeUpdate();
		session.getTransaction().commit();
		session.close();
	}

	/**
	 * retrieves all historic prices for a given uuid of a gasStation
	 * 
	 * @param uuid
	 *            gasStation id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<List<Pair<Date, Integer>>> getGasStationPrices(UUID uuid) {

		List<GasStationPrice> prices = new ArrayList<>();
		Session session = this.sessionFactory.openSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from " + GasStationPrice.class.getSimpleName() + " where stid='" + uuid + "'");
		prices = query.list();
		session.getTransaction().commit();
		session.close();

		ArrayList<Pair<Date, Integer>> historicE5 = new ArrayList<>();
		ArrayList<Pair<Date, Integer>> historicE10 = new ArrayList<>();
		ArrayList<Pair<Date, Integer>> historicDiesel = new ArrayList<>();
		for (GasStationPrice price : prices) {
			if (price.getE5() > 0) {
				historicE5.add(Pair.of(price.getDate(), price.getE5()));				
			}
			if (price.getE10() > 0) {
				historicE10.add(Pair.of(price.getDate(), price.getE10()));				
			}
			if (price.getDiesel() > 0) {
				historicDiesel.add(Pair.of(price.getDate(), price.getDiesel()));				
			}
		}
		
		Comparator<Pair<Date, Integer>> comp = Comparator.comparing(Pair::getLeft);
		historicE5.sort(comp);
		historicE10.sort(comp);
		historicDiesel.sort(comp);

		List<List<Pair<Date, Integer>>> result = new ArrayList<>();
		result.add(historicE5);
		result.add(historicE10);
		result.add(historicDiesel);

		return result;
	}
}
