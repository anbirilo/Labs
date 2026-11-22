package coffeefactory.model;

import java.util.Locale;

/**
 * Линии сборки фабрики: различные виды кофеварок.
 */
public enum MachineType {
    ESPRESSO("Рожковая эспрессо"),
    DRIP("Капельная"),
    CAPSULE("Капсульная"),
    AUTOMATIC("Автоматическая"),
    MOKA("Гейзерная"),
    POD("Чалдовая");

    private final String title;

    MachineType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static MachineType fromString(String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.trim().toLowerCase(Locale.ROOT);
        if (key.isEmpty()) {
            return null;
        }
        for (MachineType type : values()) {
            if (type.name().toLowerCase(Locale.ROOT).equals(key)
                    || type.title.toLowerCase(Locale.ROOT).equals(key)) {
                return type;
            }
        }
        if ("эспрессо".equals(key) || "рожковая".equals(key)) {
            return ESPRESSO;
        }
        if ("капельная".equals(key) || "фильтрационная".equals(key)) {
            return DRIP;
        }
        if ("капсульная".equals(key)) {
            return CAPSULE;
        }
        if ("автоматическая".equals(key) || "автомат".equals(key)) {
            return AUTOMATIC;
        }
        if ("гейзерная".equals(key) || "мока".equals(key)) {
            return MOKA;
        }
        if ("чалдовая".equals(key) || "чалды".equals(key)) {
            return POD;
        }
        return null;
    }
}
