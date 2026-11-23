package coffeefactory.io;

import java.nio.file.Path;
import java.util.List;

public abstract class AbstractReader<T> {
    public abstract List<T> read(Path path);
}
