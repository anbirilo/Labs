package coffeefactory.assembly;

import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;

import java.util.List;

public class PodAssembler extends AbstractAssembler {
    @Override
    public MachineType supportedType() {
        return MachineType.POD;
    }

    @Override
    protected void applyTechnology(CoffeeMachine machine, List<String> notes) {
        if (machine.getCupsPerCycle() != 1) {
            machine.setCupsPerCycle(1);
            notes.add("Чалдовая кофеварка готовит 1 чашку за цикл");
        }
    }
}
