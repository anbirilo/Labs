package coffeefactory.util;

/** Ошибка проверки входных данных (п. 7). */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
