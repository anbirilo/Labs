package coffeefactory.ui;

import coffeefactory.model.CoffeeMaker;
import coffeefactory.model.CoffeeMakerType;
import coffeefactory.service.FactoryService;
import coffeefactory.util.CoffeeMakerComparators;
import coffeefactory.util.DateUtil;
import coffeefactory.util.ValidationException;
import coffeefactory.util.Validator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Консольное меню (п. 6): добавление, редактирование, удаление,
 * вывод через итераторы (п. 10), сортировка (п. 11), чтение/запись файла.
 */
public class ConsoleMenu {

    /** Разбор введённой строки с проверкой; бросает ValidationException при ошибке. */
    private interface Parser<T> {
        T parse(String text) throws ValidationException;
    }

    private static final String[] SORT_NAMES = {
            "id", "тип", "модель", "мощность", "объём бака", "дата выпуска", "цена"
    };

    @SuppressWarnings("unchecked")
    private static final Comparator<CoffeeMaker>[] SORT_COMPARATORS = new Comparator[]{
            CoffeeMakerComparators.BY_ID,
            CoffeeMakerComparators.BY_TYPE,
            CoffeeMakerComparators.BY_MODEL,
            CoffeeMakerComparators.BY_POWER,
            CoffeeMakerComparators.BY_TANK,
            CoffeeMakerComparators.BY_DATE,
            CoffeeMakerComparators.BY_PRICE
    };

    private final FactoryService service;
    private final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

