package common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Вспомогательные методы сериализации объектов для отправки по сети.
 * Используется как на сервере, так и на клиенте.
 */
public final class SerializationUtils {

    private SerializationUtils() {
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }

        return baos.toByteArray();
    }
}

