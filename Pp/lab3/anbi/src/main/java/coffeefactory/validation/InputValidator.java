package coffeefactory.validation;

import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Проверка чисел, дат и текстовых полей кофеварки.
 */
public class InputValidator {
    public Long parseId(String raw) {
        String value = normalize(raw);
        if (value == null || !value.matches("[1-9]\\d{0,17}")) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public MachineType parseType(String raw) {
        return MachineType.fromString(raw);
    }

    public Integer parsePower(String raw) {
        return parseBoundedInt(raw, 0, 5000);
    }

    public Integer parseWater(String raw) {
        return parseBoundedInt(raw, 0, 5000);
    }

    public Integer parseCups(String raw) {
        return parseBoundedInt(raw, 0, 20);
    }

    public Integer parseWarranty(String raw) {
        return parseBoundedInt(raw, 0, 120);
    }

    public Double parsePressure(String raw) {
        return parseBoundedDecimal(raw, 30);
    }

    public Double parsePrice(String raw) {
        return parseBoundedDecimal(raw, 10_000_000d);
    }

    public Date parseDate(String raw) {
        String value = normalize(raw);
        if (value == null || value.isEmpty()) {
            return null;
        }
        String[] patterns = {CoffeeMachine.DATE_PATTERN, "yyyy-MM-dd"};
        for (String pattern : patterns) {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            format.setLenient(false);
            ParsePosition position = new ParsePosition(0);
            Date date = format.parse(value, position);
            if (date == null || position.getIndex() != value.length()) {
                continue;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            if (year < 1970 || year > 2035) {
                return null;
            }
            return date;
        }
        return null;
    }

    public String parseModel(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty() || value.length() > 80 || value.indexOf(';') >= 0) {
            return null;
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                return null;
            }
        }
        return value;
    }

    /**
     * @return пустая строка, если поле не заполнено; null, если значение некорректно
     */
    public String parseLabel(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.length() > 40 || !value.matches("[\\p{L}\\p{N} ._+\\-/]+")) {
            return null;
        }
        return value;
    }

    private Integer parseBoundedInt(String raw, int min, int max) {
        String value = normalize(raw);
        if (value == null || !value.matches("0|[1-9]\\d{0,6}")) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < min || parsed > max) {
                return null;
            }
            return parsed;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Double parseBoundedDecimal(String raw, double max) {
        String value = normalize(raw);
        if (value == null) {
            return null;
        }
        value = value.replace(',', '.');
        if (!value.matches("\\d+(\\.\\d{1,2})?")) {
            return null;
        }
        if (value.length() > 1 && value.charAt(0) == '0' && value.charAt(1) != '.') {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (parsed < 0 || parsed > max) {
                return null;
            }
            return parsed;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalize(String raw) {
        return raw == null ? null : raw.trim();
    }
}
