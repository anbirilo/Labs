package coffeefactory.util;

import coffeefactory.model.CoffeeMaker;
import coffeefactory.model.CoffeeMakerType;

import java.util.Date;

/**
 * Проверка входных данных на корректность (п. 7).
 * Используется и при чтении из txt-файла, и при вводе с консоли.
 * Некорректные данные -> ValidationException с понятным описанием.
 */
public final class Validator {
    public static final int MIN_POWER = 50;
    public static final int MAX_POWER = 5000;
    public static final int MIN_TANK = 50;
    public static final int MAX_TANK = 10000;
    public static final int MAX_MODEL_LENGTH = 50;
    public static final double MAX_PRICE = 10_000_000;
    public static final String FIELD_SEPARATOR = ";";
    public static final int FIELD_COUNT = 7;

    private static final String MIN_DATE = "01.01.1950";

    private Validator() {
    }

    public static int parseId(String text) throws ValidationException {
        return parseInt("id", text, 1, Integer.MAX_VALUE);
    }

    public static CoffeeMakerType parseType(String text) throws ValidationException {
        String value = clean(text);
        try {
            return CoffeeMakerType.parse(value);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("тип: неизвестное значение '" + value
                    + "' (допустимо: " + CoffeeMakerType.names() + ")");
        }
    }

    public static String parseModel(String text) throws ValidationException {
        String value = clean(text);
        if (value.isEmpty()) {
            throw new ValidationException("модель: значение не указано");
        }
        if (value.length() > MAX_MODEL_LENGTH) {
            throw new ValidationException("модель: длина больше " + MAX_MODEL_LENGTH + " символов");
        }
        if (value.contains(FIELD_SEPARATOR)) {
            throw new ValidationException("модель: нельзя использовать символ '" + FIELD_SEPARATOR + "'");
        }
        return value;
    }

    public static int parsePower(String text) throws ValidationException {
        return parseInt("мощность (Вт)", text, MIN_POWER, MAX_POWER);
    }

    public static int parseTankVolume(String text) throws ValidationException {
        return parseInt("объём бака (мл)", text, MIN_TANK, MAX_TANK);
    }

    public static Date parseDate(String text) throws ValidationException {
        Date date = DateUtil.parse(text);
        if (date.before(DateUtil.parse(MIN_DATE))) {
            throw new ValidationException("дата выпуска не может быть раньше " + MIN_DATE);
        }
        if (date.after(new Date())) {
            throw new ValidationException("дата выпуска не может быть в будущем");
        }
        return date;
    }

    public static double parsePrice(String text) throws ValidationException {
        String value = clean(text);
        if (!value.matches("\\d+([.,]\\d{1,2})?")) {
            throw new ValidationException("цена: '" + value
                    + "' должна быть положительным числом (до 2 знаков после запятой)");
        }
        double price = Double.parseDouble(value.replace(',', '.'));
        if (price <= 0 || price > MAX_PRICE) {
            throw new ValidationException("цена: значение должно быть в диапазоне (0; " + (long) MAX_PRICE + "]");
        }
        return price;
    }

    /**
     * Разбирает строку файла вида:
     * id;тип;модель;мощность;объём бака;дата выпуска;цена
     * Если хотя бы одно поле некорректно — вся строка (объект) не читается.
     */
    public static CoffeeMaker parseLine(String line) throws ValidationException {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        if (parts.length != FIELD_COUNT) {
            throw new ValidationException("ожидается " + FIELD_COUNT + " полей через '"
                    + FIELD_SEPARATOR + "', найдено " + parts.length);
        }
        int id = parseId(parts[0]);
        CoffeeMakerType type = parseType(parts[1]);
        String model = parseModel(parts[2]);
        int power = parsePower(parts[3]);
        int tank = parseTankVolume(parts[4]);
        Date date = parseDate(parts[5]);
        double price = parsePrice(parts[6]);
        return new CoffeeMaker(id, type, model, power, tank, date, price);
    }

    private static int parseInt(String field, String text, int min, int max) throws ValidationException {
        String value = clean(text);
        if (value.isEmpty()) {
            throw new ValidationException(field + ": значение не указано");
        }
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(field + ": '" + value + "' не является целым числом");
        }
        if (number < min || number > max) {
            throw new ValidationException(field + ": значение " + number
                    + " вне диапазона " + min + ".." + max);
        }
        return number;
    }

    private static String clean(String text) {
        return text == null ? "" : text.trim();
    }
}
