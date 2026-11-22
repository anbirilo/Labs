package coffeefactory.sort;

import coffeefactory.model.CoffeeMachine;

import java.util.Comparator;
import java.util.Date;

/**
 * Компараторы полей кофеварки. Сравнение записано лямбда-выражениями.
 */
public final class MachineComparators {
    public static final Comparator<CoffeeMachine> BY_PRICE =
            (left, right) -> Double.compare(left.getPrice(), right.getPrice());

    public static final Comparator<CoffeeMachine> BY_PRICE_DESC =
            (left, right) -> Double.compare(right.getPrice(), left.getPrice());

    public static final Comparator<CoffeeMachine> BY_POWER =
            (left, right) -> Integer.compare(left.getPowerWatts(), right.getPowerWatts());

    public static final Comparator<CoffeeMachine> BY_MODEL =
            (left, right) -> left.getModel().compareToIgnoreCase(right.getModel());

    public static final Comparator<CoffeeMachine> BY_MANUFACTURER =
            (left, right) -> safe(left.getManufacturer()).compareToIgnoreCase(safe(right.getManufacturer()));

    public static final Comparator<CoffeeMachine> BY_DATE = (left, right) -> {
        Date first = left.getManufactureDate();
        Date second = right.getManufactureDate();
        if (first == null && second == null) {
            return 0;
        }
        if (first == null) {
            return 1;
        }
        if (second == null) {
            return -1;
        }
        return first.compareTo(second);
    };

    public static final Comparator<CoffeeMachine> BY_ID =
            (left, right) -> Long.compare(left.getId(), right.getId());

    private MachineComparators() {
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
