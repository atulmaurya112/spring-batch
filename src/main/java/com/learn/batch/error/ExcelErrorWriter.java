package com.learn.batch.error;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelErrorWriter {

	public static ByteArrayOutputStream generate(List<FailedRecord> failed) throws IOException {
		try (Workbook wb = new XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("Failed");

			CellStyle errorStyle = wb.createCellStyle();
			errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
			errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CreationHelper helper = wb.getCreationHelper();
			Drawing<?> drawing = sheet.createDrawingPatriarch();

			int rowIdx = 0;

			rowIdx = createHeader(failed, sheet, rowIdx);

			for (FailedRecord fr : failed) {
				Row row = sheet.createRow(rowIdx++);
				int col = 0;

				for (Map.Entry<String, String> e : fr.getRow().entrySet()) {

					Cell cell = row.createCell(col);
					cell.setCellValue(e.getValue() == null ? "" : e.getValue());

					if (!fr.getErrors().isEmpty()) {
						cell.setCellStyle(errorStyle);

						if (col == 0) {
							Comment comment = drawing.createCellComment(helper.createClientAnchor());
							comment.setString(helper.createRichTextString(String.join(", ", fr.getErrors())));
							cell.setCellComment(comment);
						}
					}

					col++;
				}
			}

			if (!failed.isEmpty()) {
				int colCount = failed.get(0).getRow().size();
				for (int i = 0; i < colCount; i++) {
					sheet.autoSizeColumn(i);
				}
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			return out;
		}
	}

	private static int createHeader(List<FailedRecord> failed, Sheet sheet, int rowIdx) {
		if (!failed.isEmpty()) {
			Row header = sheet.createRow(rowIdx++);
			int h = 0;
			for (String key : failed.get(0).getRow().keySet()) {
				header.createCell(h++).setCellValue(key);
			}
		}
		return rowIdx;
	}
	
}