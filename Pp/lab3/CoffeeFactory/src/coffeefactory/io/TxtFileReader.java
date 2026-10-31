package coffeefactory.io;

import coffeefactory.model.CoffeeMaker;
import coffeefactory.util.ErrorLogger;
import coffeefactory.util.ValidationException;
import coffeefactory.util.Validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Чтение кофеварок из текстового файла (п. 4) с проверкой данных (п. 7).
 * Формат строки: id;тип;модель;мощность;объём бака;дата выпуска;цена
 * Пустые строки и строки, начинающиеся с '#', пропускаются.
 * Некорректная строка целиком не читается: ошибка и строка пишутся на консоль и в *.log.
 */
public class TxtFileReader extends AbstractFileHandler {
    private final ErrorLogger logger;

    public TxtFileReader(Path path, ErrorLogger logger) {
        super(path);
        this.logger = logger;
    }

    @Override
    public String getFormat() {
        return "TXT";
    }

    public List<CoffeeMaker> read() throws IOException {
        List<CoffeeMaker> result = new ArrayList<>();
        Set<Integer> usedIds = new HashSet<>();
        List<String> lines = Files.readAllLines(path, CHARSET);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            try {
                CoffeeMaker maker = Validator.parseLine(line);
                if (!usedIds.add(maker.getId())) {
                    throw new ValidationException("повторяющийся id " + maker.getId());
                }
                result.add(maker);
            } catch (ValidationException e) {
                logger.log(path.getFileName() + ", строка " + (i + 1) + ": " + e.getMessage(), line);
            }
        }
        return result;
    }
}
