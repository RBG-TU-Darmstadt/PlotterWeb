package plotter.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Prices {

	private final String PRICES_FILENAME = "prices.properties";
	private static Prices instance = null;
	private Map<String, Float> prices;

	public static Prices getInstance() {
		if (instance == null)
			instance = new Prices();
		return instance;
	}

	public Prices() {
		this.prices = new HashMap<String, Float>();

		// lade Properties Datei
		Properties props = new Properties();
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream(PRICES_FILENAME);
		try {
			props.load(is);
		} catch (IOException e) {
			// TODO handle Exception
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {}
		}

		// speichere Daten in HashMap
		for (String p : props.stringPropertyNames()) {
			prices.put(p, Float.valueOf(props.getProperty(p)));
		}
	}

	public Map<String, Float> getPrices() {
		return prices;
	}

	public float calculatePrice(int pages, int copies, String format) throws FormatException {
		if( ! prices.containsKey(format))
			throw new FormatException();

		return prices.get(format) * pages * copies;
	}

}
