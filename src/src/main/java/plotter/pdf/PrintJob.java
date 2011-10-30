package plotter.pdf;

import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import plotter.printing.ImagePrintable;
import plotter.util.Configuration;

import com.lowagie.text.pdf.PdfReader;

/**
 * A print job
 */
public class PrintJob implements Serializable {

	private static final long serialVersionUID = 8295962761024419184L;

	private String filename;
	private File pdfFile;
	private int numberOfPages;
	private String printSize;
	private int copies;
	private Date printDate;

	private float pricePerPage;
	List<File> thumbnails = new ArrayList<File>();

	public PrintJob(String filename, String originalFileName) throws IOException {
		this.filename = originalFileName;
		this.pdfFile = new File(filename);

		this.numberOfPages = getPageCount();
	}

	/**
	 * Use iText to retrieve the number of pages of the PDF
	 * 
	 * @return the number of pages
	 * @throws IOException
	 */
	private int getPageCount() throws IOException {
		int pageCount = 0;

		ByteArrayInputStream bais = null;
		PdfReader reader = null;

		try {
			reader = new PdfReader(new FileInputStream(pdfFile));

			pageCount = reader.getNumberOfPages();
		} catch (Exception e) {
			throw new IOException("Could not retrieve page count.", e);
		} finally {
			if (reader != null)
				reader.close();
			IOUtils.closeQuietly(bais);
		}

		return pageCount;
	}

	/**
	 * Convert PDF to images
	 * 
	 * @param resolution
	 *            the resolution in dpi
	 * @param paperSize
	 *            the paper size (e.g. a2, a4, a10...)
	 * @param portrait
	 *            rotate the images to portrait format
	 * @return a list of converted image files
	 * @throws IOException
	 */
	private List<File> convertToImages(int resolution, String paperSize, boolean portrait) throws IOException {
		// Get temporary file names for images
		File tmp = File.createTempFile("plotter_%d_", ".png");

		ArrayList<String> command = new ArrayList<String>();

		// Build command
		command.add(Configuration.getProperty("plotter.ghostscript.executable"));
		command.add("-dQUIET");
		command.add("-dNOPAUSE");
		command.add("-dBATCH");
		command.add("-dSAFER");
		command.add("-sDEVICE=png16m");
		command.add("-dTextAlphaBits=4");
		command.add("-dGraphicsAlphaBits=4");
		command.add("-r" + resolution);
		command.add("-dPDFFitPage");
		command.add("-sPAPERSIZE=" + paperSize);
		if (portrait) {
			command.add("-dNORANGEPAGESIZE");
		}
		command.add("-sOutputFile=" + tmp.getAbsolutePath());
		command.add(pdfFile.getAbsolutePath());

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process proc = processBuilder.start();

		int exitCode;
		try {
			exitCode = proc.waitFor();
		} catch (InterruptedException e) {
			throw new IOException("PDF to image conversion interrupted.", e);
		}

		if (exitCode != 0) {
			String errorMessage = IOUtils.toString(proc.getErrorStream())
					+ " - " + IOUtils.toString(proc.getInputStream());

			throw new IOException(
					"PDF could not be converted to images, message: "
							+ errorMessage);
		}

		// Retrieve images
		List<File> images = new ArrayList<File>();
		for (int i = 1; i <= this.getNumberOfPages(); i++) {
			String imageFilename = tmp.getAbsolutePath().replace("%d",
					Integer.toString(i));

			images.add(new File(imageFilename));
		}

		tmp.delete();

		return images;
	}

	/**
	 * Generate 100dpi Din A10 thumbnails (results in 101x146px)
	 * 
	 * @throws IOException
	 */
	public void generateThumbnails() throws IOException {
		thumbnails = convertToImages(100, "a10", false);
	}
	
	/**
	 * Prints this pdf file on local printer.
	 * 
	 * @throws PrinterException 
	 *             on error while printing
	 * @throws PrinterIOException
	 *             on error creating or reading rendered pages
	 */
	public void print() throws PrinterException {
		MediaSizeName mediaSize = null;
		if (this.printSize.equals("A0")) {
			mediaSize = MediaSizeName.ISO_A0;
		} else if (this.printSize.equals("A1")) {
			mediaSize = MediaSizeName.ISO_A1;
		} else if (this.printSize.equals("A2")) {
			mediaSize = MediaSizeName.ISO_A2;
		}
		
		mediaSize = MediaSizeName.ISO_A4;

		PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
		printAttributes.add(new Copies(this.copies));
		printAttributes.add(mediaSize);

		PrintService printServices[] = PrintServiceLookup.lookupPrintServices(
				DocFlavor.INPUT_STREAM.PNG, printAttributes);

		// Select correct printer
		PrintService printService = null;
		for (PrintService ps : printServices) {
			if (ps.getName().contains(
					Configuration.getProperty("plotter.device.name"))) {
				printService = ps;
				break;
			}
		}

		if (printService == null) {
			throw new PrinterException("No suitable printer found.");
		}

		PrinterJob printerJob = PrinterJob.getPrinterJob();
		printerJob.setPrintService(printService);

		ImagePrintable imagePrintable = new ImagePrintable();
		try {
			List<File>  renderedPages = convertToImages(300, this.printSize, true);
			for (File file : renderedPages) {
				FileInputStream stream = new FileInputStream(file);
				imagePrintable.addImage(ImageIO.read(stream));
				stream.close();
			}
		} catch (IOException e) {
			throw new PrinterIOException(e);
		}

		printerJob.setPrintable(imagePrintable);
		printerJob.print(printAttributes);

		printDate = new Date();
	}

	public String getFilename() {
		return filename;
	}

	public List<File> getThumbnails() {
		return thumbnails;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public String getPrintSize() {
		return printSize;
	}

	public void setPrintSize(String printSize) throws FormatException {
		this.printSize = printSize;

		pricePerPage = Prices.getInstance().getPrice(this.getPrintSize());
	}

	public int getCopies() {
		return copies;
	}

	public void setCopies(int copies) throws CopiesException {
		if (copies < 1) {
			throw new CopiesException("Copies must be greater then zero.");
		}

		this.copies = copies;
	}

	public float getPrice() {
		return pricePerPage * getCopies() * getNumberOfPages();
	}

	public Date getPrintDate() {
		return printDate;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject object = new JSONObject();
		object.put("filename", this.getFilename());
		object.put("pages", this.getNumberOfPages());
		object.put("copies", this.getCopies());
		object.put("format", this.getPrintSize());
		object.put("price", this.getPrice());

		if (this.getPrintDate() != null) {
			object.put("date", this.getPrintDate().getTime());
			object.put("status", "complete");
		}

		return object;
	}

}