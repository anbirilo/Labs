package coffeefactory;

import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;
import coffeefactory.validation.InputValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class Fixtures {
    private static final InputValidator VALIDATOR = new InputValidator();

    private Fixtures() {
    }

    static CoffeeMachine machine(long id, MachineType type, String model, double price) {
        CoffeeMachine machine = new CoffeeMachine();
        machine.setId(id);
        machine.setType(type);
        machine.setModel(model);
        machine.setPowerWatts(1300);
        machine.setPressureBar(15);
        machine.setWaterTankMl(1000);
        machine.setCupsPerCycle(1);
        machine.setManufactureDate(VALIDATOR.parseDate("15.03.2022"));
        machine.setPrice(price);
        machine.setManufacturer("DeLonghi");
        machine.setColor("silver");
        machine.setWarrantyMonths(24);
        return machine;
    }

    static CoffeeMachine special() {
        CoffeeMachine machine = machine(7, MachineType.ESPRESSO, "A & B <C> \"X\"", 10.5);
        machine.setManufactureDate(null);
        machine.setPressureBar(VALIDATOR.parsePressure("9.5"));
        machine.setManufacturer("Завод");
        return machine;
    }

    static List<CoffeeMachine> pair() {
        return new ArrayList<CoffeeMachine>(Arrays.asList(
                machine(2, MachineType.DRIP, "Kitfort", 4990),
                machine(1, MachineType.CAPSULE, "Nespresso", 12990)
        ));
    }
}
