package coffeefactory.json;

import coffeefactory.common.Io;
import coffeefactory.io.AbstractWriter;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.validation.ErrorLogger;
import coffeefactory.validation.RecordLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Чтение и запись каталога в JSON.
 */
public class JsonHandler extends AbstractWriter<CoffeeMachine> {
    private final JsonCodec codec = new JsonCodec();
    private final RecordLoader loader;

    public JsonHandler(ErrorLogger logger) {
        this.loader = new RecordLoader(logger);
    }

    @Override
    public List<CoffeeMachine> read(Path path) {
        String content = Io.readString(path);
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        if (content.trim().isEmpty()) {
            return new ArrayList<CoffeeMachine>();
        }
        List<Map<String, String>> objects = codec.parse(content);
        List<CoffeeMachine> result = new ArrayList<CoffeeMachine>();
        for (Map<String, String> fields : objects) {
            loader.consume(result, fields, String.valueOf(fields));
        }
        return result;
    }

    @Override
    public void write(Path path, List<CoffeeMachine> items) {
        Io.writeString(path, codec.write(items));
    }
}
