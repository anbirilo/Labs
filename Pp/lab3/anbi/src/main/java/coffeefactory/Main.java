package coffeefactory;

/**
 * Точка входа. В main только создаётся приложение и вызывается его метод.
 * Меню собирается отдельно, в Application.createMenu.
 */
public class Main {
    public static void main(String[] args) {
        Application application = new Application();
        application.start();
    }
}
