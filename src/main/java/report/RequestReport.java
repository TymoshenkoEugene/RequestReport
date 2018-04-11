package report;

import net.company.requesttest.ExcelHelper;
import net.company.requesttest.FilesHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

public class RequestReport {
    public static void createReport(LinkedHashMap<Integer, LinkedHashMap<Integer, String>> reportData) throws IOException, InvalidFormatException {
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;

        String currentDateFormat = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HH:mm:ss"));

        String pathFrom = "./target/classes/RequestReportTemplate.xlsx";
        String pathTo = "./target/RequestReport_" + currentDateFormat + ".xlsx";
        String pathToTemp = "./target/RequestReport_" + currentDateFormat + "_tmp.xlsx";

        try {
            FilesHelper.copyFile(pathFrom, pathToTemp);
            workbook = ExcelHelper.getWorkbook(pathToTemp);
            sheet = ExcelHelper.getSheet(workbook, 0);

            for (Integer rowNum : reportData.keySet()) {
                ExcelHelper.fillRow(sheet, rowNum, reportData.get(rowNum));
            }
            ExcelHelper.saveWorkbook(workbook, pathTo);
            (new File(pathToTemp)).delete();
        } finally {

            sheet = null;


            if (workbook != null) {
                workbook.close();
                workbook = null;
            }
        }

    }
}
