package plotter.pdf;

import java.io.Serializable;

public class Metadata implements Serializable {

	private static final long serialVersionUID = -88545128542505562L;

	private int numberOfPages;
	private String printSize;
	private boolean isEncrypted;
	private int copies;
	private String jobKey;

	public Metadata() {}

	public Metadata(int numberOfPages, String printSize) {
		this.numberOfPages = numberOfPages;
		this.printSize = printSize;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	public String getPrintSize() {
		return printSize;
	}

	public void setPrintSize(String printSize) {
		this.printSize = printSize;
	}

	public boolean isEncrypted() {
		return isEncrypted;
	}

	public void setEncrypted(boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}

	public int getCopies() {
		return copies;
	}

	public void setCopies(int copies) {
		this.copies = copies;
	}

	/**
	 * @return the jobKey
	 */
	public String getJobKey() {
		return jobKey;
	}

	/**
	 * @param jobKey the jobKey to set
	 */
	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}
}
