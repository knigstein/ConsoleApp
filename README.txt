# Lab 5 - Collection Management Client-Server Application

## Структура проекта

```
lab5/
├── dist/
│   ├── server.jar          # Серверное приложение
│   └── client.jar          # Клиентское приложение
├── data.xml                # Файл коллекции
├── srs/main/resources/
│   └── log4j2.xml          # Конфигурация логирования
├── scripts/
│   └── demo.txt            # Пример скрипта для execute_script
├── javadoc/                # Javadoc документация (HTML)
│   └── index.html          # Главная страница документации
├── run-server.sh           # Скрипт запуска сервера
├── run-client.sh           # Скрипт запуска клиента
├── build.sh                # Скрипт сборки
├── generate-javadoc.sh     # Скрипт генерации Javadoc
└── README.txt              # Эта инструкция
```

## Быстрый старт

### Запуск сервера
```bash
./run-server.sh data.xml 5555
```

### Запуск клиента (в другом терминале)
```bash
./run-client.sh localhost 5555
```

## Команды клиента

| Команда | Описание |
|---------|----------|
| `info` | Информация о коллекции |
| `show` | Показать все элементы (отсортированы по координатам) |
| `add` | Добавить новый элемент |
| `update <id>` | Обновить элемент по ID |
| `remove_by_id <id>` | Удалить элемент по ID |
| `remove_first` | Удалить первый элемент |
| `clear` | Очистить коллекцию |
| `add_if_min` | Добавить, если значение меньше минимального |
| `remove_lower` | Удалить элементы, меньшие указанного |
| `filter_contains_name <substring>` | Фильтровать по названию |
| `filter_greater_than_semester_enum <SEMESTER>` | Фильтровать по семестру |
| `print_field_descending_group_admin` | Вывести имена администраторов по убыванию |
| `execute_script <filename>` | Выполнить скрипт |
| `help` | Показать справку |
| `exit` | Завершить клиент |

## Команды сервера (ввод в консоли сервера)

| Команда | Описание |
|---------|----------|
| `save` | Сохранить коллекцию в файл (только сервер) |
| `exit` | Завершить работу сервера (с автосохранением) |

## Javadoc Документация

### Просмотр локально:
```bash
# Linux
xdg-open javadoc/index.html

# macOS
open javadoc/index.html

# Windows
start javadoc\index.html
```

### Генерация заново:
```bash
./generate-javadoc.sh
```

### Структура Javadoc:
- **model/** — классы модели данных (StudyGroup, Person, Coordinates, enums)
- **client/** — клиентское приложение (ClientMain, ClientNetwork)
- **server/** — серверное приложение (ServerMain, ServerCommand, ServerLog)
- **server.commands/** — обработчики команд на сервере
- **common.dto/** — DTO для передачи данных между клиентом и сервером
- **command/** — команды клиентского приложения
- **collection/** — управление коллекцией (CollectionManager)
- **io/** — ввод/вывод (FileManager, ConsoleManager)
- **util/** — утилиты (IdGenerator, StudyGroupBuilder)

## Требования

- Java 17 или выше
- Свободный UDP порт 5555

## Логирование

Логи выводятся в консоль сервера в формате:
```
[HH:MM:SS] LEVEL Class - Message
```

Пример:
```
[17:25:33] INFO  ServerMain - Сервер запущен на порту 5555
[17:25:40] INFO  ServerMain - Получен запрос от /127.0.0.1:54321
[17:25:40] INFO  ServerMain - Ответ отправлен /127.0.0.1:54321
```

## Примечания

- Log4J2 используется если jar-файлы есть в classpath
- Если Log4J2 недоступен, используется java.util.logging
- Команда `save` доступна ТОЛЬКО на сервере
- Клиент отправляет команды на сервер по UDP
- Все DTO реализуют Serializable для сетевой передачи
- Коллекция сортируется по координатам (местоположению) перед отправкой клиенту

## Архитектура

```
┌─────────────┐                    ┌─────────────┐
│   Клиент    │                    │   Сервер    │
│             │                    │             │
│ ClientMain  │ ───────UDP───────> │ ServerMain  │
│ ClientNetwork│   (DatagramChannel)│ ServerCommandManager │
│             │ <───────────────── │             │
│  CommandDTO │   CommandResponseDTO│ CollectionManager │
│             │                    │             │
└─────────────┘                    └─────────────┘
```
