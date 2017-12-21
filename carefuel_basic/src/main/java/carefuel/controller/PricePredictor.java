package carefuel.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import carefuel.app.App;

/**
 * The PricePredictor class enables the user to predict the prices of a gas
 * station up to one month in the future. It uses a pretrained tensorflow model
 * that needs to be located in carefuel_basic/rnn_model.
 *
 * @author nils
 *
 */
public class PricePredictor {
	private static final Logger log = LogManager.getLogger(App.class);

	private Session session;
	private Graph graph;
	private String modelPath;
	private String gasPricesDirectory;
	private final int hoursPerMonth = 372;
	private final int maxPrevMonths = 50; // All gas stations have a maximum of
	// 50 previous months of entries

	/**
	 * TODO add functionality to decide between CSV files and database as data
	 * source
	 *
	 * Uses the System property gasPricesDirectory to get the path to the
	 * input_files
	 */
	public PricePredictor() {
		// Set file paths
		modelPath = System.getProperty("user.dir") + "/rnn_model/";
		// Default is the directory above
		gasPricesDirectory = System.getProperty("user.dir") + "/../input_files/gasprices/";
		if (System.getProperty("gasPricesDir") != null) {
			gasPricesDirectory = System.getProperty("gasPricesDir");
		}

		// Initialize TensorFlow session and graph with pretrained model
		SavedModelBundle bundle = SavedModelBundle.load(modelPath, "serve");
		session = bundle.session();
		graph = bundle.graph();

	}

