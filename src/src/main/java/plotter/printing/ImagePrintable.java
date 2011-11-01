package plotter.printing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Printable interface to print multiple images at once.
 */
public class ImagePrintable implements Printable {

	/**
	 * Contains all images to print
	 */
	private List<BufferedImage> images = new ArrayList<BufferedImage>();

	/**
	 * @param image
	 *            the image to add
	 */
	public void addImage(BufferedImage image) {
		this.images.add(image);
	}

	/**
	 * @param index
	 *            the image to remove
	 */
	public void removeImages(int index) {
		this.images.remove(index);
	}

	/**
	 * @return the current number of pages
	 */
	public int getImagesCount() {
		return this.images.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.Printable#print(java.awt.Graphics,
	 * java.awt.print.PageFormat, int)
	 */
	public int print(Graphics graphics, PageFormat pf, int pageIndex)
			throws PrinterException {

		if (pageIndex >= images.size()) {
			return Printable.NO_SUCH_PAGE;
		}

		BufferedImage image = this.images.get(pageIndex);

		Graphics2D g2d = (Graphics2D) graphics;

		// Scale canvas to fit image
		g2d.translate(pf.getImageableX(), pf.getImageableY());
		double pageWidth = pf.getImageableWidth();
		double pageHeight = pf.getImageableHeight();
		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();
		double scaleX = pageWidth / imageWidth;
		double scaleY = pageHeight / imageHeight;
		double scaleFactor = Math.min(scaleX, scaleY);
		g2d.scale(scaleFactor, scaleFactor);

		// Draw image on canvas
	    g2d.drawImage(image, 0, 0, null);

		return Printable.PAGE_EXISTS;
	}
}
