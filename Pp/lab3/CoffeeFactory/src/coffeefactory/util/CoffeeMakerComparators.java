package coffeefactory.util;

import coffeefactory.model.CoffeeMaker;

import java.util.Comparator;

/**
 * Компараторы для сортировки по выбранным полям — лямбда-выражения (п. 11).
 */
public final class CoffeeMakerComparators {
    public static final Comparator<CoffeeMaker> BY_ID =
            (a, b) -> Integer.compare(a.getId(), b.getId());

    public static final Comparator<CoffeeMaker> BY_TYPE =
            (a, b) -> a.getType().compareTo(b.getType());

    public static final Comparator<CoffeeMaker> BY_MODEL =
            (a, b) -> a.getModel().compareToIgnoreCase(b.getModel());

    public static final Comparator<CoffeeMaker> BY_POWER =
            (a, b) -> Integer.compare(a.getPowerW(), b.getPowerW());

    public static final Comparator<CoffeeMaker> BY_TANK =
            (a, b) -> Integer.compare(a.getTankVolumeMl(), b.getTankVolumeMl());

    public static final Comparator<CoffeeMaker> BY_DATE =
            (a, b) -> a.getReleaseDate().compareTo(b.getReleaseDate());

    public static final Comparator<CoffeeMaker> BY_PRICE =
            (a, b) -> Double.compare(a.getPrice(), b.getPrice());

    private CoffeeMakerComparators() {
    }
}
