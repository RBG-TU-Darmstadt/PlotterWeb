package plotter.servlet;

import java.util.Map;

/**
 * Object to share information between the Process class and it's JSP page
 */
public class ProcessDAO {

	private String firstName;
	private String lastName;

	private Map<String, Float> formats;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPrice(String format) {
		return String.format("%.2f", formats.get(format));
	}

	public void setFormats(Map<String, Float> formats) {
		this.formats = formats;
	}

}
