package coffeefactory;

import coffeefactory.assembly.AbstractAssembler;
import coffeefactory.assembly.AssemblyFactory;
import coffeefactory.assembly.AssemblyRequest;
import coffeefactory.assembly.AssemblyResult;
import coffeefactory.assembly.EspressoAssembler;
import coffeefactory.io.AbstractReader;
import coffeefactory.io.AbstractWriter;
import coffeefactory.model.AbstractEntity;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;
import coffeefactory.storage.AbstractStorage;
import coffeefactory.validation.InputValidator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelAssemblyTest {
    private final InputValidator validator = new InputValidator();
    private final AssemblyFactory factory = new AssemblyFactory();

    @Test
    void abstractClassesStayAbstract() {
        assertTrue(Modifier.isAbstract(AbstractEntity.class.getModifiers()));
        assertTrue(Modifier.isAbstract(AbstractStorage.class.getModifiers()));
        assertTrue(Modifier.isAbstract(AbstractReader.class.getModifiers()));
        assertTrue(Modifier.isAbstract(AbstractWriter.class.getModifiers()));
        assertTrue(Modifier.isAbstract(AbstractAssembler.class.getModifiers()));
        assertEquals(AbstractEntity.class, CoffeeMachine.class.getSuperclass());
        assertEquals(AbstractReader.class, AbstractWriter.class.getSuperclass());
    }

    @Test
    void gettersSettersAndToStringExposeMachineFields() {
        CoffeeMachine machine = Fixtures.machine(4, MachineType.MOKA, "Brikka", 3490);
        machine.setPowerWatts(0);
        machine.setWaterTankMl(180);
        machine.setColor("серебристый");
        assertEquals(4, machine.getId());
        assertEquals(MachineType.MOKA, machine.getType());
        assertEquals("Brikka", machine.getModel());
        assertEquals(0, machine.getPowerWatts());
        assertEquals(180, machine.getWaterTankMl());
        assertEquals("серебристый", machine.getColor());
        assertTrue(machine.toString().contains("Brikka"));
        assertTrue(machine.toString().contains("Гейзерная"));
        assertTrue(machine.toString().contains("15.03.2022"));
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        CoffeeMachine first = Fixtures.machine(1, MachineType.ESPRESSO, "Dedica", 100);
        CoffeeMachine same = Fixtures.machine(1, MachineType.ESPRESSO, "Dedica", 100);
        CoffeeMachine otherPrice = Fixtures.machine(1, MachineType.ESPRESSO, "Dedica", 200);
        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, otherPrice);
        assertNotEquals(first, null);
    }

    @Test
    void fileLineContainsEveryColumn() {
        String line = Fixtures.machine(3, MachineType.CAPSULE, "Vertuo", 12990).toFileLine();
        assertTrue(line.startsWith("3;CAPSULE;Vertuo;"));
        assertEquals(CoffeeMachine.COLUMNS.length, line.split(";", -1).length);
    }

    @Test
    void russianAndEnglishTypeNamesAreAccepted() {
        assertEquals(MachineType.DRIP, MachineType.fromString("капельная"));
        assertEquals(MachineType.ESPRESSO, MachineType.fromString("рожковая"));
        assertEquals(MachineType.AUTOMATIC, MachineType.fromString("автомат"));
        assertEquals(MachineType.POD, validator.parseType("POD"));
        assertNull(validator.parseType("ROCKET"));
        assertNull(validator.parseType("  "));
    }

    @Test
    void numbersDatesAndLabelsAreChecked() {
        assertEquals(Long.valueOf(12), validator.parseId("12"));
        assertNull(validator.parseId("0"));
        assertNull(validator.parseId("bad-id"));
        assertNull(validator.parseId("-3"));
        assertEquals(Integer.valueOf(5000), validator.parsePower("5000"));
        assertNull(validator.parsePower("5001"));
        assertNull(validator.parsePower("мощность"));
        assertEquals(Double.valueOf(15.5), validator.parsePressure("15,50"));
        assertNull(validator.parsePressure("-1"));
        assertEquals(Double.valueOf(24990.5), validator.parsePrice("24990.50"));
        assertNull(validator.parsePrice("-100"));
        assertNull(validator.parseDate("31.02.2020"));
        assertEquals("15.03.2022", CoffeeMachine.formatDate(validator.parseDate("2022-03-15")));
        assertNull(validator.parseDate(""));
        assertEquals("", validator.parseLabel(""));
        assertNull(validator.parseLabel("red!"));
        assertNull(validator.parseLabel("###"));
        assertEquals("чёрный", validator.parseLabel("чёрный"));
        assertNull(validator.parseModel(""));
        assertEquals("A & B", validator.parseModel(" A & B "));
    }

    @Test
    void espressoLineRaisesPressureAndPower() {
        AssemblyResult result = factory.assemble(request(MachineType.ESPRESSO, 800, 5, 2, 400));
        assertEquals(9, result.getMachine().getPressureBar(), 0.001);
        assertEquals(1000, result.getMachine().getPowerWatts());
        assertEquals(2, result.getNotes().size());
    }

    @Test
    void readyEspressoIsNotAdjusted() {
        AssemblyResult result = factory.assemble(request(MachineType.ESPRESSO, 1350, 15, 1, 1100));
        assertEquals(15, result.getMachine().getPressureBar(), 0.001);
        assertEquals(1350, result.getMachine().getPowerWatts());
        assertTrue(result.getNotes().isEmpty());
    }

    @Test
    void dripCapsuleAutomaticMokaAndPodHaveOwnRules() {
        assertEquals(0, factory.assemble(request(MachineType.DRIP, 1000, 4, 10, 700))
                .getMachine().getPressureBar(), 0.001);
        assertEquals(1, factory.assemble(request(MachineType.CAPSULE, 1300, 3, 4, 800))
                .getMachine().getCupsPerCycle());
        assertEquals(1000, factory.assemble(request(MachineType.AUTOMATIC, 1500, 15, 2, 400))
                .getMachine().getWaterTankMl());
        CoffeeMachine moka = factory.assemble(request(MachineType.MOKA, 200, 2, 2, 180)).getMachine();
        assertEquals(0, moka.getPowerWatts());
        assertEquals(0, moka.getPressureBar(), 0.001);
        assertEquals(1, factory.assemble(request(MachineType.POD, 1300, 3, 2, 700))
                .getMachine().getCupsPerCycle());
    }

    @Test
    void factoryRejectsIncompleteRequest() {
        EspressoAssembler espresso = new EspressoAssembler();
        AssemblyRequest foreign = request(MachineType.DRIP, 1000, 0, 1, 500);
        assertThrows(IllegalArgumentException.class, () -> espresso.assemble(foreign));
        assertThrows(IllegalArgumentException.class, () -> factory.assemble(null));
        AssemblyRequest nameless = request(MachineType.ESPRESSO, 1300, 15, 1, 1000);
        nameless.setModel("  ");
        assertThrows(IllegalArgumentException.class, () -> factory.assemble(nameless));
    }

    private AssemblyRequest request(MachineType type, int power, double pressure, int cups, int water) {
        AssemblyRequest request = new AssemblyRequest();
        request.setId(20);
        request.setType(type);
        request.setModel("Линия " + type.name());
        request.setPowerWatts(power);
        request.setPressureBar(pressure);
        request.setCupsPerCycle(cups);
        request.setWaterTankMl(water);
        request.setPrice(1000);
        request.setManufacturer("Factory");
        request.setColor("black");
        request.setWarrantyMonths(12);
        request.setManufactureDate(validator.parseDate("01.01.2024"));
        return request;
    }
}
