package util;

/**
 * Генератор уникальных id для StudyGroup.
 */

public class IdGenerator {
  
    public static int currentId = 1;

    /**
     * Возвращает следующий уникальный id.
     */

    public static int generateId() {
        return currentId++;
    }

    /**
     * Используется при загрузке из файла,
     * чтобы установить максимальный существующий id.
     */
    
    public static void updateCurrentId(int id) {
        if (id >= currentId) {
            currentId = id + 1;
        }
    }


}
