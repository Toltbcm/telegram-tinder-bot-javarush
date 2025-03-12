package com.javarush.telegram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Util {

    public static String[] fetchMainMenuData(String origin) {
        String[] descriptionsCommands = Arrays.stream(
                        origin
                                .split("\\*Корисні команди та посилання:\\*")[1]
                                .split("(\\d+\\. | - |\\r\\n)"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);

        for (int i = 0; i < descriptionsCommands.length; i += 2) {
            String temp = descriptionsCommands[i];
            descriptionsCommands[i] = descriptionsCommands[i + 1];
            descriptionsCommands[i + 1] = temp;
        }

        return descriptionsCommands;
    }

    public static List<String> getDateButtonsData() {
        List<String> starsIds = new ArrayList<>();
        starsIds.add("Аріана Гранде 🔥");
        starsIds.add("date_grande");
        starsIds.add("Марго Роббі 🔥🔥");
        starsIds.add("date_robbie");
        starsIds.add("Зендея 🔥🔥🔥");
        starsIds.add("date_zendaya");
        starsIds.add("Райан Гослінг 😎");
        starsIds.add("date_gosling");
        starsIds.add("Том Харді 😎😎");
        starsIds.add("date_hardy");
        return starsIds;
    }

    public static String[] getMessageButtonsData() {
        return new String[]{
                "Наступне повідомлення", "message_next",
                "Запросити на побачення", "message_date"
        };
    }

    private Util() {
    }
}
