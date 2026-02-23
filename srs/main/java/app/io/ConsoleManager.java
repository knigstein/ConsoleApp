package io;

import java.util.Scanner;

/**
 * Класс-обёртка над стандартным вводом и выводом консоли.
 * Обеспечивает чтение строк от пользователя и вывод сообщений.
 *
 * Может использоваться для замены источника ввода/вывода при тестировании.
 */
public class ConsoleManager {

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Считывает строку ввода пользователя из консоли.
     *
     * @return введённая пользователем строка
     */
    public String readLine() {
        return scanner.nextLine();
    }

    /**
     * Выводит переданное сообщение в стандартный поток вывода.
     *
     * @param message текст сообщения для вывода
     */
    public void print(String message) {
        System.out.println(message);
    }
}

