package coffeefactory;

import coffeefactory.catalog.MachineCatalog;
import coffeefactory.io.TextFileHandler;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;
import coffeefactory.sort.MachineComparators;
import coffeefactory.storage.ListStorage;
import coffeefactory.storage.MapStorage;
import coffeefactory.validation.ErrorLogger;
import coffeefactory.validation.InputValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageCatalogSortTest {
    @TempDir
    Path temp;

    @Test
    void listKeepsInsertionOrderAndRejectsDuplicate() {
        ListStorage storage = new ListStorage();
        assertTrue(storage.add(Fixtures.machine(5, MachineType.DRIP, "Five", 100)));
        assertTrue(storage.add(Fixtures.machine(1, MachineType.DRIP, "One", 50)));
        assertFalse(storage.add(Fixtures.machine(5, MachineType.DRIP, "Again", 1)));
        assertFalse(storage.add(null));
        assertEquals(2, storage.size());
        Iterator<CoffeeMachine> iterator = storage.iterator();
        assertEquals(5, iterator.next().getId());
        assertEquals(1, iterator.next().getId());
        assertTrue(storage.formatWithIterator().contains("Five"));
        assertTrue(storage.remove(5));
        assertFalse(storage.remove(5));
        assertNull(storage.findById(5));
    }

    @Test
    void mapIsSortedByIdAndUpdatesByKey() {
        MapStorage storage = new MapStorage();
        storage.add(Fixtures.machine(5, MachineType.POD, "Five", 100));
        storage.add(Fixtures.machine(1, MachineType.POD, "One", 50));
        storage.add(Fixtures.machine(3, MachineType.POD, "Three", 70));
        Iterator<CoffeeMachine> iterator = storage.iterator();
        assertEquals(1, iterator.next().getId());
        assertEquals(3, iterator.next().getId());
        assertEquals(5, iterator.next().getId());
        assertEquals(Long.valueOf(1), storage.asSortedMap().firstKey());
        CoffeeMachine updated = Fixtures.machine(3, MachineType.POD, "Three+", 80);
        assertTrue(storage.update(updated));
        assertEquals("Three+", storage.findById(3).getModel());
        assertFalse(storage.update(Fixtures.machine(9, MachineType.POD, "Missing", 1)));
    }

    @Test
    void iteratorsPrintBothStorages() {
        ListStorage list = new ListStorage();
        MapStorage map = new MapStorage();
        for (CoffeeMachine machine : Fixtures.pair()) {
            list.add(machine);
            map.add(machine);
        }
        assertTrue(list.formatWithIterator().contains("Kitfort"));
        assertTrue(map.formatWithIterator().indexOf("Nespresso") < map.formatWithIterator().indexOf("Kitfort"));
    }

    @Test
    void catalogCudUpdatesListMapAndTextFile() {
        MachineCatalog catalog = catalog();
        CoffeeMachine machine = Fixtures.machine(8, MachineType.ESPRESSO, "Dedica", 24990);
        assertTrue(catalog.add(machine));
        assertFalse(catalog.add(Fixtures.machine(8, MachineType.ESPRESSO, "Dedica", 1)));
        assertEquals(1, catalog.getListStorage().size());
        assertEquals(1, catalog.getMapStorage().size());
        assertTrue(text().contains("Dedica"));

        machine.setPrice(19990);
        assertTrue(catalog.update(machine));
        assertEquals(19990, catalog.find(8).getPrice(), 0.001);
        assertTrue(text().contains("19990"));
        assertFalse(text().contains("24990"));

        assertTrue(catalog.remove(8));
        assertEquals(0, catalog.size());
        assertFalse(text().contains("Dedica"));
        assertFalse(catalog.remove(8));
        assertFalse(catalog.update(Fixtures.machine(8, MachineType.ESPRESSO, "Dedica", 1)));
    }

    @Test
    void duplicateIdFromImportIsSkipped() {
        MachineCatalog catalog = catalog();
        List<CoffeeMachine> machines = new ArrayList<CoffeeMachine>();
        machines.add(Fixtures.machine(4, MachineType.DRIP, "First", 100));
        machines.add(Fixtures.machine(4, MachineType.DRIP, "Second", 200));
        machines.add(Fixtures.machine(5, MachineType.DRIP, "Third", 300));
        catalog.replaceAll(machines, true);
        assertEquals(2, catalog.size());
        assertEquals("First", catalog.find(4).getModel());
        assertTrue(log().contains("Дубликат id=4"));
    }

    @Test
    void collectionsComparatorAndLambdaSortListWhileMapStaysById() {
        MachineCatalog catalog = catalog();
        catalog.add(Fixtures.machine(2, MachineType.ESPRESSO, "Bravo", 500));
        CoffeeMachine early = Fixtures.machine(1, MachineType.ESPRESSO, "Alpha", 900);
        early.setManufactureDate(null);
        early.setPowerWatts(1500);
        catalog.add(early);

        catalog.sort(MachineComparators.BY_PRICE);
        assertEquals("Bravo", catalog.getListStorage().getAll().get(0).getModel());
        catalog.sort(MachineComparators.BY_PRICE_DESC);
        assertEquals("Alpha", catalog.getListStorage().getAll().get(0).getModel());
        catalog.sort(MachineComparators.BY_POWER);
        assertEquals("Bravo", catalog.getListStorage().getAll().get(0).getModel());
        catalog.sort(MachineComparators.BY_MODEL);
        assertEquals("Alpha", catalog.getListStorage().getAll().get(0).getModel());
        catalog.sort(MachineComparators.BY_MANUFACTURER);
        assertEquals(2, catalog.getListStorage().size());
        catalog.sort(MachineComparators.BY_DATE);
        assertEquals("Bravo", catalog.getListStorage().getAll().get(0).getModel());
        assertNull(catalog.getListStorage().getAll().get(1).getManufactureDate());

        List<CoffeeMachine> lambdaSorted = catalog.getListStorage().getAll();
        Collections.sort(lambdaSorted, (left, right) -> Long.compare(right.getId(), left.getId()));
        assertEquals(2, lambdaSorted.get(0).getId());
        assertEquals(1, catalog.getMapStorage().iterator().next().getId());
    }

    private MachineCatalog catalog() {
        ErrorLogger logger = new ErrorLogger(temp.resolve("errors.log"), false);
        TextFileHandler handler = new TextFileHandler(logger);
        return new MachineCatalog(new ListStorage(), new MapStorage(), handler, logger, temp.resolve("coffeemachines.txt"));
    }

    private String text() {
        return coffeefactory.common.Io.readString(temp.resolve("coffeemachines.txt"));
    }

    private String log() {
        return coffeefactory.common.Io.readString(temp.resolve("errors.log"));
    }

    @Test
    void dateParserUsedBySortFixtureIsStrict() {
        assertNull(new InputValidator().parseDate("31.02.2020"));
    }
}
