package coffeefactory.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import coffeefactory.model.CoffeeMaker;

public abstract class AbstractStorage implements Iterable<CoffeeMaker> {

    public abstract boolean add(CoffeeMaker maker);

    public abstract boolean update(CoffeeMaker maker);

    public abstract boolean remove(int id);

    public abstract CoffeeMaker findById(int id);

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

    public int nextId() {
        int max = 0;
        for (CoffeeMaker maker : getAll()) {
            max = Math.max(max, maker.getId());
        }
        return max + 1;
    }

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
