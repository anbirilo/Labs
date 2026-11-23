package coffeefactory.validation;

import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CoffeeMachineMapper {
    private final InputValidator validator;

    public CoffeeMachineMapper(InputValidator validator) {
        this.validator = validator;
    }

    public MappedRecord map(Map<String, String> fields, String raw) {
        List<String> errors = new ArrayList<String>();
        String idRaw = value(fields, "id");
        Long id = validator.parseId(idRaw);
        if (id == null) {
            errors.add("Некорректный id '" + idRaw + "', запись пропущена");
            return MappedRecord.rejected(errors);
        }
        String model = validator.parseModel(value(fields, "model"));
        if (model == null) {
            errors.add("Некорректная модель '" + value(fields, "model") + "', запись пропущена");
            return MappedRecord.rejected(errors);
        }

        CoffeeMachine machine = new CoffeeMachine();
        machine.setId(id);
        machine.setModel(model);
        machine.setType(readType(fields, errors));
        machine.setPowerWatts(readInt(fields, "powerWatts", "мощность", errors));
        machine.setPressureBar(readDecimal(fields, "pressureBar", "давление", true, errors));
        machine.setWaterTankMl(readWater(fields, errors));
        machine.setCupsPerCycle(readCups(fields, errors));
        machine.setManufactureDate(readDate(fields, errors));
        machine.setPrice(readDecimal(fields, "price", "цена", false, errors));
        machine.setManufacturer(readLabel(fields, "manufacturer", "производитель", errors));
        machine.setColor(readLabel(fields, "color", "цвет", errors));
        machine.setWarrantyMonths(readWarranty(fields, errors));
        return MappedRecord.accepted(machine, errors);
    }

    private MachineType readType(Map<String, String> fields, List<String> errors) {
        String raw = value(fields, "type");
        if (raw.isEmpty()) {
            errors.add("Пустой тип, поле очищено");
            return null;
        }
        MachineType type = validator.parseType(raw);
        if (type == null) {
            errors.add("Некорректный тип '" + raw + "', поле очищено");
            return null;
        }
        return type;
    }

    private int readInt(Map<String, String> fields, String key, String title, List<String> errors) {
        String raw = value(fields, key);
        Integer parsed = validator.parsePower(raw);
        if (parsed == null) {
            errors.add("Некорректная " + title + " '" + raw + "', установлено 0");
            return 0;
        }
        return parsed;
    }

    private int readWater(Map<String, String> fields, List<String> errors) {
        String raw = value(fields, "waterTankMl");
        Integer parsed = validator.parseWater(raw);
        if (parsed == null) {
            errors.add("Некорректный резервуар '" + raw + "', установлено 0");
            return 0;
        }
        return parsed;
    }

    private int readCups(Map<String, String> fields, List<String> errors) {
        String raw = value(fields, "cupsPerCycle");
        Integer parsed = validator.parseCups(raw);
        if (parsed == null) {
            errors.add("Некорректное число чашек '" + raw + "', установлено 0");
            return 0;
        }
        return parsed;
    }

    private int readWarranty(Map<String, String> fields, List<String> errors) {
        String raw = value(fields, "warrantyMonths");
        Integer parsed = validator.parseWarranty(raw);
        if (parsed == null) {
            errors.add("Некорректная гарантия '" + raw + "', установлено 0");
            return 0;
        }
        return parsed;
    }

    private double readDecimal(Map<String, String> fields, String key, String title, boolean pressure, List<String> errors) {
        String raw = value(fields, key);
        Double parsed = pressure ? validator.parsePressure(raw) : validator.parsePrice(raw);
        if (parsed == null) {
            errors.add("Некорректное поле «" + title + "» '" + raw + "', установлено 0");
            return 0;
        }
        return parsed;
    }

    private Date readDate(Map<String, String> fields, List<String> errors) {
        String raw = value(fields, "manufactureDate");
        if (raw.isEmpty()) {
            return null;
        }
        Date date = validator.parseDate(raw);
        if (date == null) {
            errors.add("Некорректная дата '" + raw + "', установлено пустое значение");
            return null;
        }
        return date;
    }

    private String readLabel(Map<String, String> fields, String key, String title, List<String> errors) {
        String raw = value(fields, key);
        String parsed = validator.parseLabel(raw);
        if (parsed == null) {
            errors.add("Некорректное поле «" + title + "» '" + raw + "', поле очищено");
            return "";
        }
        return parsed;
    }

    static String value(Map<String, String> fields, String key) {
        if (fields == null || fields.get(key) == null) {
            return "";
        }
        return fields.get(key).trim();
    }
}
