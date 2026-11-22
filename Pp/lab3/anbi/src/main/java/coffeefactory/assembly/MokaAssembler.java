package coffeefactory.assembly;

import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;

import java.util.List;

public class MokaAssembler extends AbstractAssembler {
    @Override
    public MachineType supportedType() {
        return MachineType.MOKA;
    }

    @Override
    protected void applyTechnology(CoffeeMachine machine, List<String> notes) {
        if (machine.getPowerWatts() != 0) {
            machine.setPowerWatts(0);
            notes.add("Гейзерная кофеварка на плите не использует ТЭН, мощность обнулена");
        }
        if (Double.compare(machine.getPressureBar(), 0) != 0) {
            machine.setPressureBar(0);
            notes.add("Для гейзерной кофеварки давление помпы обнулено");
        }
    }
}
