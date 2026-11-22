package coffeefactory.validation;

import coffeefactory.common.DataAccessException;
import coffeefactory.common.Io;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Журнал некорректных строк: консоль и файл *.log.
 */
public class ErrorLogger {
    private final Path logFile;
    private final boolean echoToConsole;

    public ErrorLogger(Path logFile, boolean echoToConsole) {
        this.logFile = logFile;
        this.echoToConsole = echoToConsole;
    }

    public static ErrorLogger silent() {
        return new ErrorLogger(null, false);
    }

    public void reset() {
        if (logFile == null) {
            return;
        }
        Io.writeBytes(logFile, new byte[0]);
    }

    public void log(String message, String invalidData) {
        String entry = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                + " | " + message
                + " | строка: " + (invalidData == null ? "" : invalidData)
                + System.lineSeparator();
        if (echoToConsole) {
            System.out.print(entry);
        }
        if (logFile == null) {
            return;
        }
        try {
            byte[] previous = java.nio.file.Files.exists(logFile) ? Io.readBytes(logFile) : new byte[0];
            byte[] addition = entry.getBytes(StandardCharsets.UTF_8);
            byte[] next = new byte[previous.length + addition.length];
            System.arraycopy(previous, 0, next, 0, previous.length);
            System.arraycopy(addition, 0, next, previous.length, addition.length);
            Io.writeBytes(logFile, next);
        } catch (DataAccessException exception) {
            System.out.println("Не удалось записать журнал: " + exception.getMessage());
        }
    }
}
