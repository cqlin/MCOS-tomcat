package org.remchurch.mealservice.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class XlsRender {

	private Workbook workbook = new HSSFWorkbook();
	private CellStyle headerStyle = workbook.createCellStyle();

	/**
	 * Constructor to initiate workbook and header styles
	 */
	public XlsRender() {

		// Init header style
		Font headerFont = workbook.createFont();
		headerFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
		headerFont.setBold(true);
		headerFont.setFontName("Calibri");
		headerFont.setFontHeightInPoints((short)12);

		headerStyle.setFont(headerFont);
		headerStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
	}

	/**
	 * @param userDetailsList
	 * @param sheetName
	 * Header and Data values are written to the excel file
	 */
	public void renderReport(List<Map<String, Object>> dataList,String sheetName,List<String> headValueList) {
		int colCounter = 0;
		int rowCounter = 0;
		Sheet sheet = workbook.createSheet(sheetName);
		Row row = sheet.createRow(rowCounter++);
		for(String header : headValueList)
		{
			renderHeaderCell(row, header, colCounter++);
		}
		//Data fill
		for (Map<String, Object> m:dataList)
		{
			row = sheet.createRow(rowCounter++);
			colCounter = 0;
			for(Entry<String, Object> e:m.entrySet()) {
				row.createCell(colCounter++).setCellValue(String.valueOf(e.getValue()));
			}
		}
		// Auto resize all columns
		for(int i=0; i<colCounter; i++) {
			sheet.autoSizeColumn(i);
		}	
	}

	/**
	 * @param row
	 * @param cellText
	 * @param cellIndex
	 * Used for rendering header cells for the workbook
	 */
	private void renderHeaderCell(Row row, String cellText, int cellIndex){
		Cell cell = row.createCell(cellIndex);
		cell.setCellValue(cellText);
		cell.setCellStyle(headerStyle);
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	public File writeToFile(String reportFileName) throws IOException {
		File reportFile = new File(reportFileName);
		try(FileOutputStream fileOut = new FileOutputStream(reportFile)){
			workbook.write(fileOut);
			workbook.close();
		}
		return reportFile;
	}
}
