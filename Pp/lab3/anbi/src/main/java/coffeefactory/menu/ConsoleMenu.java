package coffeefactory.menu;

import coffeefactory.app.AppContext;
import coffeefactory.app.DataPaths;
import coffeefactory.archive.ArchiveService;
import coffeefactory.assembly.AssemblyFactory;
import coffeefactory.assembly.AssemblyRequest;
import coffeefactory.assembly.AssemblyResult;
import coffeefactory.catalog.MachineCatalog;
import coffeefactory.crypto.AesCipher;
import coffeefactory.crypto.Md5Hasher;
import coffeefactory.json.JsonHandler;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;
import coffeefactory.sort.MachineComparators;
import coffeefactory.validation.InputValidator;
import coffeefactory.xml.DtdValidator;
import coffeefactory.xml.XmlDomHandler;
import coffeefactory.xml.XmlSaxHandler;
import coffeefactory.xml.XmlStaxHandler;
import coffeefactory.xml.XsdValidator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Консольное меню. Создаётся отдельным методом, цикл выбора не находится в main.
 */
public class ConsoleMenu {
    private final MachineCatalog catalog;
    private final XmlDomHandler domHandler;
    private final XmlSaxHandler saxHandler;
    private final XmlStaxHandler staxHandler;
    private final JsonHandler jsonHandler;
    private final XsdValidator xsdValidator;
    private final DtdValidator dtdValidator;
    private final Md5Hasher hasher;
    private final AesCipher cipher;
    private final ArchiveService archiveService;
    private final AssemblyFactory assemblyFactory;
    private final InputValidator validator;
    private final DataPaths paths;
    private final Scanner scanner;

    public ConsoleMenu(AppContext context, Scanner scanner) {
        this.catalog = context.getCatalog();
        this.domHandler = context.getDomHandler();
        this.saxHandler = context.getSaxHandler();
        this.staxHandler = context.getStaxHandler();
        this.jsonHandler = context.getJsonHandler();
        this.xsdValidator = context.getXsdValidator();
        this.dtdValidator = context.getDtdValidator();
        this.hasher = context.getHasher();
        this.cipher = context.getCipher();
        this.archiveService = context.getArchiveService();
        this.assemblyFactory = context.getAssemblyFactory();
        this.validator = context.getValidator();
        this.paths = context.getPaths();
        this.scanner = scanner;
    }

    public String menuText() {
        return "\nМеню\n"
                + "1  — показать List (итератор)\n"
                + "2  — показать Map (итератор, порядок по id)\n"
                + "3  — собрать и добавить кофеварку\n"
                + "4  — редактировать\n"
                + "5  — удалить\n"
                + "6  — сортировка\n"
                + "7  — прочитать TXT\n"
                + "8  — записать TXT\n"
                + "9  — прочитать XML (DOM)\n"
                + "10 — записать XML (DOM)\n"
                + "11 — прочитать XML (SAX)\n"
                + "12 — прочитать XML (StAX)\n"
                + "13 — записать XML (StAX)\n"
                + "14 — проверить XML по XSD\n"
                + "15 — проверить XML по DTD\n"
                + "16 — прочитать JSON\n"
                + "17 — записать JSON\n"
                + "18 — пароль (MD5)\n"
                + "19 — зашифровать TXT (AES)\n"
                + "20 — расшифровать TXT (AES)\n"
                + "21 — архив ZIP\n"
                + "22 — архив JAR\n"
                + "0  — выход\n"
                + "Пункт: ";
    }

    public void run() {
        System.out.println("Фабрика по сборке кофеварок");
        System.out.println("В каталоге: " + catalog.size());
        while (true) {
            System.out.println(menuText());
            String choice = readLine().trim();
            if ("0".equals(choice)) {
                System.out.println("Работа завершена.");
                return;
            }
            dispatch(choice);
        }
    }

