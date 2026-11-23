package coffeefactory.io;

import coffeefactory.common.DataAccessException;
import coffeefactory.common.Io;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.validation.ErrorLogger;
import coffeefactory.validation.RecordLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextFileHandler extends AbstractWriter<CoffeeMachine> {
    private final RecordLoader loader;
    private final ErrorLogger logger;

    public TextFileHandler(ErrorLogger logger) {
        this.logger = logger;
        this.loader = new RecordLoader(logger);
    }

    @Override
    public List<CoffeeMachine> read(Path path) {
        List<CoffeeMachine> result = new ArrayList<CoffeeMachine>();
        if (path == null || !Files.exists(path)) {
            logger.log("Файл не найден, коллекция пуста", path == null ? "" : path.toString());
            return result;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось прочитать " + path, exception);
        }
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String trimmed = stripBom(line).trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (trimmed.toLowerCase(Locale.ROOT).startsWith("id;")) {
                continue;
            }
            Map<String, String> fields = split(trimmed);
            if (fields == null) {
                logger.log("Неверное число полей, строка пропущена", trimmed);
                continue;
            }
            loader.consume(result, fields, trimmed);
        }
        return result;
    }

    @Override
    public void write(Path path, List<CoffeeMachine> items) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < CoffeeMachine.COLUMNS.length; i++) {
            if (i > 0) {
                builder.append(';');
            }
            builder.append(CoffeeMachine.COLUMNS[i]);
        }
        builder.append('\n');
        if (items != null) {
            for (CoffeeMachine machine : items) {
                builder.append(machine.toFileLine()).append('\n');
            }
        }
        Io.writeString(path, builder.toString());
    }

    static Map<String, String> split(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != CoffeeMachine.COLUMNS.length) {
            return null;
        }
        Map<String, String> fields = new LinkedHashMap<String, String>();
        for (int i = 0; i < CoffeeMachine.COLUMNS.length; i++) {
            fields.put(CoffeeMachine.COLUMNS[i], parts[i].trim());
        }
        return fields;
    }

    private static String stripBom(String line) {
        if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }
}
