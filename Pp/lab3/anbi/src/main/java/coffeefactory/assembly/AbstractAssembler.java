package coffeefactory.assembly;

import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;

import java.util.ArrayList;
import java.util.List;

/**
 * Абстрактная линия сборки. Общие поля заполняются здесь, особенности типа — в наследниках.
 */
public abstract class AbstractAssembler {
    public abstract MachineType supportedType();

    public final AssemblyResult assemble(AssemblyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Пустая заявка на сборку");
        }
        if (request.getType() != supportedType()) {
            throw new IllegalArgumentException("Линия " + supportedType() + " не собирает тип " + request.getType());
        }
        if (request.getModel() == null || request.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Не указана модель");
        }
        if (request.getId() <= 0) {
            throw new IllegalArgumentException("Не указан идентификатор");
        }
        CoffeeMachine machine = new CoffeeMachine();
        machine.setId(request.getId());
        machine.setType(supportedType());
        machine.setModel(request.getModel().trim());
        machine.setPowerWatts(request.getPowerWatts());
        machine.setPressureBar(request.getPressureBar());
        machine.setWaterTankMl(request.getWaterTankMl());
        machine.setCupsPerCycle(request.getCupsPerCycle());
        machine.setManufactureDate(request.getManufactureDate());
        machine.setPrice(request.getPrice());
        machine.setManufacturer(request.getManufacturer() == null ? "" : request.getManufacturer());
        machine.setColor(request.getColor() == null ? "" : request.getColor());
        machine.setWarrantyMonths(request.getWarrantyMonths());
        List<String> notes = new ArrayList<String>();
        applyTechnology(machine, notes);
        return new AssemblyResult(machine, notes);
    }

    protected abstract void applyTechnology(CoffeeMachine machine, List<String> notes);
}
