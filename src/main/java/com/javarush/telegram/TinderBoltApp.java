package com.javarush.telegram;

import static com.javarush.telegram.Util.fetchMainMenuData;
import static com.javarush.telegram.Util.getDateButtonsData;
import static com.javarush.telegram.Util.getMessageButtonsData;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TinderBoltApp extends MultiSessionTelegramBot {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String TELEGRAM_BOT_NAME = dotenv.get("TELEGRAM_BOT_NAME");
    private static final String TELEGRAM_BOT_TOKEN = dotenv.get("TELEGRAM_BOT_TOKEN");
    private static final String OPEN_AI_TOKEN = dotenv.get("OPEN_AI_TOKEN");

    private static final ChatGPTService gptService = new ChatGPTService(OPEN_AI_TOKEN);

    private DialogMode dialogMode = DialogMode.MAIN;
    private List<String> chatHistory;
    private UserInfo userInfo;
    private int questionNumber;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String callBackMessage = getMessageText();

        switch (callBackMessage) {
            case "/start" -> {
                dialogMode = DialogMode.MAIN;
                String menu = loadMessage("main");
                showMainMenu(fetchMainMenuData(menu));
                sendTextMessage(menu);
                sendPhotoMessage("main");
                return;
            }
            case "/gpt" -> {
                dialogMode = DialogMode.GPT;
                String message = loadMessage("gpt");
                sendTextMessage(message);
                sendPhotoMessage("gpt");
                return;
            }
            case "/date" -> {
                dialogMode = DialogMode.DATE;
                sendPhotoMessage("date");
                String message = loadMessage("date");
                sendTextButtonsMessage(message, getDateButtonsData());
                return;
            }
            case "/message" -> {
                dialogMode = DialogMode.MESSAGE;
                sendPhotoMessage("message");
                String message = loadMessage("message");
                sendTextButtonsMessage(message, getMessageButtonsData());
                chatHistory = new ArrayList<>();
                return;
            }
            case "/profile" -> {
                dialogMode = DialogMode.PROFILE;
                sendPhotoMessage("profile");
                String message = loadMessage("profile");
                sendTextMessage(message);
                userInfo = new UserInfo();
                questionNumber = 1;
            }
            case "/opener" -> {
                dialogMode = DialogMode.OPENER;
                sendPhotoMessage("opener");
                String message = loadMessage("opener");
                sendTextMessage(message);
                userInfo = new UserInfo();
                questionNumber = 1;
            }
        }

        switch (dialogMode) {
            case GPT -> {
                Message waitingMessage = sendWaitingMessage();
                String prompt = loadPrompt("gpt");
                String gtpAnswer = gptService.sendMessage(prompt, callBackMessage);
                updateTextMessage(waitingMessage, gtpAnswer);
            }
            case DATE -> {
                String callBackButtonKey = getCallbackQueryButtonKey();
                if (callBackButtonKey.startsWith("date_")) {
                    sendPhotoMessage(callBackButtonKey);
                    sendTextMessage("Починай спілкування. ✍");
                    String prompt = loadPrompt(callBackButtonKey);
                    gptService.setPrompt(prompt);
                    return;
                }
                Message waitingMessage = sendWaitingMessage();
                String gptAnswer = gptService.addMessage(callBackMessage);
                updateTextMessage(waitingMessage, gptAnswer);
            }
            case MESSAGE -> {
                String callBackButtonKey = getCallbackQueryButtonKey();
                if (callBackButtonKey.startsWith("message_")) {
                    Message waitMessage = sendWaitingMessage();
                    String prompt = loadPrompt(callBackButtonKey);
                    String history = String.join("/n/n", chatHistory);
                    String gptAnswer = gptService.sendMessage(prompt, history);
                    updateTextMessage(waitMessage, gptAnswer);
                    return;
                }
                chatHistory.add(callBackMessage);
            }
            case PROFILE -> askQuestion(callBackMessage, "profile");
            case OPENER -> askQuestion(callBackMessage, "opener");
        }
    }

    private void askQuestion(String callBackMessage, String promptName) {
        switch (questionNumber++) {
            case 1 -> sendTextMessage("Введіть ім'я?");
            case 2 -> {
                userInfo.name = callBackMessage;
                sendTextMessage("Введіть вік?");
            }
            case 3 -> {
                userInfo.age = callBackMessage;
                sendTextMessage("Введіть місто?");
            }
            case 4 -> {
                userInfo.city = callBackMessage;
                sendTextMessage("Введіть професію?");
            }
            case 5 -> {
                userInfo.occupation = callBackMessage;
                sendTextMessage("Введіть хоббі?");
            }
            case 6 -> {
                userInfo.hobby = callBackMessage;
                sendTextMessage("Введіть цілі для знайомства?");
            }
            case 7 -> {
                userInfo.goals = callBackMessage;
                String prompt = loadPrompt(promptName);
                Message waitingMessage = sendWaitingMessage();
                String gptAnswer = gptService.sendMessage(prompt, userInfo.toString());
                updateTextMessage(waitingMessage, gptAnswer);
                sendTextMessage("Введіть наступну команду.");
            }
        }
    }

    private Message sendWaitingMessage() {
        return sendTextMessage("Почекай. Думаю... ⏳");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
