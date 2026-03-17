package org.example.Model;

import org.example.Model.ClassesForBot.Commands;
import org.example.Model.ClassesForBot.DateParser;
import org.example.Model.ClassesForBot.ProcessingData;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TelegramBot extends TelegramLongPollingBot {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final ProcessingData data = new ProcessingData();
    private final Commands command = new Commands();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Set<Long> users = new HashSet<>();

    public TelegramBot() {
        startDaily();
    }

    private void startDaily(){
        LocalDateTime now = LocalDateTime.now();

        int target_hour = 15;
        int target_minute = 0;
        int target_seconds = 0;
        LocalDateTime nextRun = now.withHour(target_hour)
                .withMinute(target_minute)
                .withSecond(target_seconds);
        if(now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        long initialTime = Duration.between(now, nextRun).getSeconds();
        scheduler.scheduleAtFixedRate(this::sendNotifications,
                initialTime,
                86400,
                TimeUnit.SECONDS);
    }

    @Override
    public String getBotUsername() {
        String bot_Name = "MIREA Deadline Bot";
        return bot_Name;
    }

    @Override
    public String getBotToken() {
        String bot_TOKEN = "";
        return bot_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        users.add(chatId);
        LocalDate requestedDate = DateParser.parse(messageText);

        if (requestedDate != null) {
            String report = command.getDeadlinesUntilDate(requestedDate);
            sendMessage(chatId, report);
        } else if (messageText.equals("/start") || messageText.equals("Назад")) {
            showMainMenu(chatId);
        } else if (messageText.equals("Ближайшие дедлайны")) {
            String report = command.getNearestDeadlinesReport();
            sendMessage(chatId, report);
        } else if(messageText.equals("Выбрать предмет")){
            showSubjectsMenu(chatId);
        } else if(messageText.equals("Написать запрос")){
            sendMessage(chatId,"Напишите ваш запрос текстом.\nПримеры:\n- <i>Что сдать завтра</i>\n- <i>Дедлайны через 3 дня</i>\n- <i>15.05.2026</i>");
        } else {
            String report = command.getSubjectReport(messageText);
            sendMessage(chatId, report);
        }
    }

    private void sendNotifications() {
        if (users.isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        List<String[]> tasks = data.getNearestTasks(today);

        for (String[] task : tasks) {
            try {
                String discipline = task[0];
                String taskName = task[1];
                String dateStr = task[2];
                LocalDate deadline = LocalDate.parse(dateStr, dateTimeFormatter);
                long daysLeft = ChronoUnit.DAYS.between(today, deadline);

                if (daysLeft == 1 || daysLeft == 2 || daysLeft == 3 || daysLeft == 4 ||
                        daysLeft == 5 || daysLeft == 6 || daysLeft == 7) {
                    String message;
                    if (daysLeft == 0) {
                        message = "Дедлайн сегодня" + taskName;
                    } else {
                        message = "Через " + daysLeft + " дней сдача задания " + taskName + " по предмету " + discipline;
                    }
                    for (Long user : users) {
                        sendMessage(user, message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showMainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Главное меню. Выберите действие:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Ближайшие дедлайны");
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Выбрать предмет");
        row2.add("Написать запрос");
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void showSubjectsMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите предмет:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow backRow = new KeyboardRow();
        backRow.add("Назад");
        keyboard.add(backRow);

        Set<String> subjects = data.getUniqueSubjects();
        KeyboardRow currentRow = new KeyboardRow();

        for (String subject : subjects) {
            if (subject.equalsIgnoreCase("Предмет")) continue;
            currentRow.add(subject);

            if (currentRow.size() == 2) {
                keyboard.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }
        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setParseMode("HTML");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
