package coffeefactory.model;

import java.util.Locale;

/**
 * Тип кофеварки. В файл записывается имя константы (name()),
 * на экран выводится русское название (toString()).
 */
public enum CoffeeMakerType {
    DRIP("Капельная"),
    ESPRESSO("Рожковая (эспрессо)"),
    CAPSULE("Капсульная"),
    GEYSER("Гейзерная"),
    AUTOMATIC("Кофемашина-автомат");

    private final String title;

    CoffeeMakerType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }

    /**
     * Разбирает тип из строки: либо имя константы (DRIP, espresso...),
     * либо номер из списка (1..N).
     *
     * @throws IllegalArgumentException если такого типа нет
     */
    public static CoffeeMakerType parse(String text) {
        String value = text.trim();
        if (value.matches("\\d+")) {
            int index = Integer.parseInt(value); // при переполнении тоже IllegalArgumentException
            if (index >= 1 && index <= values().length) {
                return values()[index - 1];
            }
            throw new IllegalArgumentException("нет типа с номером " + index);
        }
        return valueOf(value.toUpperCase(Locale.ROOT));
    }

    /** Список имён через запятую: "DRIP, ESPRESSO, ..." */
    public static String names() {
        StringBuilder sb = new StringBuilder();
        for (CoffeeMakerType t : values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(t.name());
        }
        return sb.toString();
    }

    /** Подсказка для консоли: "1 — Капельная, 2 — ..." */
    public static String hint() {
        StringBuilder sb = new StringBuilder();
        CoffeeMakerType[] all = values();
        for (int i = 0; i < all.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(i + 1).append(" — ").append(all[i].title);
        }
        return sb.toString();
    }
}
