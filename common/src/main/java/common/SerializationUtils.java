package common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

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

    public static ByteBuffer serializeToBuffer(Object obj) throws IOException {
        byte[] data = serialize(obj);
        return ByteBuffer.wrap(data);
    }
}

