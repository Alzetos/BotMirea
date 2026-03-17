package org.example.Model.Readers;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class ReaderExcel {

    public static void main(String[] args) {
        String filePath = "Deadlines.xlsx";

        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)){
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    printCellValue(cell);
                    System.out.println("\t");
                }
                System.out.println();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void printCellValue(Cell cell){
        switch (cell.getCellType()){
            case STRING -> System.out.println(cell.getStringCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    System.out.println(cell.getLocalDateTimeCellValue());
                } else {
                    System.out.println(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> System.out.println(cell.getBooleanCellValue());
            case FORMULA -> System.out.println(cell.getCellFormula());
            default -> System.out.println("can't define");
        }
    }
}
