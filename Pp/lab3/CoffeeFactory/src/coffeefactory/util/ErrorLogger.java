package coffeefactory.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Запись об ошибке и некорректной строке: на консоль и в *.log файл (п. 7).
 */
public class ErrorLogger {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path logFile;

    public ErrorLogger(Path logFile) {
        this.logFile = logFile;
    }

    public Path getLogFile() {
        return logFile;
    }

    /**
     * @param message описание ошибки
     * @param badLine некорректная строка (может быть null)
     */
    public void log(String message, String badLine) {
        System.out.println("[ОШИБКА] " + message);
        if (badLine != null) {
            System.out.println("         Некорректная строка: " + badLine);
        }
        try {
            Path parent = logFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String record = LocalDateTime.now().format(TIME) + " | " + message
                    + (badLine != null ? " | строка: " + badLine : "")
                    + System.lineSeparator();
            Files.writeString(logFile, record, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Не удалось записать в лог-файл: " + e.getMessage());
        }
    }
}