    private void dispatch(String choice) {
        switch (choice) {
            case "1":
                showList();
                break;
            case "2":
                showMap();
                break;
            case "3":
                runSafe("сборка", new Runnable() {
                    @Override
                    public void run() {
                        assemble();
                    }
                });
                break;
            case "4":
                runSafe("редактирование", new Runnable() {
                    @Override
                    public void run() {
                        edit();
                    }
                });
                break;
            case "5":
                runSafe("удаление", new Runnable() {
                    @Override
                    public void run() {
                        delete();
                    }
                });
                break;
            case "6":
                runSafe("сортировка", new Runnable() {
                    @Override
                    public void run() {
                        sort();
                    }
                });
                break;
            case "7":
                runSafe("чтение TXT", new Runnable() {
                    @Override
                    public void run() {
                        catalog.loadText();
                        System.out.println("TXT прочитан. В каталоге: " + catalog.size());
                    }
                });
                break;
            case "8":
                runSafe("запись TXT", new Runnable() {
                    @Override
                    public void run() {
                        catalog.saveText();
                        System.out.println("TXT записан: " + paths.text());
                    }
                });
                break;
            case "9":
                loadXml("DOM", new XmlLoad() {
                    @Override
                    public List<CoffeeMachine> load() {
                        return domHandler.read(paths.xml());
                    }
                });
                break;
            case "10":
                runSafe("запись XML DOM", new Runnable() {
                    @Override
                    public void run() {
                        domHandler.write(paths.xml(), catalog.getListStorage().getAll());
                        System.out.println("XML (DOM) записан: " + paths.xml());
                    }
                });
                break;
            case "11":
                loadXml("SAX", new XmlLoad() {
                    @Override
                    public List<CoffeeMachine> load() {
                        return saxHandler.read(paths.xml());
                    }
                });
                break;
            case "12":
                loadXml("StAX", new XmlLoad() {
                    @Override
                    public List<CoffeeMachine> load() {
                        return staxHandler.read(paths.xml());
                    }
                });
                break;
            case "13":
                runSafe("запись XML StAX", new Runnable() {
                    @Override
                    public void run() {
                        staxHandler.write(paths.xml(), catalog.getListStorage().getAll());
                        System.out.println("XML (StAX) записан: " + paths.xml());
                    }
                });
                break;
            case "14":
                runSafe("XSD", new Runnable() {
                    @Override
                    public void run() {
                        printErrors("XSD", xsdValidator.validate(paths.xml(), paths.xsd()));
                    }
                });
                break;
            case "15":
                runSafe("DTD", new Runnable() {
                    @Override
                    public void run() {
                        printErrors("DTD", dtdValidator.validate(paths.xml(), paths.dtd()));
                    }
                });
                break;
            case "16":
                runSafe("чтение JSON", new Runnable() {
                    @Override
                    public void run() {
                        catalog.replaceAll(jsonHandler.read(paths.json()), true);
                        System.out.println("JSON прочитан. В каталоге: " + catalog.size());
                    }
                });
                break;
            case "17":
                runSafe("запись JSON", new Runnable() {
                    @Override
                    public void run() {
                        jsonHandler.write(paths.json(), catalog.getListStorage().getAll());
                        System.out.println("JSON записан: " + paths.json());
                    }
                });
                break;
            case "18":
                runSafe("пароль", new Runnable() {
                    @Override
                    public void run() {
                        password();
                    }
                });
                break;
            case "19":
                runSafe("шифрование", new Runnable() {
                    @Override
                    public void run() {
                        encrypt();
                    }
                });
                break;
            case "20":
                runSafe("расшифровка", new Runnable() {
                    @Override
                    public void run() {
                        decrypt();
                    }
                });
                break;
            case "21":
                runSafe("ZIP", new Runnable() {
                    @Override
                    public void run() {
                        archive(false);
                    }
                });
                break;
            case "22":
                runSafe("JAR", new Runnable() {
                    @Override
                    public void run() {
                        archive(true);
                    }
                });
                break;
            default:
                System.out.println("Нет такого пункта.");
        }
    }

    private void showList() {
        System.out.println("List, вывод итератором:");
        catalog.getListStorage().printWithIterator(System.out);
    }

    private void showMap() {
        System.out.println("SortedMap по id, вывод итератором:");
        catalog.getMapStorage().printWithIterator(System.out);
    }

