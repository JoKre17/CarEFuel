package carefuel.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import carefuel.model.GasStation;

/**
 * 
 * @author Wolfgang
 * 
 *         class to parse the given CSV data
 * 
 */
public class InputParser {

	private static final String[] FILE_HEADER_MAPPING_GASSTATIONS = { "id", "name", "brand", "streetName", "houseNumber",
			"postalCode", "city", "latitude", "longitude" };
	
	private static final String[] FILE_HEADER_MAPPING_SINGLE_GASSTATION = { "id", "name", "brand", "streetName", "houseNumber",
			"postalCode", "city", "latitude", "longitude" };

	public InputParser() {

	}

	/**
	 * loads a CSV file and extracts all gas stations and returns a list of it
	 * 
	 * @param fileName
	 *            path to the file
	 * @return list of gas stations
	 */
	public List<GasStation> getAllGasStationsFromCSV(String fileName) {
		Reader csvReader = null;
		CSVParser csvParser = null;
		List<GasStation> list = new LinkedList<>();
		List<CSVRecord> csvRecords;
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING_GASSTATIONS).withDelimiter(';');

		try {
			csvReader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			// log.error("Encoding exception");
			return list;
		} catch (FileNotFoundException e2) {
			// log.error("File " + fileName + " not found");
			return list;
		}

		try {
			csvParser = new CSVParser(csvReader, csvFileFormat);
		} catch (IOException e) {
			// log.error("File \"" + fileName + "\" cannot be parsed! Aborting!");
			return list;
		}

		try {
			csvRecords = csvParser.getRecords();
		} catch (IOException e1) {
			// log.error("File \"" + fileName + "\" error on reading! Aborting!");
			return list;
		}

		for (CSVRecord csvRecord : csvRecords) {
			int houseNumber = 0;
			try {
				houseNumber = Integer.parseInt(csvRecord.get("houseNumber"));
			} catch (java.lang.NumberFormatException e) {
				// log.error("no house number present - set to 0");
			}

			try {
				long id = Long.parseLong(csvRecord.get("id"));
				double latitude = Double.parseDouble(csvRecord.get("latitude"));
				double longitude = Double.parseDouble(csvRecord.get("longitude"));
				list.add(new GasStation(id, csvRecord.get("name"), csvRecord.get("brand"), csvRecord.get("streetName"),
						houseNumber, csvRecord.get("postalCode"), csvRecord.get("city"), latitude, longitude));
			} catch (java.lang.NumberFormatException e) {
				// log.error("false data on necessary field - skipping entry: " +
				// csvRecord.toString());
				break;
			}
		}

		return list;
	}

}
