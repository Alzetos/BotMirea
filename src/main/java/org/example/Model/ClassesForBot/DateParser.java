package org.example.Model.ClassesForBot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {

    public static LocalDate parse(String message) {
        String lowerMessage = message.toLowerCase();
        LocalDate today = LocalDate.now();

        if (lowerMessage.contains("сегодня")) return today;
        if (lowerMessage.contains("послезавтра")) return today.plusDays(2);
        if (lowerMessage.contains("завтра")) return today.plusDays(1);

        Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})");
        Matcher matcher = pattern.matcher(lowerMessage);

        if(matcher.find()) {
            try {
                String daysStr = matcher.group(1);
                return LocalDate.parse(daysStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (NumberFormatException e) {return null;}
        }
        Pattern pattern1 =  Pattern.compile("через\\s+(\\d+)\\s+д");
        Matcher matcher1 = pattern1.matcher(lowerMessage);

        if (matcher1.find()) {
            try {
                String daysStr = matcher1.group(1);
                int days = Integer.parseInt(daysStr);
                return today.plusDays(days);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
