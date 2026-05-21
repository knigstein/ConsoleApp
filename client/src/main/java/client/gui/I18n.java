package client.gui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import model.Color;
import model.Country;
import model.Semester;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Localization resources stored in class maps.
 */
public final class I18n {

    private static final Map<String, Map<String, String>> BUNDLE = new HashMap<>();

    static {
        put("ru", "app.title", "ITMO StudyGroup Client");
        put("ru", "nav.map", "Карта");
        put("ru", "nav.table", "Таблица");
        put("ru", "nav.logout", "Выйти");
        put("ru", "cmd.manual", "Ручной ввод команды");
        put("ru", "cmd.run", "Выполнить");
        put("ru", "cmd.add", "Добавить");
        put("ru", "cmd.update", "Обновить");
        put("ru", "user", "Пользователь");
        put("ru", "status.online", "Синхронизация: online");
        put("ru", "auth.title", "Авторизация / регистрация");
        put("ru", "auth.login", "Вход");
        put("ru", "auth.register", "Регистрация");
        put("ru", "auth.username", "Логин");
        put("ru", "auth.password", "Пароль");
        put("ru", "auth.submit", "Продолжить");
        put("ru", "auth.locale", "Локаль");
        put("ru", "auth.subtitle", "Корпус ИТМО, ул. Ломоносова, 9 — интерактивная карта этажей и коллекция учебных групп");
        put("ru", "auth.error.empty", "Введите логин и пароль");
        put("ru", "auth.connecting", "Подключение к серверу…");
        put("ru", "auth.error.network", "Сервер не отвечает. Проверьте туннель и сервер на Helios.");
        put("ru", "map.building", "Корпус ИТМО · Ломоносова, 9");
        put("ru", "map.floor.short", "Этаж");
        put("ru", "map.title", "Корпус ИТМО (Ломоносова 9), этаж");
        put("ru", "cmd.section.collection", "Коллекция");
        put("ru", "cmd.section.crud", "Объекты");
        put("ru", "cmd.section.modify", "Изменение");
        put("ru", "cmd.section.filters", "Фильтры");
        put("ru", "cmd.section.other", "Прочее");
        put("ru", "cmd.show", "Показать коллекцию");
        put("ru", "cmd.info", "Сведения о коллекции");
        put("ru", "cmd.addIfMin", "Добавить, если минимальное");
        put("ru", "cmd.removeLower", "Удалить меньшие");
        put("ru", "cmd.clear", "Очистить свои объекты");
        put("ru", "cmd.removeFirst", "Удалить первый");
        put("ru", "cmd.removeById", "Удалить по ID");
        put("ru", "cmd.filterName", "Фильтр по имени");
        put("ru", "cmd.filterName.prompt", "Подстрока в названии");
        put("ru", "cmd.filterSemester", "Фильтр по семестру");
        put("ru", "cmd.printAdmins", "Админы (по убыванию)");
        put("ru", "cmd.executeScript", "Выполнить скрипт");
        put("ru", "welcome.morning", "Доброе утро, %s");
        put("ru", "welcome.afternoon", "Добрый день, %s");
        put("ru", "welcome.evening", "Добрый вечер, %s");
        put("ru", "welcome.night", "Доброй ночи, %s");
        put("ru", "cmd.refresh", "Обновить с сервера");
        put("ru", "dialog.confirm", "Подтверждение");
        put("ru", "cmd.confirm.clear", "Очистить все ваши объекты в коллекции?");
        put("ru", "cmd.confirm.delete", "Удалить выбранный объект?");
        put("ru", "cmd.confirm.removeFirst", "Удалить первый ваш объект в коллекции?");
        put("ru", "map.floor", "Этаж");
        put("ru", "map.filter.owner", "Владелец");
        put("ru", "map.filter.semester", "Семестр");
        put("ru", "map.filter.name", "Название");
        put("ru", "map.filter.reset", "Сброс");
        put("ru", "map.details.empty", "Выберите объект на карте");
        put("ru", "map.details.groupsOnFloor", "Объектов на этаже");
        put("ru", "map.error.imageLoad", "Не удалось загрузить план этажа");
        put("ru", "map.legend.own", "Ваши объекты");
        put("ru", "map.legend.others", "Другие пользователи");
        put("ru", "map.legend.hint", "Размер маркера ∝ числу студентов");
        put("ru", "map.legend.click", "клик по маркеру — детали");
        put("ru", "map.zoom.in", "Увеличить");
        put("ru", "map.zoom.out", "Уменьшить");
        put("ru", "map.zoom.reset", "Сброс");
        put("ru", "map.zoom.level", "Масштаб: %d%%");
        put("ru", "map.zoom.hint", "Колёсико — масштаб · ПКМ — перемещение");
        put("ru", "editor.room.selectFloorFirst", "Сначала выберите этаж");
        put("ru", "info.placement.header", "Размещение групп по аудиториям:");
        put("ru", "info.placement.empty", "Коллекция пуста.");
        put("ru", "info.placement.count", "Всего объектов: %d");
        put("ru", "info.placement.roomLine", "Этаж %d · %s");
        put("ru", "info.placement.owner", "владелец");
        put("ru", "info.placement.students", "студентов");
        put("ru", "cmd.help", "Справка");
        put("ru", "map.object.info", "Информация об объекте");
        put("ru", "map.object.room", "Аудитория");
        put("ru", "map.object.owner", "Владелец");
        put("ru", "map.object.edit", "Редактировать");
        put("ru", "map.object.delete", "Удалить");
        put("ru", "map.object.field.id", "ID");
        put("ru", "map.object.field.name", "Название");
        put("ru", "map.object.field.coordinates", "Координаты");
        put("ru", "map.object.field.creationDate", "Дата создания");
        put("ru", "map.object.field.students", "Студентов");
        put("ru", "map.object.field.expelled", "Отчислено");
        put("ru", "map.object.field.transferred", "Переведено");
        put("ru", "map.object.field.semester", "Семестр");
        put("ru", "map.object.field.admin", "Администратор");
        put("ru", "table.title", "База учебных групп");
        put("ru", "table.filters", "Фильтры");
        put("ru", "table.page", "Страница");
        put("ru", "table.prev", "Назад");
        put("ru", "table.next", "Вперед");
        put("ru", "table.rows", "строк");
        put("ru", "table.openMap", "Открыть на карте");
        put("ru", "table.add", "Добавить");
        put("ru", "table.edit", "Редактировать");
        put("ru", "table.delete", "Удалить");
        put("ru", "table.col.id", "id");
        put("ru", "table.col.name", "name");
        put("ru", "table.col.coordX", "coord.x");
        put("ru", "table.col.coordY", "coord.y");
        put("ru", "table.col.creationDate", "creationDate");
        put("ru", "table.col.students", "students");
        put("ru", "table.col.expelled", "expelled");
        put("ru", "table.col.transferred", "transferred");
        put("ru", "table.col.semester", "semester");
        put("ru", "table.col.adminName", "admin.name");
        put("ru", "table.col.adminBirthday", "admin.birthday");
        put("ru", "table.col.eyeColor", "admin.eyeColor");
        put("ru", "table.col.nationality", "admin.nationality");
        put("ru", "table.col.ownerId", "ownerId");
        put("ru", "table.col.room", "room");
        put("ru", "editor.title", "Редактирование группы");
        put("ru", "editor.save", "Сохранить");
        put("ru", "editor.cancel", "Отмена");
        put("ru", "editor.field.name", "Название");
        put("ru", "editor.field.floor", "Этаж");
        put("ru", "editor.field.room", "Аудитория / помещение");
        put("ru", "editor.coordsPreview", "Помещение: %s · этаж %d · координаты для сервера: X=%d, Y=%.0f");
        put("ru", "editor.error.name", "Укажите название группы");
        put("ru", "editor.error.room", "Выберите аудиторию");
        put("ru", "editor.error.students", "Количество студентов должно быть > 0");
        put("ru", "editor.error.transferred", "Количество переведённых должно быть > 0");
        put("ru", "editor.error.floor", "Выберите этаж (1–5)");
        put("ru", "editor.error.adminName", "Укажите имя администратора");
        put("ru", "editor.error.birthday", "Укажите дату рождения");
        put("ru", "editor.error.expelledNegative", "Число отчисленных не может быть отрицательным");
        put("ru", "editor.error.expelledPositive", "Если указаны отчисленные, значение должно быть > 0 (или 0 — не указывать)");
        put("ru", "editor.error.spinnerInvalid", "Введите корректные целые числа в числовых полях");
        put("ru", "editor.error.generic", "Ошибка: %s");
        put("ru", "editor.field.coordX", "Координата X");
        put("ru", "editor.field.coordY", "Координата Y");
        put("ru", "editor.coordHint", "Координаты вычисляются автоматически по выбранной аудитории.");
        put("ru", "cmd.filterSemester.hint", "Показать группы с семестром больше выбранного");
        put("ru", "editor.field.students", "Количество студентов");
        put("ru", "editor.field.expelled", "Отчисленные");
        put("ru", "editor.field.transferred", "Переведенные");
        put("ru", "editor.field.semester", "Семестр");
        put("ru", "editor.field.adminName", "Имя администратора");
        put("ru", "editor.field.adminBirthday", "Дата рождения");
        put("ru", "editor.field.eyeColor", "Цвет глаз");
        put("ru", "editor.field.nationality", "Национальность");
        put("ru", "editor.invalid", "Некорректные поля");
        put("ru", "msg.loginRequired", "Для выполнения команды требуется авторизация.");
        put("ru", "msg.loadFailed", "Ошибка получения данных");
        put("ru", "msg.noRoom", "Аудитория не определена");
        put("ru", "msg.unknownCommand", "Неизвестная или некорректная команда");
        put("ru", "msg.selectGroupFirst", "Сначала выберите объект");
        put("ru", "msg.collectionUpdated", "Коллекция обновлена");
        put("ru", "msg.mockMode", "Включен mock-режим. Используйте --mock только для локального теста UI.");
        put("ru", "msg.editDenied", "Редактирование чужого объекта запрещено");
        put("ru", "msg.deleteDenied", "Удаление чужого объекта запрещено");
        put("ru", "msg.filterResult", "Найдено элементов: %d");
        put("ru", "msg.multilineResult", "Результат: %d строк (подробности в диалоге)");
        put("ru", "msg.commandFailed", "Команда не выполнена");
        put("ru", "msg.commandInProgress", "Подождите, выполняется предыдущая команда…");
        put("ru", "msg.noAdmins", "Администраторы не найдены");
        put("ru", "msg.mockScript", "Mock: execute_script — только с подключённым сервером");
        put("ru", "cmd.help.text", "Команды: info, show, clear, remove_first, remove_by_id [id], add, add_if_min, update [id], remove_lower, filter_contains_name <name>, filter_greater_than_semester_enum <SEMESTER>, print_field_descending_group_admin, execute_script <file>, help, logout, exit");

        put("be-BY", "app.title", "ITMO StudyGroup Client");
        put("be-BY", "nav.map", "Карта");
        put("be-BY", "nav.table", "Табліца");
        put("be-BY", "nav.logout", "Выйсці");
        put("be-BY", "cmd.manual", "Ручны ўвод каманды");
        put("be-BY", "cmd.run", "Выканаць");
        put("be-BY", "cmd.add", "add");
        put("be-BY", "cmd.update", "update");
        put("be-BY", "cmd.removeById", "remove_by_id");
        put("be-BY", "user", "Карыстальнік");
        put("be-BY", "status.online", "Сінхранізацыя: online");
        put("be-BY", "auth.title", "Аўтарызацыя / рэгістрацыя");
        put("be-BY", "auth.login", "Уваход");
        put("be-BY", "auth.register", "Рэгістрацыя");
        put("be-BY", "auth.username", "Лагін");
        put("be-BY", "auth.password", "Пароль");
        put("be-BY", "auth.submit", "Працягнуць");
        put("be-BY", "auth.locale", "Лакаль");
        put("be-BY", "auth.subtitle", "Корпус ITMO, вул. Ламаносава, 9 — інтэрактыўная карта і калекцыя");
        put("be-BY", "auth.error.empty", "Увядзіце лагін і пароль");
        put("be-BY", "auth.connecting", "Падключэнне да сервера…");
        put("be-BY", "auth.error.network", "Сервер не адказвае.");
        put("be-BY", "map.building", "Корпус ITMO · Ламаносава, 9");
        put("be-BY", "map.floor.short", "Паверх");
        put("be-BY", "map.title", "Корпус ITMO (Ламаносава 9), паверх");
        put("be-BY", "cmd.section.collection", "Калекцыя");
        put("be-BY", "cmd.section.crud", "Аб'екты");
        put("be-BY", "cmd.section.modify", "Змена");
        put("be-BY", "cmd.section.filters", "Фільтры");
        put("be-BY", "cmd.section.other", "Іншае");
        put("be-BY", "cmd.show", "Паказаць калекцыю");
        put("be-BY", "cmd.info", "Інфармацыя");
        put("be-BY", "cmd.addIfMin", "add_if_min");
        put("be-BY", "cmd.removeLower", "remove_lower");
        put("be-BY", "cmd.clear", "clear");
        put("be-BY", "cmd.removeFirst", "remove_first");
        put("be-BY", "cmd.removeById", "remove_by_id");
        put("be-BY", "cmd.filterName", "filter_contains_name");
        put("be-BY", "cmd.filterName.prompt", "Падрадок у назве");
        put("be-BY", "cmd.filterSemester", "filter_greater_than_semester");
        put("be-BY", "cmd.printAdmins", "print_field_descending_group_admin");
        put("be-BY", "cmd.executeScript", "execute_script");
        put("be-BY", "cmd.refresh", "Абнавіць з сервера");
        put("be-BY", "welcome.morning", "Добрай раніцы, %s");
        put("be-BY", "welcome.afternoon", "Добры дзень, %s");
        put("be-BY", "welcome.evening", "Добры вечар, %s");
        put("be-BY", "welcome.night", "Дабранач, %s");
        put("be-BY", "dialog.confirm", "Пацверджанне");
        put("be-BY", "cmd.confirm.clear", "Ачысціць усе вашы аб'екты?");
        put("be-BY", "cmd.confirm.delete", "Выдаліць выбраны аб'ект?");
        put("be-BY", "map.floor", "Паверх");
        put("be-BY", "map.filter.owner", "Уладальнік");
        put("be-BY", "map.filter.semester", "Семестр");
        put("be-BY", "map.filter.name", "Назва");
        put("be-BY", "map.filter.reset", "Скінуць");
        put("be-BY", "map.details.empty", "Выберыце аб'ект на карце");
        put("be-BY", "map.details.groupsOnFloor", "Аб'ектаў на паверсе");
        put("be-BY", "map.error.imageLoad", "Не ўдалося загрузіць план паверха");
        put("be-BY", "map.legend.own", "Вашыя аб'екты");
        put("be-BY", "map.legend.others", "Іншыя карыстальнікі");
        put("be-BY", "map.legend.hint", "Памер маркера ∝ колькасці студэнтаў");
        put("be-BY", "map.legend.click", "клік па маркеры — дэталі");
        put("be-BY", "cmd.help", "Даведка");
        put("be-BY", "map.object.info", "Інфармацыя аб аб'екце");
        put("be-BY", "map.object.room", "Аўдыторыя");
        put("be-BY", "map.object.owner", "Уладальнік");
        put("be-BY", "map.object.edit", "Рэдагаваць");
        put("be-BY", "map.object.delete", "Выдаліць");
        put("be-BY", "map.object.field.id", "ID");
        put("be-BY", "map.object.field.name", "Назва");
        put("be-BY", "map.object.field.coordinates", "Каардынаты");
        put("be-BY", "map.object.field.creationDate", "Дата стварэння");
        put("be-BY", "map.object.field.students", "Студэнтаў");
        put("be-BY", "map.object.field.expelled", "Адлічана");
        put("be-BY", "map.object.field.transferred", "Пераведзена");
        put("be-BY", "map.object.field.semester", "Семестр");
        put("be-BY", "map.object.field.admin", "Адміністратар");
        put("be-BY", "table.title", "База вучэбных груп");
        put("be-BY", "table.filters", "Фільтры");
        put("be-BY", "table.page", "Старонка");
        put("be-BY", "table.prev", "Назад");
        put("be-BY", "table.next", "Наперад");
        put("be-BY", "table.rows", "радкоў");
        put("be-BY", "table.openMap", "Адкрыць на карце");
        put("be-BY", "table.add", "Дадаць");
        put("be-BY", "table.edit", "Рэдагаваць");
        put("be-BY", "table.delete", "Выдаліць");
        put("be-BY", "table.col.id", "id");
        put("be-BY", "table.col.name", "name");
        put("be-BY", "table.col.coordX", "coord.x");
        put("be-BY", "table.col.coordY", "coord.y");
        put("be-BY", "table.col.creationDate", "creationDate");
        put("be-BY", "table.col.students", "students");
        put("be-BY", "table.col.expelled", "expelled");
        put("be-BY", "table.col.transferred", "transferred");
        put("be-BY", "table.col.semester", "semester");
        put("be-BY", "table.col.adminName", "admin.name");
        put("be-BY", "table.col.adminBirthday", "admin.birthday");
        put("be-BY", "table.col.eyeColor", "admin.eyeColor");
        put("be-BY", "table.col.nationality", "admin.nationality");
        put("be-BY", "table.col.ownerId", "ownerId");
        put("be-BY", "table.col.room", "room");
        put("be-BY", "editor.title", "Рэдагаванне групы");
        put("be-BY", "editor.save", "Захаваць");
        put("be-BY", "editor.cancel", "Адмена");
        put("be-BY", "editor.field.name", "Назва");
        put("be-BY", "editor.field.floor", "Паверх (корпус)");
        put("be-BY", "editor.field.coordX", "Каардыната X");
        put("be-BY", "editor.field.coordY", "Каардыната Y");
        put("be-BY", "editor.coordHint", "X задае паверх: 0–199 → 1, 200–399 → 2, …, 800–1000 → 5. Y — пазіцыя на плане (0–1000).");
        put("be-BY", "editor.field.students", "Колькасць студэнтаў");
        put("be-BY", "editor.field.expelled", "Адлічаныя");
        put("be-BY", "editor.field.transferred", "Пераведзеныя");
        put("be-BY", "editor.field.semester", "Семестр");
        put("be-BY", "editor.field.adminName", "Імя адміністратара");
        put("be-BY", "editor.field.adminBirthday", "Дата нараджэння");
        put("be-BY", "editor.field.eyeColor", "Колер вачэй");
        put("be-BY", "editor.field.nationality", "Нацыянальнасць");
        put("be-BY", "editor.invalid", "Некарэктныя палі");
        put("be-BY", "msg.loginRequired", "Для каманды патрэбна аўтарызацыя.");
        put("be-BY", "msg.loadFailed", "Памылка загрузкі дадзеных");
        put("be-BY", "msg.noRoom", "Аўдыторыя не вызначана");
        put("be-BY", "msg.unknownCommand", "Невядомая ці некарэктная каманда");
        put("be-BY", "msg.selectGroupFirst", "Спачатку выберыце аб'ект");
        put("be-BY", "msg.collectionUpdated", "Калекцыя абноўлена");
        put("be-BY", "msg.mockMode", "Уключаны mock-рэжым. Выкарыстоўвайце --mock толькі для лакальнага тэста UI.");
        put("be-BY", "msg.editDenied", "Рэдагаваць чужы аб'ект нельга");
        put("be-BY", "msg.deleteDenied", "Выдаляць чужы аб'ект нельга");
        put("be-BY", "cmd.help.text", "Каманды: info, show, clear, remove_first, remove_by_id [id], add, add_if_min, update [id], remove_lower, filter_contains_name <name>, filter_greater_than_semester_enum <SEMESTER>, print_field_descending_group_admin, execute_script <file>, help, logout, exit");

        put("hu", "app.title", "ITMO StudyGroup Client");
        put("hu", "nav.map", "Térkép");
        put("hu", "nav.table", "Táblázat");
        put("hu", "nav.logout", "Kijelentkezés");
        put("hu", "cmd.manual", "Parancs kézi bevitele");
        put("hu", "cmd.run", "Futtatás");
        put("hu", "cmd.add", "add");
        put("hu", "cmd.update", "update");
        put("hu", "cmd.removeById", "remove_by_id");
        put("hu", "user", "Felhasználó");
        put("hu", "status.online", "Szinkron: online");
        put("hu", "auth.title", "Bejelentkezés / regisztráció");
        put("hu", "auth.login", "Belépés");
        put("hu", "auth.register", "Regisztráció");
        put("hu", "auth.username", "Felhasználónév");
        put("hu", "auth.password", "Jelszó");
        put("hu", "auth.submit", "Folytatás");
        put("hu", "auth.locale", "Nyelv");
        put("hu", "auth.subtitle", "ITMO épület, Lomonoszov utca 9. — interaktív térkép és gyűjtemény");
        put("hu", "auth.error.empty", "Adja meg a felhasználónevet és jelszót");
        put("hu", "auth.connecting", "Kapcsolódás a szerverhez…");
        put("hu", "auth.error.network", "A szerver nem válaszol.");
        put("hu", "map.building", "ITMO épület · Lomonoszov u. 9.");
        put("hu", "map.floor.short", "Szint");
        put("hu", "map.title", "ITMO épület (Lomonoszov utca 9.), szint");
        put("hu", "cmd.section.collection", "Gyűjtemény");
        put("hu", "cmd.section.crud", "Objektumok");
        put("hu", "cmd.section.modify", "Módosítás");
        put("hu", "cmd.section.filters", "Szűrők");
        put("hu", "cmd.section.other", "Egyéb");
        put("hu", "cmd.show", "Gyűjtemény megjelenítése");
        put("hu", "cmd.info", "Információ");
        put("hu", "cmd.addIfMin", "add_if_min");
        put("hu", "cmd.removeLower", "remove_lower");
        put("hu", "cmd.clear", "clear");
        put("hu", "cmd.removeFirst", "remove_first");
        put("hu", "cmd.removeById", "remove_by_id");
        put("hu", "cmd.filterName", "filter_contains_name");
        put("hu", "cmd.filterName.prompt", "Névrészlet");
        put("hu", "cmd.filterSemester", "filter_greater_than_semester");
        put("hu", "cmd.printAdmins", "print_field_descending_group_admin");
        put("hu", "cmd.executeScript", "execute_script");
        put("hu", "cmd.refresh", "Frissítés a szerverről");
        put("hu", "welcome.morning", "Jó reggelt, %s");
        put("hu", "welcome.afternoon", "Jó napot, %s");
        put("hu", "welcome.evening", "Jó estét, %s");
        put("hu", "welcome.night", "Jó éjszakát, %s");
        put("hu", "dialog.confirm", "Megerősítés");
        put("hu", "cmd.confirm.clear", "Törli az összes saját objektumot?");
        put("hu", "cmd.confirm.delete", "Törli a kijelölt objektumot?");
        put("hu", "map.floor", "Szint");
        put("hu", "map.filter.owner", "Tulajdonos");
        put("hu", "map.filter.semester", "Szemeszter");
        put("hu", "map.filter.name", "Név");
        put("hu", "map.filter.reset", "Törlés");
        put("hu", "map.details.empty", "Válasszon objektumot a térképen");
        put("hu", "map.details.groupsOnFloor", "Objektumok a szinten");
        put("hu", "map.error.imageLoad", "Nem sikerült betölteni a szint tervét");
        put("hu", "map.legend.own", "Saját objektumok");
        put("hu", "map.legend.others", "Más felhasználók");
        put("hu", "map.legend.hint", "A jelölő mérete ∝ hallgatók száma");
        put("hu", "map.legend.click", "kattintás a jelölőre — részletek");
        put("hu", "cmd.help", "Súgó");
        put("hu", "map.object.info", "Objektum adatai");
        put("hu", "map.object.room", "Terem");
        put("hu", "map.object.owner", "Tulajdonos");
        put("hu", "map.object.edit", "Szerkesztés");
        put("hu", "map.object.delete", "Törlés");
        put("hu", "map.object.field.id", "ID");
        put("hu", "map.object.field.name", "Név");
        put("hu", "map.object.field.coordinates", "Koordináták");
        put("hu", "map.object.field.creationDate", "Létrehozás dátuma");
        put("hu", "map.object.field.students", "Hallgatók");
        put("hu", "map.object.field.expelled", "Kizárt");
        put("hu", "map.object.field.transferred", "Áthelyezett");
        put("hu", "map.object.field.semester", "Szemeszter");
        put("hu", "map.object.field.admin", "Admin");
        put("hu", "table.title", "StudyGroup adatbázis");
        put("hu", "table.filters", "Szűrők");
        put("hu", "table.page", "Oldal");
        put("hu", "table.prev", "Előző");
        put("hu", "table.next", "Következő");
        put("hu", "table.rows", "sor");
        put("hu", "table.openMap", "Megnyitás térképen");
        put("hu", "table.add", "Hozzáadás");
        put("hu", "table.edit", "Szerkesztés");
        put("hu", "table.delete", "Törlés");
        put("hu", "table.col.id", "id");
        put("hu", "table.col.name", "name");
        put("hu", "table.col.coordX", "coord.x");
        put("hu", "table.col.coordY", "coord.y");
        put("hu", "table.col.creationDate", "creationDate");
        put("hu", "table.col.students", "students");
        put("hu", "table.col.expelled", "expelled");
        put("hu", "table.col.transferred", "transferred");
        put("hu", "table.col.semester", "semester");
        put("hu", "table.col.adminName", "admin.name");
        put("hu", "table.col.adminBirthday", "admin.birthday");
        put("hu", "table.col.eyeColor", "admin.eyeColor");
        put("hu", "table.col.nationality", "admin.nationality");
        put("hu", "table.col.ownerId", "ownerId");
        put("hu", "table.col.room", "room");
        put("hu", "editor.title", "Csoport szerkesztése");
        put("hu", "editor.save", "Mentés");
        put("hu", "editor.cancel", "Mégse");
        put("hu", "editor.field.name", "Név");
        put("hu", "editor.field.floor", "Szint (épület)");
        put("hu", "editor.field.coordX", "Koordináta X");
        put("hu", "editor.field.coordY", "Koordináta Y");
        put("hu", "editor.coordHint", "Az X jelöli a szintet: 0–199 → 1, 200–399 → 2, …, 800–1000 → 5. Az Y a helyzet a terven (0–1000).");
        put("hu", "editor.field.students", "Hallgatók száma");
        put("hu", "editor.field.expelled", "Kizárt hallgatók");
        put("hu", "editor.field.transferred", "Átjelentkezett hallgatók");
        put("hu", "editor.field.semester", "Szemeszter");
        put("hu", "editor.field.adminName", "Admin neve");
        put("hu", "editor.field.adminBirthday", "Születési dátum");
        put("hu", "editor.field.eyeColor", "Szemszín");
        put("hu", "editor.field.nationality", "Állampolgárság");
        put("hu", "editor.invalid", "Érvénytelen mezők");
        put("hu", "msg.loginRequired", "Ehhez a művelethez bejelentkezés kell.");
        put("hu", "msg.loadFailed", "Hiba adatlekéréskor");
        put("hu", "msg.noRoom", "Nincs terem-hozzárendelés");
        put("hu", "msg.unknownCommand", "Ismeretlen vagy hibás parancs");
        put("hu", "msg.selectGroupFirst", "Előbb válasszon objektumot");
        put("hu", "msg.collectionUpdated", "Gyűjtemény frissítve");
        put("hu", "msg.mockMode", "Mock mód aktív. A --mock csak helyi UI teszthez.");
        put("hu", "msg.editDenied", "Idegen objektum nem szerkeszthető");
        put("hu", "msg.deleteDenied", "Idegen objektum nem törölhető");
        put("hu", "cmd.help.text", "Parancsok: info, show, clear, remove_first, remove_by_id [id], add, add_if_min, update [id], remove_lower, filter_contains_name <name>, filter_greater_than_semester_enum <SEMESTER>, print_field_descending_group_admin, execute_script <file>, help, logout, exit");

        put("en-IE", "app.title", "ITMO StudyGroup Client");
        put("en-IE", "nav.map", "Map");
        put("en-IE", "nav.table", "Table");
        put("en-IE", "nav.logout", "Logout");
        put("en-IE", "cmd.manual", "Manual command input");
        put("en-IE", "cmd.run", "Run");
        put("en-IE", "cmd.add", "add");
        put("en-IE", "cmd.update", "update");
        put("en-IE", "cmd.removeById", "remove_by_id");
        put("en-IE", "user", "User");
        put("en-IE", "status.online", "Sync: online");
        put("en-IE", "auth.title", "Login / registration");
        put("en-IE", "auth.login", "Login");
        put("en-IE", "auth.register", "Register");
        put("en-IE", "auth.username", "Username");
        put("en-IE", "auth.password", "Password");
        put("en-IE", "auth.submit", "Continue");
        put("en-IE", "auth.locale", "Locale");
        put("en-IE", "auth.subtitle", "ITMO building, 9 Lomonosova St. — interactive floor map and collection");
        put("en-IE", "auth.error.empty", "Enter username and password");
        put("en-IE", "auth.connecting", "Connecting to server…");
        put("en-IE", "auth.error.network", "Server not responding. Check tunnel and Helios server.");
        put("en-IE", "map.building", "ITMO Building · 9 Lomonosova St.");
        put("en-IE", "map.floor.short", "Floor");
        put("en-IE", "map.title", "ITMO building (Lomonosova 9), floor");
        put("en-IE", "cmd.section.collection", "Collection");
        put("en-IE", "cmd.section.crud", "Objects");
        put("en-IE", "cmd.section.modify", "Modify");
        put("en-IE", "cmd.section.filters", "Filters");
        put("en-IE", "cmd.section.other", "Other");
        put("en-IE", "cmd.show", "Show collection");
        put("en-IE", "cmd.info", "Info");
        put("en-IE", "cmd.addIfMin", "add_if_min");
        put("en-IE", "cmd.removeLower", "remove_lower");
        put("en-IE", "cmd.clear", "clear");
        put("en-IE", "cmd.removeFirst", "remove_first");
        put("en-IE", "cmd.removeById", "remove_by_id");
        put("en-IE", "cmd.filterName", "filter_contains_name");
        put("en-IE", "cmd.filterName.prompt", "Name substring");
        put("en-IE", "cmd.filterSemester", "filter_greater_than_semester");
        put("en-IE", "cmd.printAdmins", "print_field_descending_group_admin");
        put("en-IE", "cmd.executeScript", "execute_script");
        put("en-IE", "cmd.refresh", "Refresh from server");
        put("en-IE", "welcome.morning", "Good morning, %s");
        put("en-IE", "welcome.afternoon", "Good afternoon, %s");
        put("en-IE", "welcome.evening", "Good evening, %s");
        put("en-IE", "welcome.night", "Good night, %s");
        put("en-IE", "dialog.confirm", "Confirm");
        put("en-IE", "cmd.confirm.clear", "Clear all your objects in the collection?");
        put("en-IE", "cmd.confirm.delete", "Delete the selected object?");
        put("en-IE", "map.floor", "Floor");
        put("en-IE", "map.filter.owner", "Owner");
        put("en-IE", "map.filter.semester", "Semester");
        put("en-IE", "map.filter.name", "Name");
        put("en-IE", "map.filter.reset", "Reset");
        put("en-IE", "map.details.empty", "Select an object on the map");
        put("en-IE", "map.details.groupsOnFloor", "Objects on floor");
        put("en-IE", "map.error.imageLoad", "Failed to load floor plan image");
        put("en-IE", "map.legend.own", "Your objects");
        put("en-IE", "map.legend.others", "Other users");
        put("en-IE", "map.legend.hint", "Marker size ∝ student count");
        put("en-IE", "map.legend.click", "click marker for details");
        put("en-IE", "cmd.help", "Help");
        put("en-IE", "map.object.info", "Object details");
        put("en-IE", "map.object.room", "Room");
        put("en-IE", "map.object.owner", "Owner");
        put("en-IE", "map.object.edit", "Edit");
        put("en-IE", "map.object.delete", "Delete");
        put("en-IE", "map.object.field.id", "ID");
        put("en-IE", "map.object.field.name", "Name");
        put("en-IE", "map.object.field.coordinates", "Coordinates");
        put("en-IE", "map.object.field.creationDate", "Creation date");
        put("en-IE", "map.object.field.students", "Students");
        put("en-IE", "map.object.field.expelled", "Expelled");
        put("en-IE", "map.object.field.transferred", "Transferred");
        put("en-IE", "map.object.field.semester", "Semester");
        put("en-IE", "map.object.field.admin", "Admin");
        put("en-IE", "table.title", "StudyGroup database");
        put("en-IE", "table.filters", "Filters");
        put("en-IE", "table.page", "Page");
        put("en-IE", "table.prev", "Prev");
        put("en-IE", "table.next", "Next");
        put("en-IE", "table.rows", "rows");
        put("en-IE", "table.openMap", "Open on map");
        put("en-IE", "table.add", "Add");
        put("en-IE", "table.edit", "Edit");
        put("en-IE", "table.delete", "Delete");
        put("en-IE", "table.col.id", "id");
        put("en-IE", "table.col.name", "name");
        put("en-IE", "table.col.coordX", "coord.x");
        put("en-IE", "table.col.coordY", "coord.y");
        put("en-IE", "table.col.creationDate", "creationDate");
        put("en-IE", "table.col.students", "students");
        put("en-IE", "table.col.expelled", "expelled");
        put("en-IE", "table.col.transferred", "transferred");
        put("en-IE", "table.col.semester", "semester");
        put("en-IE", "table.col.adminName", "admin.name");
        put("en-IE", "table.col.adminBirthday", "admin.birthday");
        put("en-IE", "table.col.eyeColor", "admin.eyeColor");
        put("en-IE", "table.col.nationality", "admin.nationality");
        put("en-IE", "table.col.ownerId", "ownerId");
        put("en-IE", "table.col.room", "room");
        put("en-IE", "editor.title", "Edit study group");
        put("en-IE", "editor.save", "Save");
        put("en-IE", "editor.cancel", "Cancel");
        put("en-IE", "editor.field.name", "Name");
        put("en-IE", "editor.field.floor", "Floor (building)");
        put("en-IE", "editor.field.coordX", "Coordinate X");
        put("en-IE", "editor.field.coordY", "Coordinate Y");
        put("en-IE", "editor.coordHint", "X selects floor: 0–199 → 1, 200–399 → 2, …, 800–1000 → 5. Y is position on plan (0–1000).");
        put("en-IE", "editor.field.students", "Students count");
        put("en-IE", "editor.field.expelled", "Expelled students");
        put("en-IE", "editor.field.transferred", "Transferred students");
        put("en-IE", "editor.field.semester", "Semester");
        put("en-IE", "editor.field.adminName", "Admin name");
        put("en-IE", "editor.field.adminBirthday", "Birthday");
        put("en-IE", "editor.field.eyeColor", "Eye color");
        put("en-IE", "editor.field.nationality", "Nationality");
        put("en-IE", "editor.invalid", "Invalid fields");
        put("en-IE", "msg.loginRequired", "Authorization is required.");
        put("en-IE", "msg.loadFailed", "Failed to load data");
        put("en-IE", "msg.noRoom", "Room is not detected");
        put("en-IE", "msg.unknownCommand", "Unknown or invalid command");
        put("en-IE", "msg.selectGroupFirst", "Select a group first");
        put("en-IE", "msg.collectionUpdated", "Collection updated");
        put("en-IE", "msg.mockMode", "Mock mode enabled. Use --mock only for local UI tests.");
        put("en-IE", "msg.editDenied", "Editing foreign objects is not allowed");
        put("en-IE", "msg.deleteDenied", "Deleting foreign objects is not allowed");
        put("en-IE", "cmd.help.text", "Commands: info, show, clear, remove_first, remove_by_id [id], add, add_if_min, update [id], remove_lower, filter_contains_name <name>, filter_greater_than_semester_enum <SEMESTER>, print_field_descending_group_admin, execute_script <file>, help, logout, exit");

        put("ru", "cmd.restoreFilter", "Сбросить фильтр");
        put("ru", "dialog.error", "Ошибка");
        put("ru", "dialog.info", "Информация");
        put("ru", "room.auditorium", "Ауд. %s");
        put("ru", "room.cafeteria", "Столовая");
        put("ru", "room.wc", "Санузел");
        put("ru", "room.corridor", "Коридор (нижний)");
        put("ru", "locale.ru", "Русский");
        put("ru", "locale.be_BY", "Беларуская");
        put("ru", "locale.hu", "Венгерский");
        put("ru", "locale.en_IE", "Английский (IE)");

        I18nExtended.registerAll();
    }

