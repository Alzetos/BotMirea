package org.example.Model.Readers;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReaderCSV {
    private static String fileName = "Deadlines.csv";

    public static List<String[]> readAllLines() {
        List<String[]> rows = new ArrayList<>();

        try {
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    .build();
            CSVReader reader = new CSVReaderBuilder(new FileReader(fileName))
                    .withCSVParser(parser)
                    .build();


            String[] nextLine;
            while ((nextLine = reader.readNext()) != null){
                rows.add(nextLine);
            }
            reader.close();
        } catch (IOException | CsvValidationException e ){
            e.printStackTrace();
        }
        return rows;
    }
}
