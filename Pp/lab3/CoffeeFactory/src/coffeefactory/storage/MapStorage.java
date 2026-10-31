package coffeefactory.storage;

import coffeefactory.model.CoffeeMaker;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Хранилище на основе Map: ключ — id, значение — кофеварка (п. 3, 8).
 * TreeMap (SortedMap) хранит записи отсортированными по ключу (id).
 */
public class MapStorage extends AbstractStorage {
    private final SortedMap<Integer, CoffeeMaker> items = new TreeMap<>();

    @Override
    public boolean add(CoffeeMaker maker) {
        if (items.containsKey(maker.getId())) {
            return false;
        }
        items.put(maker.getId(), maker);
        return true;
    }

    @Override
    public boolean update(CoffeeMaker maker) {
        if (!items.containsKey(maker.getId())) {
            return false;
        }
        items.put(maker.getId(), maker);
        return true;
    }

    @Override
    public boolean remove(int id) {
        return items.remove(id) != null;
    }

    @Override
    public CoffeeMaker findById(int id) {
        return items.get(id);
    }

    @Override
    public Collection<CoffeeMaker> getAll() {
        return Collections.unmodifiableCollection(items.values());
    }

    /** Сама карта (только для чтения) — чтобы выводить пары «ключ -> значение». */
    public SortedMap<Integer, CoffeeMaker> getMap() {
        return Collections.unmodifiableSortedMap(items);
    }

    @Override
    public void clear() {
        items.clear();
    }
}
