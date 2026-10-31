package coffeefactory.io;

import coffeefactory.model.CoffeeMaker;
import coffeefactory.util.DateUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Запись кофеварок в текстовый файл (п. 4).
 * Формат строки совпадает с TxtFileReader.
 */
public class TxtFileWriter extends AbstractFileHandler {
    private static final String HEADER = "# id;тип;модель;мощность(Вт);бак(мл);дата выпуска;цена";

    public TxtFileWriter(Path path) {
        super(path);
    }

    @Override
    public String getFormat() {
        return "TXT";
    }

    public void write(Collection<CoffeeMaker> makers) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (CoffeeMaker m : makers) {
            lines.add(toLine(m));
        }
        Files.write(path, lines, CHARSET);
    }

    static String toLine(CoffeeMaker m) {
        return m.getId() + ";"
                + m.getType().name() + ";"
                + m.getModel() + ";"
                + m.getPowerW() + ";"
                + m.getTankVolumeMl() + ";"
                + DateUtil.format(m.getReleaseDate()) + ";"
                + String.format(Locale.ROOT, "%.2f", m.getPrice());
    }
}
