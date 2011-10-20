package plotter.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;

import plotter.storage.DocumentDAO;
import plotter.entities.Document;

public class Export {

	public static File xlsExport(List<Integer> documentId) throws IOException {

		final int COLUMNS = 6;

		// receive document objects
		List<Document> documents = new ArrayList<Document>();
		for (Integer id : documentId)
			documents.add(DocumentDAO.getDocumentById(id.toString()));

		// prepare XLS file
		HSSFWorkbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		HSSFSheet sheet = wb.createSheet("Plotter");

		// setting header
		HSSFRow header = sheet.createRow(0);
		HSSFCellStyle headerStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(font);
		headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		header.createCell(0).setCellValue("Benutzer");
		header.createCell(1).setCellValue("Datum");
		header.createCell(2).setCellValue("Format");
		header.createCell(3).setCellValue("Kopien");
		header.createCell(4).setCellValue("Seiten");
		header.createCell(5).setCellValue("Preis");

		for (Iterator<Cell> cells = header.cellIterator(); cells.hasNext();)
			cells.next().setCellStyle(headerStyle);

		// setting formats
		DataFormat dataFormat = wb.createDataFormat();
		CellStyle numberStyle = wb.createCellStyle();
		CellStyle priceStyle = wb.createCellStyle();
		numberStyle.setDataFormat(dataFormat.getFormat("0"));
		priceStyle.setDataFormat(dataFormat.getFormat("0.00"));

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(
				"dd.mm.yy h:mm"));

		// assemble rows
		for (int i = 1; i < documents.size() + 1; i++) {
			Document document = documents.get(i - 1);
			HSSFRow row = sheet.createRow(i);
			for (int j = 0; j < COLUMNS; j++) {
				HSSFCell cell = row.createCell(j);
				Object value = getValue(j, document);
				if (value instanceof Date) {
					cell.setCellValue((Date) value);
					cell.setCellStyle(dateStyle);
				} else if (value instanceof Double) {
					cell.setCellStyle(numberStyle);
					cell.setCellValue(new Double(value.toString()));
				} else if (value instanceof Float) {
					cell.setCellStyle(priceStyle);
					cell.setCellValue(new Double(value.toString()));
				} else
					cell.setCellValue(value.toString());

			}
		}

		// setting automatic width for first two columns
		sheet.autoSizeColumn((short) 0);
		sheet.autoSizeColumn((short) 1);

		// setting Footer
		HSSFFooter footer = sheet.getFooter();
		footer.setRight("Seite " + HSSFFooter.page() + " von "
				+ HSSFFooter.numPages());

		// File file = new File("workbook.xls");
		File file = null;

		// throws the IOException on error
		file = File.createTempFile("export", ".xls");
		file.deleteOnExit();
		wb.write(FileUtils.openOutputStream(file));

		return file;

	}

	/**
	 * @param cell
	 * @param document
	 * @return
	 */
	private static Object getValue(int cell, Document document) {

		if (cell == 0)
			return document.getUser().getFirstName() + " "
					+ document.getUser().getLastName();
		else if (cell == 1)
			return document.getPrintDate();
		else if (cell == 2)
			return document.getFormat();
		else if (cell == 3)
			return new Double(document.getPageCount());
		else if (cell == 4)
			return new Double(document.getCopies());
		else if (cell == 5)
			return new Float(document.getPrice().toString());

		return new String("Feld nicht gefunden!");
	}
}
