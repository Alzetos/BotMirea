package org.example.Model.ClassesForBot;

import org.example.Model.Readers.ReaderCSV;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProcessingData {
    private final List<String[]> rawData = ReaderCSV.readAllLines();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Set<String> getUniqueSubjects() {
        Set<String> subjects = new HashSet<>();
        for (String[] row : rawData) {
            if (row.length > 0) {
                String subject = row[0].replaceAll("\uFEFF", "").trim();
                if (!subject.equalsIgnoreCase("Предмет") && !subject.isEmpty()) {
                    subjects.add(subject);
                }
            }
        }
        return subjects;
    }

    public List<String[]> getTasksBySubject(String subjectName) {
        List<String[]> res = new ArrayList<>();

        for (String[] row : rawData) {
            if (row.length > 0) {
                String rowSubject = row[0].replaceAll("\uFEFF", "").trim();
                if (rowSubject.equalsIgnoreCase(subjectName.trim())) {
                    res.add(row);
                }
            }
        }
        return res;
    }

    public List<String[]> getNearestTasks(LocalDate currentTime) {
        List<String[]> sortedTasks = new ArrayList<>();

        for (String[] row : rawData) {
            if (row.length < 3) continue;
            String dateStr = row[2].trim();

            if (dateStr.equals("-") || dateStr.isEmpty() ||
                    dateStr.equalsIgnoreCase("Дедлайн") ||
                    dateStr.equalsIgnoreCase("Deadline")) {
                continue;
            }
            try {
                LocalDate deadline = LocalDate.parse(dateStr, dateTimeFormatter);
                if (!deadline.isBefore(currentTime)) {
                    sortedTasks.add(row);
                }
            } catch (Exception e) {
            }
        }
        sortedTasks.sort(new Comparator<String[]>() {
            @Override
            public int compare(String[] row1, String[] row2) {
                LocalDate date1 = LocalDate.parse(row1[2].trim(), dateTimeFormatter);
                LocalDate date2 = LocalDate.parse(row2[2].trim(), dateTimeFormatter);
                return date1.compareTo(date2);
            }
        });
        return sortedTasks;
    }

    public ArrayList<String> processingLines() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String[] row : rawData) {
            if (row.length < 3) continue;
            arrayList.addAll(Arrays.asList(row));
        }
        return arrayList;
    }

    public List<String[]> getTasksOnDate(LocalDate target) {
        List<String[]> res = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (String[] row : rawData) {
            if (row.length < 3) {
                continue;
            }

            String dateStr = row[2].trim();
            if (dateStr.equals("-") || dateStr.isEmpty() ||
                    dateStr.equalsIgnoreCase("Дедлайн") ||
                    dateStr.equalsIgnoreCase("Deadline")) continue;

            try {
                LocalDate deadline = LocalDate.parse(dateStr, dateTimeFormatter);
                if (!deadline.isAfter(target) && !deadline.isBefore(today)) {
                    res.add(row);
                }
            } catch (Exception e) {
            }
        }
        res.sort((r1, r2) -> {
            try {
                return LocalDate.parse(r1[2].trim(), dateTimeFormatter)
                        .compareTo(LocalDate.parse(r2[2].trim(), dateTimeFormatter));
            } catch (Exception e) {
                return 0;
            }
        });
        return res;
    }
}