    public ConsoleMenu(FactoryService service) {
        this.service = service;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            try {
                String choice = readLine("Ваш выбор: ").trim();
                switch (choice) {
                    case "1" -> printList();
                    case "2" -> printMap();
                    case "3" -> addMaker();
                    case "4" -> editMaker();
                    case "5" -> deleteMaker();
                    case "6" -> sortAndPrint();
                    case "7" -> loadFromFile();
                    case "8" -> saveToFile();
                    case "0" -> running = false;
                    default -> System.out.println("Нет такого пункта меню.");
                }
            } catch (IOException e) {
                System.out.println("Ошибка работы с файлом: " + e.getMessage());
            } catch (NoSuchElementException e) {
                running = false; // ввод закончился (Ctrl+D / Ctrl+Z)
            }
        }
        System.out.println("Работа завершена.");
    }

    // ---------- меню ----------

    private void printMenu() {
        System.out.println();
        System.out.println("===== Фабрика кофеварок =====");
        System.out.println("Записей: " + service.getListStorage().size()
                + " | рабочий файл: " + service.getCurrentFile());
        System.out.println("1 — Показать все (List)");
        System.out.println("2 — Показать все (Map)");
        System.out.println("3 — Добавить кофеварку");
        System.out.println("4 — Редактировать кофеварку");
        System.out.println("5 — Удалить кофеварку");
        System.out.println("6 — Сортировка и вывод");
        System.out.println("7 — Загрузить из файла");
        System.out.println("8 — Сохранить в файл");
        System.out.println("0 — Выход");
    }

    // ---------- вывод через итераторы (п. 10) ----------

    private void printList() {
        System.out.println("--- Данные из List ---");
        printWithIterator(service.getListStorage().iterator());
    }

    private void printMap() {
        System.out.println("--- Данные из Map (ключ -> значение) ---");
        Iterator<Map.Entry<Integer, CoffeeMaker>> it =
                service.getMapStorage().getMap().entrySet().iterator();
        if (!it.hasNext()) {
            System.out.println("(пусто)");
            return;
        }
        while (it.hasNext()) {
            Map.Entry<Integer, CoffeeMaker> entry = it.next();
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    private void printWithIterator(Iterator<CoffeeMaker> it) {
        if (!it.hasNext()) {
            System.out.println("(пусто)");
            return;
        }
        int number = 1;
        while (it.hasNext()) {
            System.out.println(number++ + ". " + it.next());
        }
    }

    // ---------- добавление / редактирование / удаление (п. 9) ----------

    private void addMaker() throws IOException {
        System.out.println("--- Добавление кофеварки ---");
        int id = readNewId();
        CoffeeMakerType type = readValid("Тип (" + CoffeeMakerType.hint() + "): ", Validator::parseType);
        String model = readValid("Модель: ", Validator::parseModel);
        int power = readValid("Мощность, Вт (" + Validator.MIN_POWER + ".." + Validator.MAX_POWER + "): ",
                Validator::parsePower);
        int tank = readValid("Объём бака, мл (" + Validator.MIN_TANK + ".." + Validator.MAX_TANK + "): ",
                Validator::parseTankVolume);
        Date date = readValid("Дата выпуска (дд.мм.гггг): ", Validator::parseDate);
        double price = readValid("Цена: ", Validator::parsePrice);

        CoffeeMaker maker = new CoffeeMaker(id, type, model, power, tank, date, price);
        if (service.add(maker)) {
            System.out.println("Добавлено: " + maker);
        } else {
            System.out.println("Кофеварка с id " + id + " уже существует.");
        }
    }

    private void editMaker() throws IOException {
        System.out.println("--- Редактирование кофеварки ---");
        int id = readValid("id редактируемой кофеварки: ", Validator::parseId);
        CoffeeMaker old = service.getListStorage().findById(id);
        if (old == null) {
            System.out.println("Кофеварка с id " + id + " не найдена.");
            return;
        }
        System.out.println("Текущие данные: " + old);
        System.out.println("(Enter — оставить прежнее значение)");

        CoffeeMaker edited = new CoffeeMaker(old); // правим копию
        edited.setType(readValidOrKeep("Тип (" + CoffeeMakerType.hint() + ") [" + old.getType() + "]: ",
                Validator::parseType, old.getType()));
        edited.setModel(readValidOrKeep("Модель [" + old.getModel() + "]: ",
                Validator::parseModel, old.getModel()));
        edited.setPowerW(readValidOrKeep("Мощность, Вт [" + old.getPowerW() + "]: ",
                Validator::parsePower, old.getPowerW()));
        edited.setTankVolumeMl(readValidOrKeep("Объём бака, мл [" + old.getTankVolumeMl() + "]: ",
                Validator::parseTankVolume, old.getTankVolumeMl()));
        edited.setReleaseDate(readValidOrKeep("Дата выпуска (дд.мм.гггг) ["
                        + DateUtil.format(old.getReleaseDate()) + "]: ",
                Validator::parseDate, old.getReleaseDate()));
        edited.setPrice(readValidOrKeep("Цена [" + old.getPrice() + "]: ",
                Validator::parsePrice, old.getPrice()));

        if (service.update(edited)) {
            System.out.println("Обновлено: " + edited);
        } else {
            System.out.println("Не удалось обновить запись.");
        }
    }

    private void deleteMaker() throws IOException {
        System.out.println("--- Удаление кофеварки ---");
        int id = readValid("id удаляемой кофеварки: ", Validator::parseId);
        if (service.remove(id)) {
            System.out.println("Кофеварка с id " + id + " удалена.");
        } else {
            System.out.println("Кофеварка с id " + id + " не найдена.");
        }
    }

    // ---------- сортировка (п. 11) ----------

    private void sortAndPrint() {
        System.out.println("--- Сортировка ---");
        for (int i = 0; i < SORT_NAMES.length; i++) {
            System.out.println((i + 1) + " — по полю: " + SORT_NAMES[i]);
        }
        int field = readValid("Номер поля: ",
                text -> parseChoice(text, SORT_NAMES.length));
        int order = readValid("Порядок (1 — по возрастанию, 2 — по убыванию): ",
                text -> parseChoice(text, 2));

        Comparator<CoffeeMaker> comparator = SORT_COMPARATORS[field - 1];
        if (order == 2) {
            comparator = comparator.reversed();
        }
        System.out.println("Сортировка по полю «" + SORT_NAMES[field - 1] + "»:");
        printWithIterator(service.getListStorage().getSorted(comparator).iterator());
    }

    private int parseChoice(String text, int max) throws ValidationException {
        try {
            int n = Integer.parseInt(text.trim());
            if (n >= 1 && n <= max) {
                return n;
            }
        } catch (NumberFormatException ignored) {
            // обрабатывается ниже
        }
        throw new ValidationException("введите число от 1 до " + max);
    }

    // ---------- файл ----------

    private void loadFromFile() throws IOException {
        Path path = readPath("Путь к файлу для загрузки");
        int count = service.loadFromFile(path);
        System.out.println("Загружено записей: " + count + ". Некорректные строки пропущены "
                + "(см. сообщения выше и logs/errors.log).");
    }

    private void saveToFile() throws IOException {
        Path path = readPath("Путь к файлу для сохранения");
        service.saveToFile(path);
        System.out.println("Сохранено записей: " + service.getListStorage().size() + " -> " + path);
    }

    private Path readPath(String prompt) {
        String text = readLine(prompt + " [" + service.getCurrentFile() + "]: ").trim();
        return text.isEmpty() ? service.getCurrentFile() : Paths.get(text);
    }

    // ---------- вспомогательные методы ввода ----------

    private int readNewId() {
        while (true) {
            String text = readLine("id (Enter — автоматически, сейчас " + service.nextId() + "): ");
            if (text.isBlank()) {
                return service.nextId();
            }
            try {
                int id = Validator.parseId(text);
                if (service.getListStorage().contains(id)) {
                    System.out.println("  Ошибка: id " + id + " уже занят. Повторите ввод.");
                } else {
                    return id;
                }
            } catch (ValidationException e) {
                System.out.println("  Ошибка: " + e.getMessage() + ". Повторите ввод.");
            }
        }
    }

    private <T> T readValid(String prompt, Parser<T> parser) {
        while (true) {
            String text = readLine(prompt);
            try {
                return parser.parse(text);
            } catch (ValidationException e) {
                System.out.println("  Ошибка: " + e.getMessage() + ". Повторите ввод.");
            }
        }
    }

    private <T> T readValidOrKeep(String prompt, Parser<T> parser, T current) {
        while (true) {
            String text = readLine(prompt);
            if (text.isBlank()) {
                return current;
            }
            try {
                return parser.parse(text);
            } catch (ValidationException e) {
                System.out.println("  Ошибка: " + e.getMessage() + ". Повторите ввод.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return in.nextLine();
    }
}
