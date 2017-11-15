package carefuel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Parser {

	private File file;

	public Parser(File file) {
		// to-do
		this.file = file;
	}

	/**
	 * Hauptfunktion des Parser. Die Datei wird zeilenweise durchgegangen und das
	 * ComModel um neue Informationen erg�nzt.
	 */
	public void parse() {
		System.out.println("************** Parser starts ***********************");

		String line = "";
		String[] entry;
		String splitBy = ";";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			while ((line = reader.readLine()) != null) {

				entry = line.split(splitBy);

				for (int i = 0; i < entry.length; i++) {
					System.out.println("Entry " + i + " " + entry[i]);
				}
			}

			reader.close();
			System.out.println("************** Parser ends ***********************");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
