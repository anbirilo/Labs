package coffeefactory.storage;

import coffeefactory.model.CoffeeMachine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class MapStorage extends AbstractStorage<CoffeeMachine> {
    private final SortedMap<Long, CoffeeMachine> items = new TreeMap<Long, CoffeeMachine>();

    @Override
    public boolean add(CoffeeMachine entity) {
        if (!valid(entity) || items.containsKey(entity.getId())) {
            return false;
        }
        items.put(entity.getId(), entity);
        return true;
    }

    @Override
    public boolean update(CoffeeMachine entity) {
        if (!valid(entity) || !items.containsKey(entity.getId())) {
            return false;
        }
        items.put(entity.getId(), entity);
        return true;
    }

    @Override
    public boolean remove(long id) {
        return items.remove(id) != null;
    }

    @Override
    public CoffeeMachine findById(long id) {
        return items.get(id);
    }

    @Override
    public List<CoffeeMachine> getAll() {
        return new ArrayList<CoffeeMachine>(items.values());
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public Iterator<CoffeeMachine> iterator() {
        return items.values().iterator();
    }

    public SortedMap<Long, CoffeeMachine> asSortedMap() {
        return new TreeMap<Long, CoffeeMachine>(items);
    }

    private boolean valid(CoffeeMachine entity) {
        return entity != null && entity.getId() > 0;
    }
}
