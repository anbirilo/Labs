package coffeefactory.storage;

import coffeefactory.model.CoffeeMachine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Хранилище кофеварок в List. Обход и удаление выполняются итератором.
 */
public class ListStorage extends AbstractStorage<CoffeeMachine> {
    private final List<CoffeeMachine> items = new ArrayList<CoffeeMachine>();

    @Override
    public boolean add(CoffeeMachine entity) {
        if (!valid(entity) || findById(entity.getId()) != null) {
            return false;
        }
        items.add(entity);
        return true;
    }

    @Override
    public boolean update(CoffeeMachine entity) {
        if (!valid(entity)) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == entity.getId()) {
                items.set(i, entity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean remove(long id) {
        Iterator<CoffeeMachine> iterator = items.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId() == id) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public CoffeeMachine findById(long id) {
        Iterator<CoffeeMachine> iterator = iterator();
        while (iterator.hasNext()) {
            CoffeeMachine machine = iterator.next();
            if (machine.getId() == id) {
                return machine;
            }
        }
        return null;
    }

    @Override
    public List<CoffeeMachine> getAll() {
        return new ArrayList<CoffeeMachine>(items);
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
        return items.iterator();
    }

    private boolean valid(CoffeeMachine entity) {
        return entity != null && entity.getId() > 0;
    }
}
