package carefuel.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.tensorflow.*;

public class PricePredictor {
	
	private File gasPricesDirectory; //Points to the directory containing all gas prices
	
	public PricePredictor(){
		SavedModelBundle bundle =
				SavedModelBundle.load("/home/nils/Uni/InformatiCup/rnn_training/models/model", "serve");
		Session sess = bundle.session();
		Graph g = bundle.graph();

		for(int i = 0; i < 500; ++i)
		{
			float[][][] matrix = new float[1][50][744];
			Tensor<Float> prevMonthsTensor = Tensor.create(matrix, Float.class);
			
			int[] nPrevMonths = {50};
			Tensor<Integer> nPrevMonthsTensor = Tensor.create(nPrevMonths, Integer.class);
			Output output = g.operation("output").output(0);
			
			List<Tensor<?>> bla = sess.runner().feed("Teeeest/prev_months", prevMonthsTensor)
					.feed("Teeeest/n_prev_months", nPrevMonthsTensor)
					.fetch(output).run();
			
			float[][] result = new float[1][744];
			bla.get(0).copyTo(result);
			
			System.out.println(i);
		}		
	}
	
	public int predictPrice(String maxDate, String predictionDate, int gasStationID)
	{
		
	}
	
	public ArrayList<DatePricePair> predictNextMonth(String maxDate, int gasStationID)
	{
		
	}
	
	
	/**
	 * This function returns a list of all dates and corresponding prices of a single
	 * gas station with the ID gasStationID.
	 * @ToDo add functionality to decide between CSV files and database as source
	 */
	private ArrayList<DatePricePair> getGasPricesToID(int gasStationID)
	{
		
	}
	
	
	/**
	 * This function takes a string representing a single date and time
	 * and parses it into a java Date object. The string is expected to
	 * be in the format "YYYY-MM-dd Tz". It is important to notice that
	 * the offset from the GMT, indicated by the formatter z, is expected
	 * to be in the format +02, as it is in the data base and CSV files
	 * and not +0200 as it would be commonly used.
	 * @throws ParseException
	 */
	private Date parseDateString(String dateString) throws ParseException {
		// Append neccessary zeros at the end of the String
		dateString += "00";
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd Tz");
		return dateFormat.parse(dateString);
	}
	
	/**
	 * This class represents a single pair of a java date object and price at that time.
	 * (Thank you Java, for not providing a generic Pair class :-) ) Objects can be
	 * created by providing a date object and a price integer. The price
	 * is expected to be in cents * 10, as it is in the CSV files and database.
	 * @author nils
	 *
	 */
	public class DatePricePair{
		private Date date;
		private int price;
		
		public DatePricePair(Date date, int price){
			this.date = date;
			this.price = price;
		}
		
		public Date getDate() {
			return date;
		}
		
		public int getPrice() {
			return price;
		}
		
		void setPrice(int price) {
			this.price = price;
		}
		
		void setDate(Date date) {
			this.date = date;
		}
	}
}