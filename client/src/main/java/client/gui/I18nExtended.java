package client.gui;

import model.Color;
import model.Country;
import model.Semester;

/**
 * Дополнительные ключи локализации (полный паритет ru / be-BY / hu / en-IE).
 */
final class I18nExtended {

    private I18nExtended() {
    }

    static void registerAll() {
        registerEnums();
        registerRooms();
        registerCommandLabels();
        registerEditorAndMap();
        registerMessages();
    }

    private static void registerEnums() {
        for (Semester s : Semester.values()) {
            p("ru", "enum.semester." + s.name(), semesterRu(s));
            p("be-BY", "enum.semester." + s.name(), semesterBe(s));
            p("hu", "enum.semester." + s.name(), semesterHu(s));
            p("en-IE", "enum.semester." + s.name(), semesterEn(s));
        }
        for (Color c : Color.values()) {
            p("ru", "enum.color." + c.name(), colorRu(c));
            p("be-BY", "enum.color." + c.name(), colorBe(c));
            p("hu", "enum.color." + c.name(), colorEn(c));
            p("en-IE", "enum.color." + c.name(), colorEn(c));
        }
        for (Country c : Country.values()) {
            p("ru", "enum.country." + c.name(), countryRu(c));
            p("be-BY", "enum.country." + c.name(), countryBe(c));
            p("hu", "enum.country." + c.name(), countryHu(c));
            p("en-IE", "enum.country." + c.name(), countryEn(c));
        }
        p("ru", "locale.ru", "Русский");
        p("be-BY", "locale.ru", "Рускай");
        p("hu", "locale.ru", "Orosz");
        p("en-IE", "locale.ru", "Russian");
        p("ru", "locale.be_BY", "Беларуская");
        p("be-BY", "locale.be_BY", "Беларуская");
        p("hu", "locale.be_BY", "Belorusz");
        p("en-IE", "locale.be_BY", "Belarusian");
        p("ru", "locale.hu", "Венгерский");
        p("be-BY", "locale.hu", "Венгерская");
        p("hu", "locale.hu", "Magyar");
        p("en-IE", "locale.hu", "Hungarian");
        p("ru", "locale.en_IE", "Английский (IE)");
        p("be-BY", "locale.en_IE", "Англійская (IE)");
        p("hu", "locale.en_IE", "Angol (IE)");
        p("en-IE", "locale.en_IE", "English (IE)");
    }

    private static void registerRooms() {
        p("ru", "room.auditorium", "Ауд. %s");
        p("be-BY", "room.auditorium", "Ауд. %s");
        p("hu", "room.auditorium", "Terem %s");
        p("en-IE", "room.auditorium", "Room %s");
        p("ru", "room.cafeteria", "Столовая");
        p("be-BY", "room.cafeteria", "Сталовая");
        p("hu", "room.cafeteria", "Étkező");
        p("en-IE", "room.cafeteria", "Cafeteria");
        p("ru", "room.wc", "Санузел");
        p("be-BY", "room.wc", "Санвузел");
        p("hu", "room.wc", "Mosdó");
        p("en-IE", "room.wc", "Restroom");
        p("ru", "room.corridor", "Коридор (нижний)");
        p("be-BY", "room.corridor", "Калідор (ніжні)");
        p("hu", "room.corridor", "Folyosó (alsó)");
        p("en-IE", "room.corridor", "Corridor (lower)");
    }

