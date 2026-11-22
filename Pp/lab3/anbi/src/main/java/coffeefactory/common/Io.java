package coffeefactory.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Io {
    private Io() {
    }

    public static byte[] readBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось прочитать файл " + path, exception);
        }
    }

    public static String readString(Path path) {
        return new String(readBytes(path), StandardCharsets.UTF_8);
    }

    public static void writeBytes(Path path, byte[] data) {
        ensureParent(path);
        try {
            Files.write(path, data);
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось записать файл " + path, exception);
        }
    }

    public static void writeString(Path path, String content) {
        writeBytes(path, content.getBytes(StandardCharsets.UTF_8));
    }

    public static void ensureParent(Path path) {
        Path parent = path.getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось создать каталог " + parent, exception);
        }
    }
}
