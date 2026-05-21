package common.dto;

import java.io.Serializable;

/**
 * Базовый маркерный интерфейс для всех объектов-команд,
 * которые передаются между клиентом и сервером в сериализованном виде.
 *
 * Каждый конкретный тип команды (add, remove_by_id, show и т.п.)
 * должен иметь собственный класс, реализующий данный интерфейс.
 */
public interface CommandDTO extends Serializable {
}

