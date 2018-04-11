package net.company.requesttest;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExcelHelper {
    public static XSSFWorkbook getWorkbook() {
        return new XSSFWorkbook();
    }

    public static XSSFWorkbook getWorkbook(String pathToFile) throws IOException, InvalidFormatException {
        return getWorkbook(new File(pathToFile));
    }

    public static XSSFWorkbook getWorkbook(File file) throws IOException, InvalidFormatException {
        return new XSSFWorkbook(file);
    }

    public static XSSFSheet getSheet(XSSFWorkbook workbook, String sheetName) {
        return workbook.getSheet(sheetName);
    }

    public static XSSFSheet getSheet(XSSFWorkbook workbook, Integer sheetIndex) {
        return workbook.getSheetAt(sheetIndex);
    }

    public static void fillRow(XSSFSheet sheet, Integer rowNum, LinkedHashMap<Integer, String> rowData) {
        Row currentRow = sheet.getRow(rowNum - 1);

        if (currentRow == null) {
            currentRow = sheet.createRow(rowNum - 1);
        }
        for (Map.Entry<Integer, String> entry : rowData.entrySet()) {
            Cell cell = currentRow.getCell(entry.getKey() - 1);
            if (cell == null) {
                cell = currentRow.createCell(entry.getKey() - 1);
            }


            cell.setCellValue(entry.getValue().toString());
        }
    }

    public static void saveWorkbook(XSSFWorkbook workbook, String pathToFile) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(pathToFile);
        workbook.write(outputStream);
        workbook.close();
    }
}