    /** Регистрация ключей из {@link I18nExtended}. */
    static void registerKey(String langTag, String key, String value) {
        put(langTag, key, value);
    }

    private I18n() {
    }

    private static void put(String langTag, String key, String value) {
        BUNDLE.computeIfAbsent(langTag, k -> new HashMap<>()).put(key, value);
    }

    public static String tr(Locale locale, String key) {
        String langTag = locale.toLanguageTag();
        Map<String, String> m = BUNDLE.get(langTag);
        if (m != null && m.containsKey(key)) {
            return m.get(key);
        }
        String language = locale.getLanguage();
        Map<String, String> byLanguage = BUNDLE.get(language);
        if (byLanguage != null && byLanguage.containsKey(key)) {
            return byLanguage.get(key);
        }
        Map<String, String> en = BUNDLE.get("en-IE");
        if (en != null && en.containsKey(key)) {
            return en.get(key);
        }
        Map<String, String> fallback = BUNDLE.get("ru");
        return fallback == null ? key : fallback.getOrDefault(key, key);
    }

    public static String localeName(Locale displayIn, Locale target) {
        if (target == null) {
            return "";
        }
        String tag = target.toLanguageTag().replace('-', '_');
        return tr(displayIn, "locale." + tag);
    }

    public static String semester(Locale locale, Semester value) {
        return value == null ? "" : tr(locale, "enum.semester." + value.name());
    }

    public static String color(Locale locale, Color value) {
        return value == null ? "" : tr(locale, "enum.color." + value.name());
    }

    public static String country(Locale locale, Country value) {
        return value == null ? "" : tr(locale, "enum.country." + value.name());
    }

    public static String tr(Locale locale, String key, Object... args) {
        String template = tr(locale, key);
        if (args == null || args.length == 0) {
            return template;
        }
        return String.format(locale, template, args);
    }

    public static String formatDate(Locale locale, LocalDate date) {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
        return formatter.format(date);
    }

    public static String formatDate(Locale locale, Date date) {
        if (date == null) {
            return "";
        }
        return formatDate(locale, date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public static String formatNumber(Locale locale, Number n) {
        if (n == null) {
            return "";
        }
        return java.text.NumberFormat.getNumberInstance(locale).format(n);
    }

    public static String formatDateTime(Locale locale, LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale);
        return formatter.format(dateTime);
    }
}
