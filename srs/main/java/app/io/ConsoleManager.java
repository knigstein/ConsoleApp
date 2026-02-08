package io;

import java.util.Scanner;

/**
 * Управляет вводом пользователя.
 */

public class ConsoleManager {

    private final Scanner scanner = new Scanner(System.in);

    public String readLine() {
        return scanner.nextLine();
    }

    public void print(String message) {
        System.out.println(message);
    }
}

