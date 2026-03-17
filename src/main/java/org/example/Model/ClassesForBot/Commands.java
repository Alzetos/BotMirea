package org.example.Model.ClassesForBot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Commands {
    private final ProcessingData data = new ProcessingData();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    public String getSubjectReport(String subjectName) {
        List<String[]> tasks = data.getTasksBySubject(subjectName);

        if (tasks.isEmpty()) {
            return "По предмету \"" + subjectName + "\" задач не найдено.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(subjectName).append("</b>\n\n");

        for (String[] task : tasks) {

            String taskName = task[1];
            String deadlineStr = task[2];
            String grade = task.length > 3 ? task[3] : "-";
            if (deadlineStr.equalsIgnoreCase("Дедлайн") ||
                    deadlineStr.equalsIgnoreCase("Deadline") ||
                    deadlineStr.equalsIgnoreCase("Срок")) {
                continue;
            }

            sb.append("<b>").append(taskName).append("</b>\n");
            sb.append("Баллы: ").append(grade).append("\n");
            appendDeadlineInfo(sb, deadlineStr);
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getNearestDeadlinesReport() {
        LocalDate currentTime = LocalDate.now();
        List<String[]> tasks = data.getNearestTasks(currentTime);

        if(tasks.isEmpty()) {return "Задач нет";}

        StringBuilder sb = new StringBuilder();
        sb.append("<b>Ближайшие дедлайны:</b>\n\n");

        int count = 0;
        for(String[] task : tasks) {
            if (count >=10 ) {break;}

            String subject = task[0];
            String taskName = task[1];
            String deadlineStr = task[2];

            sb.append("<b>").append(subject).append("</b>\n");
            sb.append("<b>").append(taskName).append("</b>\n");
            appendDeadlineInfo(sb, deadlineStr);
            sb.append("\n");
            count++;
        }
        return sb.toString();
    }

    public String getDeadlinesUntilDate(LocalDate targetDate) {
        List<String[]> tasks = data.getTasksOnDate(targetDate);

        if (tasks.isEmpty()) {
            return "До " + targetDate.format(DateTimeFormatter.ofPattern("dd.MM")) + " дедлайнов нет.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<b>Дедлайны до ").append(targetDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).append(":</b>\n\n");

        for (String[] task : tasks) {
            String subject = task[0];
            String taskName = task[1];
            String deadlineStr = task[2];

            sb.append("<b>").append(subject).append("</b>\n");
            sb.append("").append(taskName).append("\n");
            appendDeadlineInfo(sb, deadlineStr);
            sb.append("\n");
        }
        return sb.toString();
    }

    private void appendDeadlineInfo(StringBuilder sb, String deadlineStr){
        LocalDate currentTime = LocalDate.now();
        try {
            LocalDate deadlineDate = LocalDate.parse(deadlineStr, dateTimeFormatter);
            long difference = ChronoUnit.DAYS.between(currentTime, deadlineDate);

            if (deadlineDate.isBefore(currentTime)) {
                sb.append("<b>Просрочено на </b> ").append(Math.abs(difference)).append(" дней\n");
            } else if (deadlineDate.isEqual(currentTime)) {
                sb.append("<b>Срок сдачи сегодня</b>\n");
            } else {
                sb.append("<b>До сдачи осталось </b> ").append(difference).append(" дней. Дата сдачи ").append(deadlineStr).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.append("\n");
    }
}
