package carefuel.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import carefuel.model.GasStation;
import carefuel.model.GasStationPrice;
import carefuel.model.GasStationPricePrediction;
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

/**
 * The PricePredictor class enables the user to predict the prices of a gas
 * station up to one month in the future. It uses a pretrained tensorflow model
 * that needs to be located in carefuel_basic/rnn_model.
 *
 * @author nils
 *
 */
public class PricePredictor {
	private static final Logger log = LogManager.getLogger(PricePredictor.class);

	private Session session;
	private Graph graph;
	private String modelPath;
	private final int hoursPerMonth = 372;
	private final int maxPrevMonths = 50; // All gas stations have a maximum of 50 previous months of entries

	/**
	 * TODO add functionality to decide between CSV files and database as data
	 * source
	 * 
	 * Uses the System property gasPricesDirectory to get the path to the
	 * input_files
	 */
	public PricePredictor() {
		// Set file path
		modelPath = System.getProperty("user.dir") + "/rnn_model/";

		// Initialize TensorFlow session and graph with pretrained model
		SavedModelBundle bundle = SavedModelBundle.load(modelPath, "serve");
		session = bundle.session();
		graph = bundle.graph();

	}

	/**
	 * This function uses the neural network in order to predict all prices of a
	 * given gas station for the next month using all previous prices.
	 *
	 * @param gasStation
	 *            GasStation object containing all prices
	 * @return A set of of price predictions for the next month of 'gasStation'.
	 * @throws Exception
	 */
	public Set<GasStationPricePrediction> predictNextMonth(GasStation gasStation){
		// First get a sorted list of historic prices for all fuel types with corresponding date
		ArrayList<ArrayList<Pair<Date, Integer>>> datePriceList = getHistoricPrices(gasStation);

		/*
		 * Interpolate the data from the first to the entry at lastEntryIndex at every
		 * hour The array interpolatedPrices is of the form [?][nHoursPerMonth], where ?
		 * is the number of "whole" months, i.e. every month that still has 31 days á 24
		 * hours of entries. Note that the array is in reversed form, meaning it goes
		 * back in time. Therefore, interpolatedPrices[0][0] contains the last price of
		 * the month before the month that needs to be predicted,
		 * interpolatedPrices[0][1] the price before that and so on.
		 */
		float[][] interpolatedPricesE5 = interpolatePrices(datePriceList.get(0));
		float[][] interpolatedPricesE10 = interpolatePrices(datePriceList.get(1));
		float[][] interpolatedPricesDiesel = interpolatePrices(datePriceList.get(2));

		// Predict prices for all fuel type using the neural network
		float[] predictionE5 = runNetwork(interpolatedPricesE5);
		float[] predictionE10 = runNetwork(interpolatedPricesE10);
		float[] predictionDiesel = runNetwork(interpolatedPricesDiesel);

		// We need the last date as starting point for the prediction
		Date currentDate = datePriceList.get(0).get(0).getLeft();

		// Wrap all price predictions into objects of the GasStationPricePrediction class
		Set<GasStationPricePrediction> predictions = new HashSet<>();
		for(int i = 0; i < predictionE5.length; ++i){
			GasStationPricePrediction prediction = new GasStationPricePrediction(gasStation.getId(), currentDate,
					(int) predictionE5[i], (int) predictionE10[i], (int) predictionDiesel[i]);
			prediction.setGasStation(gasStation);
			predictions.add(prediction);
		}

		return predictions;
	}