    private void assemble() {
        AssemblyRequest request = new AssemblyRequest();
        request.setId(askId(true));
        request.setType(askType(false, null));
        request.setModel(askModel(false, null));
        request.setPowerWatts(askInt("Мощность, Вт", false, 0, new IntParser() {
            @Override
            public Integer parse(String raw) {
                return validator.parsePower(raw);
            }
        }));
        request.setPressureBar(askDouble("Давление, бар", false, 0, new DoubleParser() {
            @Override
            public Double parse(String raw) {
                return validator.parsePressure(raw);
            }
        }));
        request.setWaterTankMl(askInt("Резервуар, мл", false, 0, new IntParser() {
            @Override
            public Integer parse(String raw) {
                return validator.parseWater(raw);
            }
        }));
        request.setCupsPerCycle(askInt("Чашек за цикл", false, 0, new IntParser() {
            @Override
            public Integer parse(String raw) {
                return validator.parseCups(raw);
            }
        }));
        request.setManufactureDate(askDate(false, null));
        request.setPrice(askDouble("Цена", false, 0, new DoubleParser() {
            @Override
            public Double parse(String raw) {
                return validator.parsePrice(raw);
            }
        }));
        request.setManufacturer(askLabel("Производитель", false, ""));
        request.setColor(askLabel("Цвет", false, ""));
        request.setWarrantyMonths(askInt("Гарантия, мес.", false, 0, new IntParser() {
            @Override
            public Integer parse(String raw) {
                return validator.parseWarranty(raw);
            }
        }));
        AssemblyResult result = assemblyFactory.assemble(request);
        for (String note : result.getNotes()) {
            System.out.println(note);
        }
        if (catalog.add(result.getMachine())) {
            System.out.println("Кофеварка собрана и добавлена. Всего: " + catalog.size());
        } else {
            System.out.println("Не удалось добавить кофеварку.");
        }
    }

    private void edit() {
        long id = askId(false);
        CoffeeMachine current = catalog.find(id);
        if (current == null) {
            System.out.println("Запись не найдена.");
            return;
        }
        CoffeeMachine edited = copy(current);
        edited.setType(askType(true, current.getType()));
        edited.setModel(askModel(true, current.getModel()));
        edited.setPowerWatts(askInt("Мощность, Вт", true, current.getPowerWatts(), new IntParser() {
            @Override
            public Integer parse(String raw) {
                return validator.parsePower(raw);
            }
        }));
        edited.setPressureBar(askDouble("Давление, бар", true, current.getPressureBar(), new DoubleParser() {
            @Override
            public Double parse(String raw) {
                return validator.parsePressure(raw);
            }
        }));
        edited.setWaterTankMl(askInt("Резервуар, мл", true, current.getWaterTankMl(), new IntParser() {
            @Override
            public Integer parse(String raw) {
                return validator.parseWater(raw);
            }
        }));
        edited.setCupsPerCycle(askInt("Чашек за цикл", true, current.getCupsPerCycle(), new IntParser() {
            @Override
            public Integer parse(String raw) {
                return validator.parseCups(raw);
            }
        }));
        edited.setManufactureDate(askDate(true, current.getManufactureDate()));
        edited.setPrice(askDouble("Цена", true, current.getPrice(), new DoubleParser() {
            @Override
            public Double parse(String raw) {
                return validator.parsePrice(raw);
            }
        }));
        edited.setManufacturer(askLabel("Производитель", true, current.getManufacturer()));
        edited.setColor(askLabel("Цвет", true, current.getColor()));
        edited.setWarrantyMonths(askInt("Гарантия, мес.", true, current.getWarrantyMonths(), new IntParser() {
            @Override
            public Integer parse(String raw) {
                return validator.parseWarranty(raw);
            }
        }));
        if (catalog.update(edited)) {
            System.out.println("Запись обновлена.");
        } else {
            System.out.println("Не удалось обновить запись.");
        }
    }

