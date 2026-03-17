package org.example;

import org.example.Model.ClassesForBot.ProcessingData;
import org.example.Model.TelegramBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class Main {
    public static void main(String[] args) {
        try {

            ProcessingData processingData = new ProcessingData();
            System.out.println(processingData.processingLines());

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot());
            System.out.println("Success");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}