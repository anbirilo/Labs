package coffeefactory;

import coffeefactory.service.FactoryService;
import coffeefactory.ui.ConsoleMenu;
import coffeefactory.util.ErrorLogger;

import java.nio.file.Paths;

/**
 * Точка входа (п. 1): main только создаёт объекты и вызывает методы.
 * Создание меню вынесено в отдельную функцию startMenu.
 */
public class Main {

    public static void main(String[] args) {
        ErrorLogger logger = new ErrorLogger(Paths.get("logs", "errors.log"));
        FactoryService service = new FactoryService(Paths.get("data", "coffee_makers.txt"), logger);
        service.loadIfExists();
        startMenu(service);
    }

    private static void startMenu(FactoryService service) {
        ConsoleMenu menu = new ConsoleMenu(service);
        menu.run();
    }
}
