package carefuel.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

	public void test() {
		List<String> testIDs = Arrays.asList("001975cc-d534-4819-ab35-8e88848c3096",
				"005056ba-7cb6-1ed2-bceb-60191af70d1b", "005056ba-7cb6-1ed2-bceb-7a1cf1468d26",
				"005056ba-7cb6-1ed2-bceb-7ddfa374cd2a", "005056ba-7cb6-1ed2-bceb-97d3e2bc8d3b",
				"005056ba-7cb6-1ed2-bceb-b776305dcd4a", "005056ba-7cb6-1ed2-bceb-bc577a5e6d4e",
				"005056ba-7cb6-1ed5-a6a1-930ae49a5411", "0055bbb5-2c30-4cb9-89f4-d937cf121770",
				"0173fd22-174a-41f2-9e0d-004b7da5b74f", "01a2529b-da12-40db-9e07-8a6646b54ddb",
				"0263adda-28e0-415c-97f7-dac62ebbf179", "026e1252-db5d-459f-944a-15081d9d2c60",
				"0271f0fc-65ed-4725-a922-57eec615195b", "028687da-704e-5500-8a63-3223f4a589f8",
				"03697a19-4dec-4562-9264-bb8cd4d19fb2", "057827cd-6583-4a3a-a461-f8546073fd02",
				"066f69fb-c215-418b-9177-89e6ea5e1654", "072c35bc-349b-4ffa-852e-5ac20d98c211",
				"0a5f98bf-3dd4-4bf3-bd09-57c1cfa9b397", "0b7b03b2-feb8-4c6d-9e65-053003a7e450",
				"0f269d49-b196-49c4-b21d-df11a40cd3ff", "0fb89c36-de83-46c7-9526-fbfef75bea3a");

		List<UUID> testUUIDs = testIDs.stream().map(idString -> UUID.fromString(idString)).collect(Collectors.toList());
		log.info(testUUIDs);

		// Methode 1 - alles in einem
		double startTime = System.currentTimeMillis();
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Query spSQLQuery = session.createQuery("FROM GasStation G WHERE G.id IN (:param1)");
		spSQLQuery.setParameterList("param1", testUUIDs);

		Set<GasStation> gasStations = (Set<GasStation>) spSQLQuery.list().stream().collect(Collectors.toSet());

		session.getTransaction().commit();

		double runTime = System.currentTimeMillis() - startTime;
		log.info("#1: RunTime: " + (runTime / 1000.0));

		// Methode 2 - für alles ne Session etc...
		final double startTime2 = System.currentTimeMillis();
		Session session2 = sessionFactory.openSession();
		List<Boolean> finished = new ArrayList<>();
		for (UUID id : testUUIDs) {
			new Thread() {
				@Override
				public void run() {

					Query spSQLQuery2 = session2.createQuery("FROM GasStation G WHERE G.id = :param1");
					spSQLQuery2.setParameter("param1", id);

					Set<GasStation> gasStation = (Set<GasStation>) spSQLQuery2.list().stream()
							.collect(Collectors.toSet());

					finished.add(Boolean.TRUE);
					if (finished.size() == testUUIDs.size()) {
						double runTime = System.currentTimeMillis() - startTime2;
						log.info("#2: RunTime: " + (runTime / 1000.0));
					}
				}
			}.start();

		}

		// wait until the threads are about to be run
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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