	/**
	 * This function returns a list of all dates and corresponding prices of a
	 * single gas station with the ID gasStationID.
	 *
	 * @ToDo add functionality to decide between CSV files and database as source
	 * @return the according list of dates and prices or null if file cannot be
	 *         opened
	 */
	private ArrayList<Pair<Date, Integer>> getGasPricesToID(final int gasStationID) throws Exception {
		try {
			/**** Parse from CSV files for now ****/
			// Open directory and find matching files
			File csvDirectory = new File(gasPricesDirectory);
			if (!csvDirectory.exists()) {
				log.error("Could not find directory with gas price csv files");
				log.error("Consider using the parameter -gasPricesDir PATH/TO/GASPRICES_DIRECTORY");
				throw new Exception();
			}
			File[] matches = csvDirectory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.equals(Integer.toString(gasStationID) + ".csv");
				}
			});

			// Exactly one file should be found
			if (matches.length != 1) {
				log.error("Could not find gas price csv file in directory");
				throw new Exception();
			}
			File csvFile = matches[0];

			// Try to parse the csv file
			CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), CSVFormat.newFormat(';'));
			List<CSVRecord> csvEntries = parser.getRecords();
			parser.close();

			// Parse all entries
			ArrayList<Pair<Date, Integer>> result = new ArrayList<Pair<Date, Integer>>();
			for (CSVRecord entry : csvEntries) {
				String dateString = entry.get(0);
				String priceString = entry.get(1);
				Date date = parseDateString(dateString);
				int price = Integer.parseInt(priceString);
				Pair<Date, Integer> pair = Pair.of(date, price);
				result.add(pair);
			}
			return result;
		} catch (Exception e) {
			// If any error occurs throw exception
			log.error("Error while parsing CSV file");
			throw new Exception();
		}
	}

	/**
	 * This function interpolates the data given by 'datePriceList' at every hour
	 * linearly.
	 *
	 * @param datePriceList
	 *            should contain a list of ordered dates and prices from the
	 *            beginning to the last entry, where the last entry has to be
	 *            slightly AFTER 'maxDate'
	 * @param maxDate
	 *            is the last date that can be used for the prediction
	 *
	 * @return Returns a two dimensional array of interpolated data. The first index
	 *         corresponds to the month and second to the hour of the month,
	 *         resulting in the shape [?][nHoursPerMonth]. The data is in reversed
	 *         order and begins at maxDate.
	 */
	private float[][] interpolatePrices(List<Pair<Date, Integer>> datePriceList, Date maxDate) {
		// Create the data points for the interpolation
		double[] x = new double[datePriceList.size()];
		double[] y = new double[datePriceList.size()];

		Date lastDate = datePriceList.get(datePriceList.size() - 1).getLeft();
		for (int i = datePriceList.size() - 1; i >= 0; i--) {
			Date currentDate = datePriceList.get(i).getLeft();
			int currentPrice = datePriceList.get(i).getRight();
			long diff = (lastDate.getTime() - currentDate.getTime()); // difference
			// in
			// milliseconds;

			// Just in case two dates are added at the same time, slightly move
			// the point to
			// the right for one ms
			if (i < (datePriceList.size() - 1)) {
				if (diff == x[datePriceList.size() - i - 2]) {
					diff += 1;
				}
			}
			x[datePriceList.size() - i - 1] = diff;
			y[datePriceList.size() - i - 1] = currentPrice;
		}

		// Create the interpolating function from data points
		PolynomialSplineFunction func = new LinearInterpolator().interpolate(x, y);

		/*
		 * Use the function to interpolate data at every our of each month, beginning
		 * with 'maxDate'
		 */

		// Calculate the number of 'whole' months contained in the data
		long diff_hours = (maxDate.getTime() - datePriceList.get(0).getLeft().getTime()) / (3600 * 1000); // number
		// of
		// hours
		// contained
		int nMonths = (int) diff_hours / (2 * hoursPerMonth);

		// First interpolation point
		double current_x = lastDate.getTime() - maxDate.getTime();

		// Iterate over all months
		float[][] result = new float[nMonths][hoursPerMonth];
		for (int month = 0; month < nMonths; ++month) {
			for (int hour = 0; hour < hoursPerMonth; ++hour) {
				result[month][hour] = (float) func.value(current_x);
				current_x += (1000 * 3600 * 2);
			}
		}
		return result;
	}

	/**
	 * This function takes a string representing a single date and time and parses
	 * it into a java Date object. The string is expected to be in the format
	 * "yyyy-MM-dd HH:mm:ssz". It is important to notice that the offset from the
	 * GMT, indicated by the formatter z, is expected to be in the format +02, as it
	 * is in the data base and CSV files and not +0200 as it would be commonly used.
	 *
	 * @throws ParseException
	 */
	private Date parseDateString(String dateString) throws ParseException {
		// Append neccessary zeros at the end of the String
		dateString += "00";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz");
		Date date = dateFormat.parse(dateString);
		return date;
	}

	/**
	 * Convenience function. Calls predictNextMonth and calculates the price at
	 * predictionDate. See predictNextMonth for more information.
	 *
	 * @return the predicted price in cent * 10 at predictionDate.
	 */
	public int predictPrice(String maxDateString, String predictionDateString, int gasStationID) throws Exception {
		// Predict the prices at every hour of the next month since 'maxDate'
		float[] prices = predictNextMonth(maxDateString, gasStationID);

		// Check if something went wrong
		if (prices == null) {
			log.error("Something went wrong during price prediction");
			throw new Exception();
		}

		Date maxDate = parseDateString(maxDateString);
		Date predictionDate = parseDateString(predictionDateString);

		// Get the number"2017-08-21 23:03:06+02" of hours from maxDate and
		// predictionDate
		double nHours = (predictionDate.getTime() - maxDate.getTime()) / ((double) 3600 * 1000 * 2);

		// Interpolate the value using the previous and next hour
		int prevHour = (int) nHours;
		int nextHour = prevHour + 1;
		if (prevHour < 0) {
			return Math.round(prices[nextHour]);
		}
		if (nextHour > hoursPerMonth) {
			return Math.round(prices[prevHour]);
		}

		double m = (prices[nextHour] - prices[prevHour]) / (double) (nextHour - prevHour);
		double b = prices[nextHour] - (m * nextHour);
		double price = (m * nHours) + b;
		return (int) Math.round(price);
	}

	/**
	 * This function uses the neural network in order to predict all prices of a
	 * given gas station using for the next month since 'maxDate'. It uses all
	 * previous data before 'maxDate'. Note that 'maxDate' has to be BEFORE the last
	 * entry in the corresponding data set of the gas station.
	 *
	 * @param maxDateString
	 *            beginning of the month at which to predict
	 * @param gasStationID
	 *            ID of the corresponding gas station
	 * @return the gasprices of the next month at every hour after maxDateString.
	 *         The first entry contains the price exactly one hour after
	 *         maxDateString
	 * @throws Exception
	 */
	public float[] predictNextMonth(String maxDateString, int gasStationID) throws Exception {
		// First load all entries from the gas station and return null if an
		// error
		// occured
		ArrayList<Pair<Date, Integer>> datePriceList = getGasPricesToID(gasStationID);

		// Next, find the first entry that is in the next month
		Date maxDate;
		try {
			maxDate = parseDateString(maxDateString);
		} catch (ParseException e) {
			log.error("Error while parsing maxDateString");
			throw new Exception();
		}
		int firstEntryIndex = 0;
		for (int i = 0; i < datePriceList.size(); ++i) {
			// Find the first element that is not before maxdate
			Date currentDate = datePriceList.get(i).getLeft();
			if (!currentDate.before(maxDate)) {
				firstEntryIndex = i;
			}
		}

		/*
		 * Interpolate the data from the first to the entry at lastEntryIndex at every
		 * hour The array interpolatedPrices is of the form [?][nHoursPerMonth], where ?
		 * is the number of "whole" months, i.e. every month that still has 31 days á 24
		 * hours of entries. Note that the array is in reversed form, meaning it goes
		 * back in time. Therefore, interpolatedPrices[0][0] contains the last price of
		 * the month before the month that needs to be predicted,
		 * interpolatedPrices[0][1] the price before that and so on.
		 */
		float[][] interpolatedPrices = interpolatePrices(datePriceList.subList(0, firstEntryIndex + 1), maxDate);

		/*
		 * The network expects exactly maxPrevMonths = 50 entries in the first dimension
		 * of the input tensor (even though not all data from the first months may be
		 * accessible). Therefore, fill in the according number of zero arrays
		 */
		int nPrevMonths = interpolatedPrices.length;
		int nMissingEntries = maxPrevMonths - nPrevMonths;
		float[][] zeros = new float[nMissingEntries][hoursPerMonth];
		float[][] combinedInput = ArrayUtils.addAll(interpolatedPrices, zeros);

		// Add another dimension (the network expects three dimensions, due to
		// batches)
		float[][][] input = { combinedInput };

		// Create the first input tensor of shape (1, 50, hoursPerMonth) containing the
		// previous
		// months
		Tensor<Float> prevMonthsTensor = Tensor.create(input, Float.class);

		// Also create an input tensor for the number of previous months of
		// shape (1, 1)
		// -> network avoids using the padded inputs
		int[] nPrevMonthsArray = { nPrevMonths };
		Tensor<Integer> nPrevMonthsTensor = Tensor.create(nPrevMonthsArray, Integer.class);

		// Fetch the output tensor of the network
		@SuppressWarnings("rawtypes")
		Output output = graph.operation("Output/rescaled_output").output(0);

		// Feed the input tensors and run the TensorFlow graph
		float[][] result = new float[1][hoursPerMonth];
		session.runner().feed("Input/prev_months", prevMonthsTensor).feed("Input/n_prev_months", nPrevMonthsTensor)
		.fetch(output).run().get(0).copyTo(result);
		return result[0];
	}
}