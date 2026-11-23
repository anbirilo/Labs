package coffeefactory.io;

import java.nio.file.Path;
import java.util.List;

public abstract class AbstractWriter<T> extends AbstractReader<T> {
    public abstract void write(Path path, List<T> items);
}
