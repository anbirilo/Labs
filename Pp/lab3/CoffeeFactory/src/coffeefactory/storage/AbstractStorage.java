package coffeefactory.storage;

import coffeefactory.model.CoffeeMaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Абстрактный класс хранилища объектов (п. 2, 3).
 * Наследники: ListStorage (List) и MapStorage (Map).
 * Реализует Iterable, чтобы вывод можно было делать через итератор (п. 10).
 */
public abstract class AbstractStorage implements Iterable<CoffeeMaker> {

    /** Добавляет объект. Возвращает false, если объект с таким id уже есть. */
    public abstract boolean add(CoffeeMaker maker);

    /** Заменяет объект с тем же id. Возвращает false, если такого id нет. */
    public abstract boolean update(CoffeeMaker maker);

    /** Удаляет объект по id. Возвращает false, если такого id нет. */
    public abstract boolean remove(int id);

    /** Ищет объект по id, null — если не найден. */
    public abstract CoffeeMaker findById(int id);

    /** Все объекты (коллекция только для чтения). */
    public abstract Collection<CoffeeMaker> getAll();

    public abstract void clear();

    public int size() {
        return getAll().size();
    }

    public boolean isEmpty() {
        return getAll().isEmpty();
    }

    public boolean contains(int id) {
        return findById(id) != null;
    }

    /** Следующий свободный id (максимальный + 1). */
    public int nextId() {
        int max = 0;
        for (CoffeeMaker maker : getAll()) {
            max = Math.max(max, maker.getId());
        }
        return max + 1;
    }

    /**
     * Возвращает отсортированную копию данных (п. 11):
     * Collections.sort + переданный Comparator.
     */
    public List<CoffeeMaker> getSorted(Comparator<CoffeeMaker> comparator) {
        List<CoffeeMaker> copy = new ArrayList<>(getAll());
        Collections.sort(copy, comparator);
        return copy;
    }

    @Override
    public Iterator<CoffeeMaker> iterator() {
        return getAll().iterator();
    }
}
