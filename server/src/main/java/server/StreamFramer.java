package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Кадрирование потока TCP: 4 байта (big-endian) длины + payload.
 * Нужно, чтобы UDP-датаграммы не склеивались в SSH-туннеле.
 */
public final class StreamFramer {

    private StreamFramer() {
    }

    public static void writeFrame(OutputStream out, byte[] payload) throws IOException {
        int len = payload.length;
        out.write((len >>> 24) & 0xFF);
        out.write((len >>> 16) & 0xFF);
        out.write((len >>> 8) & 0xFF);
        out.write(len & 0xFF);
        out.write(payload);
        out.flush();
    }

    /**
     * @return payload или {@code null} при закрытии потока до чтения длины
     */
    public static byte[] readFrame(InputStream in) throws IOException {
        int b0 = in.read();
        if (b0 < 0) {
            return null;
        }
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        if (b1 < 0 || b2 < 0 || b3 < 0) {
            throw new EOFException("truncated frame length");
        }
        int len = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        if (len < 0 || len > 512 * 1024) {
            throw new IOException("invalid frame length: " + len);
        }
        byte[] payload = in.readNBytes(len);
        if (payload.length != len) {
            throw new EOFException("truncated frame body");
        }
        return payload;
    }
}
