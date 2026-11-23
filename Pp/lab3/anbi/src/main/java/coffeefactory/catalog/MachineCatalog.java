package coffeefactory.catalog;

import coffeefactory.io.TextFileHandler;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.sort.MachineComparators;
import coffeefactory.storage.ListStorage;
import coffeefactory.storage.MapStorage;
import coffeefactory.validation.ErrorLogger;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MachineCatalog {
    private final ListStorage listStorage;
    private final MapStorage mapStorage;
    private final TextFileHandler textFileHandler;
    private final ErrorLogger logger;
    private final Path textPath;

    public MachineCatalog(ListStorage listStorage, MapStorage mapStorage, TextFileHandler textFileHandler,
                          ErrorLogger logger, Path textPath) {
        this.listStorage = listStorage;
        this.mapStorage = mapStorage;
        this.textFileHandler = textFileHandler;
        this.logger = logger;
        this.textPath = textPath;
    }

    public ListStorage getListStorage() {
        return listStorage;
    }

    public MapStorage getMapStorage() {
        return mapStorage;
    }

    public int size() {
        return listStorage.size();
    }

    public CoffeeMachine find(long id) {
        return listStorage.findById(id);
    }

    public boolean add(CoffeeMachine machine) {
        if (machine == null || !listStorage.add(machine)) {
            return false;
        }
        if (!mapStorage.add(machine)) {
            listStorage.remove(machine.getId());
            return false;
        }
        saveText();
        return true;
    }

    public boolean update(CoffeeMachine machine) {
        if (machine == null) {
            return false;
        }
        boolean updatedList = listStorage.update(machine);
        boolean updatedMap = mapStorage.update(machine);
        if (!updatedList && !updatedMap) {
            return false;
        }
        saveText();
        return true;
    }

    public boolean remove(long id) {
        boolean removedList = listStorage.remove(id);
        boolean removedMap = mapStorage.remove(id);
        if (!removedList && !removedMap) {
            return false;
        }
        saveText();
        return true;
    }

    public void loadText() {
        replaceAll(textFileHandler.read(textPath), false);
    }

    public void saveText() {
        textFileHandler.write(textPath, listStorage.getAll());
    }

    public void replaceAll(List<CoffeeMachine> machines, boolean save) {
        listStorage.clear();
        mapStorage.clear();
        Set<Long> seen = new HashSet<Long>();
        if (machines != null) {
            for (CoffeeMachine machine : machines) {
                if (machine == null || !seen.add(machine.getId())) {
                    if (machine != null) {
                        logger.log("Дубликат id=" + machine.getId() + ", объект пропущен", machine.toFileLine());
                    }
                    continue;
                }
                listStorage.add(machine);
                mapStorage.add(machine);
            }
        }
        if (save) {
            saveText();
        }
    }

    public void sort(Comparator<CoffeeMachine> comparator) {
        List<CoffeeMachine> sorted = listStorage.getAll();
        Collections.sort(sorted, comparator == null ? MachineComparators.BY_ID : comparator);
        listStorage.clear();
        for (CoffeeMachine machine : sorted) {
            listStorage.add(machine);
        }
        saveText();
    }
}