    private static void registerCommandLabels() {
        p("be-BY", "cmd.filterName", "Фільтр па назве");
        p("hu", "cmd.filterName", "Szűrés név szerint");
        p("en-IE", "cmd.filterName", "Filter by name");
        p("be-BY", "cmd.filterSemester", "Фільтр па семестры");
        p("hu", "cmd.filterSemester", "Szűrés szemeszter szerint");
        p("en-IE", "cmd.filterSemester", "Filter by semester");
        p("be-BY", "cmd.info", "Звесткі пра калекцыю");
        p("hu", "cmd.info", "Gyűjtemény adatai");
        p("en-IE", "cmd.info", "Collection info");
        p("be-BY", "cmd.show", "Паказаць калекцыю");
        p("hu", "cmd.show", "Gyűjtemény megjelenítése");
        p("be-BY", "cmd.printAdmins", "Адміны (па змяншэнні)");
        p("hu", "cmd.printAdmins", "Adminok (csökkenő)");
        p("be-BY", "cmd.executeScript", "Выканать скрыпт");
        p("hu", "cmd.executeScript", "Szkript futtatása");
        p("be-BY", "cmd.add", "Дадаць");
        p("hu", "cmd.add", "Hozzáadás");
        p("en-IE", "cmd.add", "Add");
        p("be-BY", "cmd.update", "Абнавіць");
        p("hu", "cmd.update", "Frissítés");
        p("en-IE", "cmd.update", "Update");
        p("be-BY", "cmd.addIfMin", "Дадаць, калі мінімальнае");
        p("hu", "cmd.addIfMin", "Hozzáadás, ha minimális");
        p("en-IE", "cmd.addIfMin", "Add if minimum");
        p("be-BY", "cmd.removeLower", "Выдаліць меншыя");
        p("hu", "cmd.removeLower", "Kisebbek törlése");
        p("en-IE", "cmd.removeLower", "Remove lower");
        p("be-BY", "cmd.clear", "Ачысціць свае аб'екты");
        p("hu", "cmd.clear", "Saját objektumok törlése");
        p("en-IE", "cmd.clear", "Clear your objects");
        p("be-BY", "cmd.removeFirst", "Выдаліць першы");
        p("hu", "cmd.removeFirst", "Első törlése");
        p("en-IE", "cmd.removeFirst", "Remove first");
        p("be-BY", "cmd.removeById", "Выдаліць па ID");
        p("hu", "cmd.removeById", "Törlés ID alapján");
        p("en-IE", "cmd.removeById", "Remove by ID");
        p("be-BY", "cmd.confirm.removeFirst", "Выдаліць першы ваш аб'ект?");
        p("hu", "cmd.confirm.removeFirst", "Törli az első saját objektumot?");
        p("en-IE", "cmd.confirm.removeFirst", "Remove your first object in the collection?");
        p("be-BY", "cmd.restoreFilter", "Скінуць фільтр");
        p("hu", "cmd.restoreFilter", "Szűrő törlése");
        p("en-IE", "cmd.restoreFilter", "Reset filter");
        p("be-BY", "cmd.filterSemester.hint", "Паказаць групы з семестрам большым за выбраны");
        p("hu", "cmd.filterSemester.hint", "Nagyobb szemeszterű csoportok");
        p("en-IE", "cmd.filterSemester.hint", "Show groups with semester greater than selected");
    }

