package coffeefactory.storage;

import coffeefactory.model.AbstractEntity;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

/**
 * Абстрактное хранилище коллекции сущностей.
 */
public abstract class AbstractStorage<T extends AbstractEntity> implements Iterable<T> {
    public abstract boolean add(T entity);

    public abstract boolean update(T entity);

    public abstract boolean remove(long id);

    public abstract T findById(long id);

    public abstract List<T> getAll();

    public abstract int size();

    public abstract void clear();

    public String formatWithIterator() {
        StringBuilder builder = new StringBuilder();
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()) {
            if (builder.length() > 0) {
                builder.append(System.lineSeparator());
            }
            builder.append(iterator.next());
        }
        return builder.toString();
    }

    public void printWithIterator(PrintStream out) {
        Iterator<T> iterator = iterator();
        if (!iterator.hasNext()) {
            out.println("Записей нет.");
            return;
        }
        while (iterator.hasNext()) {
            out.println(iterator.next());
        }
    }
}
