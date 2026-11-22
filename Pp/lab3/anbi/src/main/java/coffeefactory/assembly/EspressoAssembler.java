package coffeefactory.assembly;

import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;

import java.util.List;

public class EspressoAssembler extends AbstractAssembler {
    @Override
    public MachineType supportedType() {
        return MachineType.ESPRESSO;
    }

    @Override
    protected void applyTechnology(CoffeeMachine machine, List<String> notes) {
        if (machine.getPressureBar() < 9) {
            machine.setPressureBar(9);
            notes.add("Для эспрессо давление поднято до 9 бар");
        }
        if (machine.getPowerWatts() < 1000) {
            machine.setPowerWatts(1000);
            notes.add("Для эспрессо мощность поднята до 1000 Вт");
        }
    }
}
