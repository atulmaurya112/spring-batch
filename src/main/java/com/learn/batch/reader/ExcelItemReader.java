package com.learn.batch.reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.EmptyFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;

public class ExcelItemReader implements ItemReader<Map<String, String>>, ItemStream {

	private final String filePath;

	private Workbook workbook;
	private Iterator<Row> iterator;
	private List<String> headers;

	public ExcelItemReader(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public void open(ExecutionContext executionContext) {
		try {
			workbook = new XSSFWorkbook(new FileInputStream(filePath));
			Sheet sheet = workbook.getSheetAt(0);
			
			iterator = sheet.iterator();
			if (!iterator.hasNext()) {
				throw new EmptyFileException();
			}

			headers = readHeaders(iterator.next());
		} catch (Exception e) {
			throw new RuntimeException("Error opening Excel", e);
		}
	}

	private List<String> readHeaders(Row row) {
		List<String> list = new ArrayList<>();
		DataFormatter formatter = new DataFormatter();

		for (Cell cell : row) {
			list.add(formatter.formatCellValue(cell));
		}

		return list;
	}

	@Override
	public @Nullable Map<String, String> read() {
		if (iterator == null || !iterator.hasNext()) {
			return null;
		}

		Row row = iterator.next();
		Map<String, String> map = new HashMap<>();

		DataFormatter formatter = new DataFormatter();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		for (int i = 0; i < headers.size(); i++) {
			Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
			String value = "";
			if (cell != null) {
				if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
					value = cell.getLocalDateTimeCellValue().toLocalDate().format(dateFormatter);
				} else {
					value = formatter.formatCellValue(cell);
				}
			}

			map.put(headers.get(i), value);
		}
		return map;
	}

	@Override
	public void update(ExecutionContext executionContext) {
		// optional (restart support)
	}

	@Override
	public void close() {
		try {
			if (workbook != null) {
				workbook.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Error closing workbook", e);
		}
	}
	
}