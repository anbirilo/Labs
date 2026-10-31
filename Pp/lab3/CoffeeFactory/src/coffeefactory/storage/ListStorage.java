package coffeefactory.storage;

import coffeefactory.model.CoffeeMaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Хранилище на основе List (п. 3, 8). */
public class ListStorage extends AbstractStorage {
    private final List<CoffeeMaker> items = new ArrayList<>();

    @Override
    public boolean add(CoffeeMaker maker) {
        if (contains(maker.getId())) {
            return false;
        }
        items.add(maker);
        return true;
    }

    @Override
    public boolean update(CoffeeMaker maker) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == maker.getId()) {
                items.set(i, maker);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean remove(int id) {
        return items.removeIf(m -> m.getId() == id);
    }

    @Override
    public CoffeeMaker findById(int id) {
        for (CoffeeMaker maker : items) {
            if (maker.getId() == id) {
                return maker;
            }
        }
        return null;
    }

    @Override
    public Collection<CoffeeMaker> getAll() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void clear() {
        items.clear();
    }
}
