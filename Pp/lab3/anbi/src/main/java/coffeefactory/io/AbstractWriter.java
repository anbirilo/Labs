package coffeefactory.io;

import java.nio.file.Path;
import java.util.List;

/**
 * Абстрактные чтение и запись коллекции.
 */
public abstract class AbstractWriter<T> extends AbstractReader<T> {
    public abstract void write(Path path, List<T> items);
}