    private void delete() {
        long id = askId(false);
        if (catalog.find(id) == null) {
            System.out.println("Запись не найдена.");
            return;
        }
        String answer = ask("Удалить запись " + id + "? да/нет");
        if ("да".equalsIgnoreCase(answer) || "yes".equalsIgnoreCase(answer) || "y".equalsIgnoreCase(answer)) {
            catalog.remove(id);
            System.out.println("Удалено. Всего: " + catalog.size());
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    private void sort() {
        System.out.println("1 — цена по возрастанию");
        System.out.println("2 — мощность");
        System.out.println("3 — дата выпуска");
        System.out.println("4 — модель");
        System.out.println("5 — производитель");
        System.out.println("6 — цена по убыванию (лямбда)");
        System.out.println("7 — идентификатор (лямбда)");
        switch (ask("Поле")) {
            case "1":
                catalog.sort(MachineComparators.BY_PRICE);
                break;
            case "2":
                catalog.sort(MachineComparators.BY_POWER);
                break;
            case "3":
                catalog.sort(MachineComparators.BY_DATE);
                break;
            case "4":
                catalog.sort(MachineComparators.BY_MODEL);
                break;
            case "5":
                catalog.sort(MachineComparators.BY_MANUFACTURER);
                break;
            case "6":
                catalog.sort((left, right) -> Double.compare(right.getPrice(), left.getPrice()));
                break;
            case "7":
                catalog.sort((left, right) -> Long.compare(left.getId(), right.getId()));
                break;
            default:
                System.out.println("Нет такого поля.");
                return;
        }
        System.out.println("Список отсортирован.");
        showList();
    }

    private void password() {
        System.out.println("1 — сохранить MD5 пароля");
        System.out.println("2 — проверить пароль");
        switch (ask("Действие")) {
            case "1":
                String created = ask("Новый пароль");
                if (created.isEmpty()) {
                    System.out.println("Пустой пароль не сохраняется.");
                    return;
                }
                hasher.saveHash(paths.password(), created);
                System.out.println("MD5: " + hasher.hash(created));
                break;
            case "2":
                if (!Files.exists(paths.password())) {
                    System.out.println("Хеш ещё не сохранён.");
                    return;
                }
                String password = ask("Пароль");
                System.out.println(hasher.matchesFile(paths.password(), password)
                        ? "Пароль верный."
                        : "Пароль не совпадает.");
                break;
            default:
                System.out.println("Нет такого действия.");
        }
    }

    private void encrypt() {
        String key = ask("Ключ");
        if (key.isEmpty()) {
            System.out.println("Ключ не задан.");
            return;
        }
        cipher.encryptFile(paths.text(), paths.encrypted(), key);
        System.out.println("Зашифровано: " + paths.encrypted());
    }

    private void decrypt() {
        String key = ask("Ключ");
        if (key.isEmpty()) {
            System.out.println("Ключ не задан.");
            return;
        }
        cipher.decryptFile(paths.encrypted(), paths.restored(), key);
        System.out.println("Расшифровано: " + paths.restored());
    }

    private void archive(boolean jar) {
        List<Path> files = existingDataFiles();
        if (files.isEmpty()) {
            System.out.println("Нет файлов для архивации. Сначала сохраните TXT, XML или JSON.");
            return;
        }
        Path target = jar ? paths.jar() : paths.zip();
        if (jar) {
            archiveService.jar(target, files);
        } else {
            archiveService.zip(target, files);
        }
        System.out.println("Архив: " + target);
    }

    private List<Path> existingDataFiles() {
        List<Path> files = new ArrayList<Path>();
        addIfExists(files, paths.text());
        addIfExists(files, paths.xml());
        addIfExists(files, paths.json());
        return files;
    }

    private static void addIfExists(List<Path> files, Path path) {
        if (Files.exists(path)) {
            files.add(path);
        }
    }

    private void loadXml(final String parserName, final XmlLoad loader) {
        runSafe("чтение XML " + parserName, new Runnable() {
            @Override
            public void run() {
                catalog.replaceAll(loader.load(), true);
                System.out.println("XML (" + parserName + ") прочитан. В каталоге: " + catalog.size());
            }
        });
    }

    private void printErrors(String kind, List<String> errors) {
        if (errors.isEmpty()) {
            System.out.println("Документ соответствует " + kind + ".");
            return;
        }
        System.out.println("Ошибки " + kind + ":");
        for (String error : errors) {
            System.out.println(error);
        }
    }

    private long askId(boolean mustBeFree) {
        while (true) {
            Long id = validator.parseId(ask("Идентификатор"));
            if (id == null) {
                System.out.println("Ожидается положительное целое.");
                continue;
            }
            if (mustBeFree && catalog.find(id) != null) {
                System.out.println("Такой идентификатор уже есть.");
                continue;
            }
            return id;
        }
    }

    private MachineType askType(boolean keep, MachineType current) {
        while (true) {
            System.out.println("Типы:");
            for (MachineType type : MachineType.values()) {
                System.out.println("  " + type.name() + " — " + type.getTitle());
            }
            String currentName = current == null ? "" : current.name();
            String raw = ask(keep ? "Тип [" + currentName + "]" : "Тип");
            if (keep && raw.isEmpty()) {
                return current;
            }
            MachineType type = validator.parseType(raw);
            if (type != null) {
                return type;
            }
            System.out.println("Неизвестный тип.");
        }
    }

    private String askModel(boolean keep, String current) {
        while (true) {
            String raw = ask(keep ? "Модель [" + current + "]" : "Модель");
            if (keep && raw.isEmpty()) {
                return current;
            }
            String model = validator.parseModel(raw);
            if (model != null) {
                return model;
            }
            System.out.println("Некорректная модель.");
        }
    }

    private String askLabel(String prompt, boolean keep, String current) {
        String shown = current == null ? "" : current;
        while (true) {
            String raw = ask(keep ? prompt + " [" + shown + "]" : prompt);
            if (keep && raw.isEmpty()) {
                return shown;
            }
            String value = validator.parseLabel(raw);
            if (value != null) {
                return value;
            }
            System.out.println("Допустимы буквы, цифры, пробел, точка и дефис.");
        }
    }

    private int askInt(String prompt, boolean keep, int current, IntParser parser) {
        while (true) {
            String raw = ask(keep ? prompt + " [" + current + "]" : prompt);
            if (keep && raw.isEmpty()) {
                return current;
            }
            Integer value = parser.parse(raw);
            if (value != null) {
                return value;
            }
            System.out.println("Некорректное число, повторите ввод.");
        }
    }

    private double askDouble(String prompt, boolean keep, double current, DoubleParser parser) {
        while (true) {
            String shown = CoffeeMachine.formatDecimal(current);
            String raw = ask(keep ? prompt + " [" + shown + "]" : prompt);
            if (keep && raw.isEmpty()) {
                return current;
            }
            Double value = parser.parse(raw);
            if (value != null) {
                return value;
            }
            System.out.println("Некорректное число, повторите ввод.");
        }
    }

    private Date askDate(boolean keep, Date current) {
        while (true) {
            String shown = CoffeeMachine.formatDate(current);
            String raw = ask(keep ? "Дата выпуска дд.мм.гггг [" + shown + "]" : "Дата выпуска дд.мм.гггг");
            if (raw.isEmpty()) {
                return keep ? current : null;
            }
            Date date = validator.parseDate(raw);
            if (date != null) {
                return date;
            }
            System.out.println("Ожидается дата в формате дд.мм.гггг.");
        }
    }

    private String ask(String prompt) {
        System.out.println(prompt + ":");
        return readLine().trim();
    }

    private String readLine() {
        if (!scanner.hasNextLine()) {
            return "0";
        }
        return scanner.nextLine();
    }

    private static CoffeeMachine copy(CoffeeMachine source) {
        CoffeeMachine copy = new CoffeeMachine();
        copy.setId(source.getId());
        copy.setType(source.getType());
        copy.setModel(source.getModel());
        copy.setPowerWatts(source.getPowerWatts());
        copy.setPressureBar(source.getPressureBar());
        copy.setWaterTankMl(source.getWaterTankMl());
        copy.setCupsPerCycle(source.getCupsPerCycle());
        copy.setManufactureDate(source.getManufactureDate());
        copy.setPrice(source.getPrice());
        copy.setManufacturer(source.getManufacturer());
        copy.setColor(source.getColor());
        copy.setWarrantyMonths(source.getWarrantyMonths());
        return copy;
    }

    private static void runSafe(String action, Runnable task) {
        try {
            task.run();
        } catch (RuntimeException exception) {
            System.out.println("Ошибка (" + action + "): " + exception.getMessage());
        }
    }

    private interface IntParser {
        Integer parse(String raw);
    }

    private interface DoubleParser {
        Double parse(String raw);
    }

    private interface XmlLoad {
        List<CoffeeMachine> load();
    }
}
