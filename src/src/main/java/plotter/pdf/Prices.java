package plotter.pdf;

import java.util.HashMap;
import java.util.Map;

import plotter.util.Configuration;

public class Prices {

	private static Prices instance = null;

	private Map<String, Float> prices;

	public static Prices getInstance() {
		if (instance == null)
			instance = new Prices();
		return instance;
	}

	public Prices() {
		this.prices = new HashMap<String, Float>();

		// speichere Daten in HashMap
		for (Map.Entry<String, String> entry : Configuration.getPropertiesWithPrefix("plotter.price.").entrySet()) {
			prices.put(entry.getKey(), Float.valueOf(entry.getValue()));
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
