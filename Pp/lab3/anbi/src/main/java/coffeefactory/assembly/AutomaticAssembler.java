package coffeefactory.assembly;

import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;

import java.util.List;

public class AutomaticAssembler extends AbstractAssembler {
    @Override
    public MachineType supportedType() {
        return MachineType.AUTOMATIC;
    }

    @Override
    protected void applyTechnology(CoffeeMachine machine, List<String> notes) {
        if (machine.getWaterTankMl() < 1000) {
            machine.setWaterTankMl(1000);
            notes.add("Для автомата резервуар увеличен до 1000 мл");
        }
    }
}
