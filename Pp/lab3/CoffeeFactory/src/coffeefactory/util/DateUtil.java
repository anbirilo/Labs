package coffeefactory.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Разбор и вывод дат в формате дд.мм.гггг (используется тип java.util.Date). */
public final class DateUtil {
    public static final String PATTERN = "dd.MM.yyyy";

    private DateUtil() {
    }

    public static Date parse(String text) throws ValidationException {
        String value = text == null ? "" : text.trim();
        if (!value.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
            throw new ValidationException("дата '" + value + "' должна быть в формате " + PATTERN);
        }
        SimpleDateFormat format = new SimpleDateFormat(PATTERN);
        format.setLenient(false); // 31.02.2023 не пройдёт
        Date date = format.parse(value, new ParsePosition(0));
        if (date == null) {
            throw new ValidationException("несуществующая дата '" + value + "'");
        }
        return date;
    }

    public static String format(Date date) {
        return new SimpleDateFormat(PATTERN).format(date);
    }
}