    private static void registerEditorAndMap() {
        String[][] keys = {
                {"editor.room.selectFloorFirst", "Спачатку выберыце паверх", "Előbb válasszon szintet", "Select floor first"},
                {"editor.field.room", "Аўдыторыя / памяшканне", "Terem / helyiség", "Room / space"},
                {"editor.coordsPreview", "Памяшканне: %s · паверх %d · каардынаты: X=%d, Y=%.0f",
                        "Helyiség: %s · szint %d · koordináták: X=%d, Y=%.0f",
                        "Room: %s · floor %d · coordinates: X=%d, Y=%.0f"},
                {"editor.error.name", "Укажыце назву групы", "Adja meg a csoport nevét", "Enter group name"},
                {"editor.error.room", "Выберыце аўдыторыю", "Válasszon termet", "Select a room"},
                {"editor.error.students", "Колькасць студэнтаў павінна быць > 0",
                        "A hallgatók számának > 0-nak kell lennie", "Students count must be > 0"},
                {"editor.error.transferred", "Пераведзеныя павінны быць > 0",
                        "Az áthelyezettek számának > 0-nak kell lennie", "Transferred count must be > 0"},
                {"editor.error.floor", "Выберыце паверх (1–5)", "Válasszon szintet (1–5)", "Select floor (1–5)"},
                {"editor.error.adminName", "Укажыце імя адміністратара", "Adja meg az admin nevét", "Enter administrator name"},
                {"editor.error.birthday", "Укажыце дату нараджэння", "Adja meg a születési dátumot", "Enter birthday"},
                {"editor.error.expelledNegative", "Адлічаныя не могуць быць адмоўнымі",
                        "A kizártak száma nem lehet negatív", "Expelled count cannot be negative"},
                {"editor.error.expelledPositive", "Калі ўказаны адлічаныя, значэнне павінна быць > 0 (0 — не ўказваць)",
                        "Ha megad kizártakat, az érték > 0 legyen (0 = nincs megadva)",
                        "If expelled is set, value must be > 0 (0 means not set)"},
                {"editor.error.spinnerInvalid", "Увядзіце карэктныя цэлыя лікі",
                        "Adjon meg helyes egész számokat", "Enter valid integers in numeric fields"},
                {"editor.error.generic", "Памылка: %s", "Hiba: %s", "Error: %s"},
                {"editor.coordHint", "Каардынаты вылічваюцца аўтаматычна па аўдыторыі.",
                        "A koordináták a terem alapján automatikusan számolódnak.",
                        "Coordinates are computed from the selected room."},
                {"map.zoom.in", "Павялічыць", "Nagyítás", "Zoom in"},
                {"map.zoom.out", "Паменшыць", "Kicsinyítés", "Zoom out"},
                {"map.zoom.reset", "Скінуць", "Alaphelyzet", "Reset"},
                {"map.zoom.level", "Маштаб: %d%%", "Nagyítás: %d%%", "Zoom: %d%%"},
                {"map.zoom.hint", "Кола — маштаб · ПКМ — рух", "Görgő — zoom · RMB — mozgatás",
                        "Wheel — zoom · RMB — pan"},
                {"info.placement.header", "Размяшчэнне груп па аўдыторыях:",
                        "Csoportok elhelyezése termenként:", "Groups by room:"},
                {"info.placement.empty", "Калекцыя пустая.", "A gyűjtemény üres.", "Collection is empty."},
                {"info.placement.count", "Усяго аб'ектаў: %d", "Összesen: %d objektum", "Total objects: %d"},
                {"info.placement.roomLine", "Паверх %d · %s", "Szint %d · %s", "Floor %d · %s"},
                {"info.placement.owner", "уладальнік", "tulajdonos", "owner"},
                {"info.placement.students", "студэнтаў", "hallgató", "students"},
                {"map.details.hint", "%d · %s", "%d · %s", "%d · %s"},
        };
        for (String[] row : keys) {
            p("be-BY", row[0], row[1]);
            p("hu", row[0], row[2]);
            p("en-IE", row[0], row[3]);
        }
    }

    private static void registerMessages() {
        p("be-BY", "msg.filterResult", "Знойдзена элементаў: %d");
        p("hu", "msg.filterResult", "Találat: %d");
        p("en-IE", "msg.filterResult", "Items found: %d");
        p("be-BY", "msg.multilineResult", "Вынік: %d радкоў (падрабязнасці ў дыялогу)");
        p("hu", "msg.multilineResult", "Eredmény: %d sor (részletek a párbeszédablakban)");
        p("en-IE", "msg.multilineResult", "Result: %d lines (see dialog)");
        p("be-BY", "msg.commandFailed", "Каманда не выканана");
        p("be-BY", "msg.commandInProgress", "Пачакайце, выконваецца папярэдняя каманда…");
        p("hu", "msg.commandFailed", "A parancs sikertelen");
        p("hu", "msg.commandInProgress", "Várjon, folyamatban van egy parancs…");
        p("en-IE", "msg.commandFailed", "Command failed");
        p("en-IE", "msg.commandInProgress", "Please wait, a command is still running…");
        p("be-BY", "msg.noAdmins", "Адміністратары не знойдзены");
        p("hu", "msg.noAdmins", "Nincs admin");
        p("en-IE", "msg.noAdmins", "No administrators found");
        p("be-BY", "msg.mockScript", "Mock: execute_script — толькі з серверам");
        p("hu", "msg.mockScript", "Mock: execute_script — csak szerverrel");
        p("en-IE", "msg.mockScript", "Mock: execute_script — server only");
        p("be-BY", "dialog.error", "Памылка");
        p("hu", "dialog.error", "Hiba");
        p("en-IE", "dialog.error", "Error");
        p("be-BY", "dialog.info", "Інфармацыя");
        p("hu", "dialog.info", "Információ");
        p("en-IE", "dialog.info", "Information");
    }

