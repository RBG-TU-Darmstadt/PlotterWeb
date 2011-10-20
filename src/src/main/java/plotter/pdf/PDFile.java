package plotter.pdf;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import plotter.connection.Manager;

public class PDFile extends File {

	private static final long serialVersionUID = 1578101753406396888L;

	private UUID id = UUID.randomUUID();

	private PDFDocument doc;
	private ArrayList<File> images;
	private final int MAX_HEIGHT = 120;
	private final int MAX_WIDTH = 120;
	private Metadata meta;
	private String filename;

	public PDFile(String filename, String originalFileName) throws IOException {
		super(filename);

		this.filename = originalFileName;

		// load the PDF Document
		this.doc = new PDFDocument();
		this.doc.load(this);

		// set the Metadata
		this.meta = new Metadata();

		// initialize images Array
		this.images = new ArrayList<File>();

		// Create preview images
//		try {
//			this.convertToImages();
//		} catch (RendererException e) {
//			throw new IOException(e);
//		} catch (DocumentException e) {
//			throw new IOException(e);
//		}
	}

	/**
	 * Convert PDF to preview images
	 * 
	 * @throws DocumentException 
	 * @throws RendererException 
	 * @throws IOException 
	 */
	public void convertToImages() throws IOException, RendererException, DocumentException {
		SimpleRenderer renderer = new SimpleRenderer();
		renderer.setResolution(72);

		List<Image> images = renderer.render(this.doc);

		this.meta.setNumberOfPages(images.size());

		for (int i = 0; i < images.size(); i++) {
			// Retrieve a temporary file
			File tmp = File.createTempFile(
					"img" + i + "_" + this.getName(), ".png");
			tmp.deleteOnExit();

			RenderedImage image = (RenderedImage) images.get(i);

			// compute thumbnail dimensions
			int width = MAX_WIDTH;
			int height = MAX_HEIGHT;
			float scale_width = (float) MAX_WIDTH / (float) image.getWidth();
			float scale_height = (float) MAX_HEIGHT / (float) image.getHeight();
			if (scale_width < scale_height)
				height = Math.round(image.getHeight() * scale_width);
			else
				width = Math.round(image.getWidth() * scale_height);

			// scale image to thumbnail
			Image scaledImage = ((Image) image).getScaledInstance(width,
					height, BufferedImage.SCALE_SMOOTH);
			BufferedImage scaledBufferedImage = new BufferedImage(width,
					height, BufferedImage.TYPE_INT_RGB);
			scaledBufferedImage.getGraphics().drawImage(scaledImage, 0, 0,
					null);

			// Write image
			ImageIO.write(scaledBufferedImage, "png", tmp);
			this.images.add(tmp);
		}
	}

	/**
	 * This method sends this PDF to the plotter, for details see
	 * {@link Manager#sendFile()}.
	 * 
	 * @return boolean true on success, otherwise false
	 */
	public boolean sendFileToPrinter() {
		Manager manager = new Manager();
		return manager.sendFile(this);
	}

	public UUID getId() {
		return id;
	}

	public List<File> getImages() {
		return images;
	}

	public int getHeight() {
		return MAX_HEIGHT;
	}

	public int getWidth() {
		return MAX_WIDTH;
	}

	public Metadata getMetadata() {
		return meta;
	}

	public String getFilename() {
		return filename;
	}

	public JSONObject toJSON() throws JSONException {
		return new JSONObject().put("name", this.getFilename()).put("pages",
				this.meta.getNumberOfPages());
	}
}