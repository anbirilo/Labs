package coffeefactory.service;

import coffeefactory.io.TxtFileReader;
import coffeefactory.io.TxtFileWriter;
import coffeefactory.model.CoffeeMaker;
import coffeefactory.storage.ListStorage;
import coffeefactory.storage.MapStorage;
import coffeefactory.util.ErrorLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Логика фабрики: хранит данные одновременно в List и Map (п. 8),
 * выполняет добавление / обновление / удаление и после каждого изменения
 * обновляет и List, и Map, и txt-файл (п. 9).
 */
public class FactoryService {
    private final ListStorage listStorage = new ListStorage();
    private final MapStorage mapStorage = new MapStorage();
    private final ErrorLogger logger;
    private Path currentFile;

    public FactoryService(Path dataFile, ErrorLogger logger) {
        this.currentFile = dataFile;
        this.logger = logger;
    }

    public ListStorage getListStorage() {
        return listStorage;
    }

    public MapStorage getMapStorage() {
        return mapStorage;
    }

    public Path getCurrentFile() {
        return currentFile;
    }

    public int nextId() {
        return listStorage.nextId();
    }

    // ---------- чтение / запись файла ----------

    /** Загружает данные из txt-файла вместо текущих. Возвращает число прочитанных записей. */
    public int loadFromFile(Path path) throws IOException {
        List<CoffeeMaker> loaded = new TxtFileReader(path, logger).read();
        listStorage.clear();
        mapStorage.clear();
        for (CoffeeMaker maker : loaded) {
            listStorage.add(maker);
            mapStorage.add(maker);
        }
        currentFile = path;
        return loaded.size();
    }

    /** При старте: если рабочий файл существует — загрузить его. */
    public void loadIfExists() {
        if (!Files.exists(currentFile)) {
            System.out.println("Файл " + currentFile + " не найден — начинаем с пустого списка.");
            return;
        }
        try {
            int count = loadFromFile(currentFile);
            System.out.println("Загружено записей: " + count + " (файл " + currentFile + ")");
        } catch (IOException e) {
            System.out.println("Не удалось прочитать файл " + currentFile + ": " + e.getMessage());
        }
    }

    /** Сохраняет текущие данные в указанный файл, он становится рабочим. */
    public void saveToFile(Path path) throws IOException {
        new TxtFileWriter(path).write(listStorage.getAll());
        currentFile = path;
    }

    // ---------- добавление / обновление / удаление (CUD) ----------

    public boolean add(CoffeeMaker maker) throws IOException {
        if (!listStorage.add(maker)) {
            return false;
        }
        mapStorage.add(maker);
        saveToFile(currentFile);
        return true;
    }

    public boolean update(CoffeeMaker maker) throws IOException {
        if (!listStorage.update(maker)) {
            return false;
        }
        mapStorage.update(maker);
        saveToFile(currentFile);
        return true;
    }

    public boolean remove(int id) throws IOException {
        if (!listStorage.remove(id)) {
            return false;
        }
        mapStorage.remove(id);
        saveToFile(currentFile);
        return true;
    }
}