    private static void p(String lang, String key, String value) {
        I18n.registerKey(lang, key, value);
    }

    private static String semesterRu(Semester s) {
        return switch (s) {
            case FIRST -> "1-й";
            case SECOND -> "2-й";
            case THIRD -> "3-й";
            case FOURTH -> "4-й";
            case FIFTH -> "5-й";
            case SIXTH -> "6-й";
            case SEVENTH -> "7-й";
            case EIGHTH -> "8-й";
        };
    }

    private static String semesterBe(Semester s) {
        return switch (s) {
            case FIRST -> "1-ы";
            case SECOND -> "2-і";
            case THIRD -> "3-і";
            case FOURTH -> "4-ы";
            case FIFTH -> "5-ы";
            case SIXTH -> "6-ы";
            case SEVENTH -> "7-ы";
            case EIGHTH -> "8-ы";
        };
    }

    private static String semesterHu(Semester s) {
        return switch (s) {
            case FIRST -> "1.";
            case SECOND -> "2.";
            case THIRD -> "3.";
            case FOURTH -> "4.";
            case FIFTH -> "5.";
            case SIXTH -> "6.";
            case SEVENTH -> "7.";
            case EIGHTH -> "8.";
        };
    }

    private static String semesterEn(Semester s) {
        return switch (s) {
            case FIRST -> "1st";
            case SECOND -> "2nd";
            case THIRD -> "3rd";
            case FOURTH -> "4th";
            case FIFTH -> "5th";
            case SIXTH -> "6th";
            case SEVENTH -> "7th";
            case EIGHTH -> "8th";
        };
    }

    private static String colorRu(Color c) {
        return switch (c) {
            case BLACK -> "Чёрный";
            case BLUE -> "Синий";
            case ORANGE -> "Оранжевый";
            case WHITE -> "Белый";
            case GREEN -> "Зелёный";
            case BROWN -> "Коричневый";
        };
    }

    private static String colorBe(Color c) {
        return switch (c) {
            case BLACK -> "Чорны";
            case BLUE -> "Сіні";
            case ORANGE -> "Аранжавы";
            case WHITE -> "Белы";
            case GREEN -> "Зялёны";
            case BROWN -> "Карычневы";
        };
    }

    private static String colorEn(Color c) {
        return switch (c) {
            case BLACK -> "Black";
            case BLUE -> "Blue";
            case ORANGE -> "Orange";
            case WHITE -> "White";
            case GREEN -> "Green";
            case BROWN -> "Brown";
        };
    }

    private static String countryRu(Country c) {
        return switch (c) {
            case RUSSIA -> "Россия";
            case GERMANY -> "Германия";
            case USA -> "США";
            case SPAIN -> "Испания";
        };
    }

    private static String countryBe(Country c) {
        return switch (c) {
            case RUSSIA -> "Расія";
            case GERMANY -> "Германія";
            case USA -> "ЗША";
            case SPAIN -> "Іспанія";
        };
    }

    private static String countryHu(Country c) {
        return switch (c) {
            case RUSSIA -> "Oroszország";
            case GERMANY -> "Németország";
            case USA -> "USA";
            case SPAIN -> "Spanyolország";
        };
    }

    private static String countryEn(Country c) {
        return switch (c) {
            case RUSSIA -> "Russia";
            case GERMANY -> "Germany";
            case USA -> "USA";
            case SPAIN -> "Spain";
        };
    }
}
