package coffeefactory;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import coffeefactory.app.AppContext;
import coffeefactory.catalog.MachineCatalog;
import coffeefactory.menu.ConsoleMenu;


public class Application {
    private final Path dataRoot;
    private MachineCatalog catalog;

    public Application() {
        this(Paths.get("data"));
    }

    public Application(Path dataRoot) {
        this.dataRoot = dataRoot;
    }

    public void start() {
        start(new Scanner(System.in, StandardCharsets.UTF_8.name()));
    }

    public void start(Scanner scanner) {
        useUtf8Console();
        AppContext context = AppContext.create(dataRoot, true);
        this.catalog = context.getCatalog();
        context.getLogger().reset();
        catalog.loadText();
        ConsoleMenu menu = createMenu(context, scanner);
        menu.run();
    }

    public ConsoleMenu createMenu(AppContext context, Scanner scanner) {
        return new ConsoleMenu(context, scanner);
    }

    public MachineCatalog getCatalog() {
        return catalog;
    }

    private static void useUtf8Console() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UTF-8 недоступен", exception);
        }
    }
}
