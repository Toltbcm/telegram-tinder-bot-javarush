package com.javarush.telegram;

import java.util.Arrays;

public final class Util {

    public static String[] fetchMainMenuData(String origin) {
        String[] commands = Arrays.stream(
                        origin
                                .split("\\*Корисні команди та посилання:\\*")[1]
                                .split("(\\d+. | - |\\r\\n)"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);

        for (int i = 0; i < commands.length; i += 2) {
            String temp = commands[i];
            commands[i] = commands[i + 1];
            commands[i + 1] = temp;
        }

        return commands;
    }

    private Util() {
    }
}
