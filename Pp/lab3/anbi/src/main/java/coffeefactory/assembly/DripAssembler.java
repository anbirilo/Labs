package coffeefactory.assembly;

import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;

import java.util.List;

public class DripAssembler extends AbstractAssembler {
    @Override
    public MachineType supportedType() {
        return MachineType.DRIP;
    }

    @Override
    protected void applyTechnology(CoffeeMachine machine, List<String> notes) {
        if (Double.compare(machine.getPressureBar(), 0) != 0) {
            machine.setPressureBar(0);
            notes.add("У капельной кофеварки нет помпы, давление обнулено");
        }
    }
}
