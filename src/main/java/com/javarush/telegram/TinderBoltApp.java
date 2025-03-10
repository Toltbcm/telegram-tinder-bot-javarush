package com.javarush.telegram;

import static com.javarush.telegram.Util.fetchMainMenuData;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TinderBoltApp extends MultiSessionTelegramBot {
    private static final Dotenv dotenv = Dotenv.load();

    public static final String TELEGRAM_BOT_NAME = dotenv.get("TELEGRAM_BOT_NAME");
    public static final String TELEGRAM_BOT_TOKEN = dotenv.get("TELEGRAM_BOT_TOKEN");
    public static final String OPEN_AI_TOKEN = dotenv.get("OPEN_AI_TOKEN");

    public DialogMode dialogMode = DialogMode.MAIN;
    public ChatGPTService gptService = new ChatGPTService(OPEN_AI_TOKEN);

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();

        if (message.equals("/start")) {
            dialogMode = DialogMode.MAIN;
            String menu = loadMessage("main");
            showMainMenu(fetchMainMenuData(menu));
            sendTextMessage(menu);
            sendPhotoMessage("main");
            return;
        }

        if (message.equals("/gpt")) {
            dialogMode = DialogMode.GPT;
            String gptMessage = loadMessage("gpt");
            sendTextMessage(gptMessage);
            sendPhotoMessage("gpt");
            return;
        }

        if (dialogMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");
            String string = gptService.sendMessage(prompt, message);
            sendTextMessage(string);
            return;
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
