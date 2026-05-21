package command;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Команда {@code execute_script}, выполняющая последовательность команд из текстового файла.
 * Каждая строка файла интерпретируется как пользовательский ввод, который передаётся
 * в {@link CommandManager} вместе со {@link Scanner}, читающим из файла.
 *
 * Режим скрипта реализован за счёт передачи сканера файла в {@link CommandManager#execute(String, Scanner)},
 * который, в свою очередь, передаёт его командам, реализующим {@link ScriptAware}. Таким образом, команды,
 * требующие интерактивного ввода (например, добавление или обновление элемента), могут считывать параметры
 * не из консоли, а непосредственно из файла скрипта.
 *
 * Для защиты от рекурсии используется множество {@link #executingScripts}: если попытаться повторно
 * запустить скрипт с тем же именем файла (в том числе косвенно, через вложенные вызовы),
 * выполнение будет заблокировано с сообщением «Рекурсия запрещена.».
 */
public class ExecuteScriptCommand implements Command {

    /**
     * Менеджер команд, через который проксируется выполнение строк скрипта.
     */
    private final CommandManager commandManager;

    /**
     * Набор имён файлов скриптов, которые в данный момент находятся в процессе выполнения.
     * Используется для предотвращения прямой и косвенной рекурсии при вызове {@code execute_script}.
     */
    private static final Set<String> executingScripts = new HashSet<>();

    /**
     * Создаёт команду выполнения скрипта, связанную с указанным менеджером команд.
     *
     * @param commandManager менеджер команд, используемый для исполнения строк скрипта
     */
    public ExecuteScriptCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Выполняет команды, перечисленные в указанном файле-скрипте.
     * Ожидается, что второй аргумент командной строки ({@code args[1]})
     * содержит путь к файлу.
     *
     * Особенности поведения:
     * <ul>
     *     <li>При отсутствии аргумента с именем файла выводится сообщение об ошибке.</li>
     *     <li>Если скрипт уже выполняется (обнаружен в {@link #executingScripts}), выполнение
     *     блокируется для предотвращения рекурсии.</li>
     *     <li>При отсутствии или недоступности файла выводится сообщение «Файл не найден.».</li>
     *     <li>Любые ошибки при чтении файла или выполнении вложенных команд перехватываются,
     *     и пользователю выводится сообщение «Ошибка выполнения скрипта.».</li>
     *     <li>Независимо от результата выполнения, имя файла удаляется из множества
     *     {@link #executingScripts} в блоке {@code finally}.</li>
     * </ul>
     *
     * @param args массив аргументов, где {@code args[1]} — путь к файлу со скриптом
     */
    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указано имя файла.");
            return;
        }

        String fileName = args[1];

        if (executingScripts.contains(fileName)) {
            System.out.println("Рекурсия запрещена.");
            return;
        }

        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("Файл не найден.");
            return;
        }

        executingScripts.add(fileName);

        try (Scanner fileScanner = new Scanner(file)) {

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                commandManager.execute(line, fileScanner);
            }

        } catch (Exception e) {
            System.out.println("Ошибка выполнения скрипта.");
        } finally {
            executingScripts.remove(fileName);
        }
    }

    /**
     * Возвращает краткое текстовое описание команды для вывода в справке.
     *
     * @return строка с описанием назначения команды
     */
    @Override
    public String getDescription() {
        return "выполнить скрипт";
    }
}