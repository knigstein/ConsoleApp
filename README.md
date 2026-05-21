# Console-application-for-managing-collections
A console application that implements interactive management of a collection of objects. The collection must store objects of the StudyGroup class, described below.

---

In interactive mode, the program supports executing the following commands:
    help : output help for available commands
    info : output information about the collection (type, initialization date, number of elements, etc.)
    to the standard output stream show : output all the elements of the collection in a string representation to the standard output stream
    add {element} : add a new element to the collection
    update id {element} : update the value of a collection element whose id is equal to the specified
    remove_by_id id : delete an element from the collection by its id
    clear : clear the collection
    save : save the collection to a file
    execute_script file_name : read and execute the script from the specified file. The script contains commands in the same form as they are entered by the      user interactively.
    exit : terminate the program (without saving to a file)
    remove_first : remove the first element from the collection
    add_if_min {element} : add a new element to the collection if its value is less than that of the smallest element in this collection
    remove_lower {element} : remove from the collection all elements smaller than the specified one
    filter_contains_name name : output the elements whose name field value contains the specified substring
    filter_greater_than_semester_enum semesterEnum : output the elements whose semesterEnum field value is greater than the specified
    one print_field_descending_group_admin : output the values of the GroupAdmin field of all elements in descending order

---

## Client/Server (Lab 6-style requirements)

This repo contains a split into **client** and **server** applications:

- `server.ServerMain`: коллекция в PostgreSQL, команды по UDP, аргументы: `<db_login> [port]`.
- `client.ClientMain`: интерактивно читает команды, валидирует ввод, отправляет DTO на сервер по UDP и печатает ответ.
- `common.dto.*`: объекты-команды и ответ сервера (обмен не строками, а объектами).
- `common.SerializationUtils`: сериализация объектов для передачи по сети.

### Run

Сборка и запуск выполняются через **Gradle Wrapper**:

```bash
chmod +x ./gradlew
./gradlew clean build
```

Запуск сервера:

```bash
./gradlew runServer -PappArgs="<db_login> 5555"
```

В другом терминале:

```bash
./gradlew runClient -PappArgs="localhost 5555"
```

Сборка готовых артефактов:

```bash
./gradlew serverJar clientJar guiClientJar copyRuntimeLibs
```

### Notes

- Команда `save` **недоступна на клиенте** (не отправляется). На сервере есть **локальная** команда `save`, вводится в консоль сервера.
- Команда `exit` завершает клиент.
- Сервер работает **в одном потоке**: сеть на `DatagramChannel` + `Selector` (non-blocking), локальные команды читаются через `stdin.ready()` без блокировки.
- Коллекции, которые возвращаются клиенту (например `show`/фильтры), сервер сортирует по «местоположению» (координатам).

### Logging (Log4J2)

Под Log4J2 добавлен конфиг `server/src/main/resources/log4j2.xml` (и аналогичный в `client/`).
Если Log4J2 присутствует в classpath, сервер будет логировать через него; иначе используется стандартный `java.util.logging` (чтобы проект компилировался без внешних зависимостей).

## Gradle Tasks

- `./gradlew clean build` — компиляция и стандартная сборка
- `./gradlew serverJar` — серверный исполняемый jar
- `./gradlew clientJar` — консольный клиентский jar
- `./gradlew guiClientJar` — GUI-клиентский jar
- `./gradlew runServer -PappArgs=\"<db_login> 5555\"` — запуск сервера
- `./gradlew runClient -PappArgs=\"localhost 5555\"` — запуск клиента
- `./gradlew distHelios` — подготовка `deploy/helios/dist` и `deploy/helios/lib`
