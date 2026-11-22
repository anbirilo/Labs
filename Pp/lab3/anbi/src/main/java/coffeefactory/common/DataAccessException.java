package coffeefactory.common;

/**
 * Ошибка чтения, записи или криптографической операции.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