	/**
	 * This function extracts the historic prices of all fuel types from the given GasStation objects.
	 *
	 * @return An array of array lists containing all prices with corresponding dates, sorted by date. The array
	 * has a length of three, where array[0] is the entry for E5, array[1] for E10 and array[2] for Diesel
	 */
	private ArrayList<ArrayList<Pair<Date, Integer>>> getHistoricPrices(GasStation gasStation){
		//Fetch all historic price data and sort by date and fuel type
		Set<GasStationPrice> prices = gasStation.getGasStationPrices();
		ArrayList<Pair<Date, Integer>> historicE5 = new ArrayList<>();
		ArrayList<Pair<Date, Integer>> historicE10 = new ArrayList<>();
		ArrayList<Pair<Date, Integer>> historicDiesel = new ArrayList<>();
		for(GasStationPrice price : prices){
			historicE5.add(Pair.of(price.getDate(), price.getE5()));
			historicE10.add(Pair.of(price.getDate(), price.getE10()));
			historicDiesel.add(Pair.of(price.getDate(), price.getDiesel()));
		}

		Comparator<Pair<Date, Integer>> comp = Comparator.comparing(Pair::getLeft);
		historicE5.sort(comp);
		historicE10.sort(comp);
		historicDiesel.sort(comp);

		ArrayList<ArrayList<Pair<Date, Integer>>> result = new ArrayList<>();
		result.add(historicE5);
		result.add(historicE10);
		result.add(historicDiesel);
		return result;
	}

	/**
	 * This function interpolates the data given by 'datePriceList' at every hour
	 * linearly.
	 *
	 * @param datePriceList
	 *            should contain a list of ordered dates and prices from the
	 *            beginning to the last entry.
	 *
	 * @return Returns a two dimensional array of interpolated data. The first index
	 *         corresponds to the month and second to the hour of the month,
	 *         resulting in the shape [?][nHoursPerMonth]. The data is in reversed
	 *         order and begins at the last entry of datePriceList
	 */
	private float[][] interpolatePrices(List<Pair<Date, Integer>> datePriceList) {
		// Create the data points for the interpolation
		double[] x = new double[datePriceList.size()];
		double[] y = new double[datePriceList.size()];

		// Iterate over all entries in reverse order
		Date lastDate = datePriceList.get(datePriceList.size() - 1).getLeft();
		for (int i = datePriceList.size() - 1; i >= 0; i--) {
			Date currentDate = datePriceList.get(i).getLeft();
			int currentPrice = datePriceList.get(i).getRight();

			long diff = (lastDate.getTime() - currentDate.getTime()); // difference in milliseconds;

			// Just in case two dates are added at the same time, slightly move the point to the right for one ms
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


		/* Use the function to interpolate data at every two hours of each month, beginning */

		// Calculate the number of 'whole' months contained in the data
		long diff_hours = (lastDate.getTime() - datePriceList.get(0).getLeft().getTime()) / (3600 * 1000);
		int nMonths = (int) diff_hours / (2 * hoursPerMonth);

		// First interpolation point
		double current_x = 0;

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
	 * This function takes the interpolated historic prices of any fuel type and predicts the prices of the next month
	 * by running the neural network.
	 * @return the prices of the next month every two hours
	 */
	private float[] runNetwork(float[][] interpolatedHistoricPrices){
		/*
		 * The network expects exactly maxPrevMonths = 50 entries in the first dimension
		 * of the input tensor (even though not all data from the first months may be
		 * accessible). Therefore, fill in the according number of zero arrays
		 */
		int nPrevMonths = interpolatedHistoricPrices.length;
		int nMissingEntries = maxPrevMonths - nPrevMonths;
		float[][] zeros = new float[nMissingEntries][hoursPerMonth];
		float[][] combinedInput = ArrayUtils.addAll(interpolatedHistoricPrices, zeros);

		// Add another dimension (the network expects three dimensions, due to batches)
		float[][][] input = { combinedInput };

		// Create the first input tensor of shape (1, 50, hoursPerMonth) containing the previous months
		Tensor<Float> prevMonthsTensor = Tensor.create(input, Float.class);

		// Also create an input tensor for the number of previous months of shape (1, 1)
		// -> network avoids using the padded inputs
		int[] nPrevMonthsArray = { nPrevMonths };
		Tensor<Integer> nPrevMonthsTensor = Tensor.create(nPrevMonthsArray, Integer.class);

		// Fetch the output tensor of the network
		@SuppressWarnings("rawtypes")
		Output output = graph.operation("Output/rescaled_output").output(0);

		// Feed the input tensors and run the TensorFlow graph
		float[][] result = new float[1][hoursPerMonth];
		session.runner().feed("Input/prev_months", prevMonthsTensor)
				.feed("Input/n_prev_months", nPrevMonthsTensor)
				.fetch(output).run().get(0).copyTo(result);
		return result[0];
	}
}