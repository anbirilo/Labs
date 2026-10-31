package coffeefactory.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Абстрактный класс для работы с файлом данных (п. 2, 4).
 * Наследники конкретизируют формат и направление (чтение / запись).
 */
public abstract class AbstractFileHandler {
    protected static final Charset CHARSET = StandardCharsets.UTF_8;

    protected final Path path;

    protected AbstractFileHandler(Path path) {
        this.path = path;
    }

    /** Название формата для сообщений, например "TXT". */
    public abstract String getFormat();

    public Path getPath() {
        return path;
    }

    public boolean exists() {
        return Files.exists(path);
    }
}
