package coffeefactory.validation;

import coffeefactory.model.CoffeeMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MappedRecord {
    private final CoffeeMachine machine;
    private final List<String> errors;
    private final boolean rejected;

    private MappedRecord(CoffeeMachine machine, List<String> errors, boolean rejected) {
        this.machine = machine;
        this.errors = Collections.unmodifiableList(new ArrayList<String>(errors));
        this.rejected = rejected;
    }

    public static MappedRecord accepted(CoffeeMachine machine, List<String> errors) {
        return new MappedRecord(machine, errors, false);
    }

    public static MappedRecord rejected(List<String> errors) {
        return new MappedRecord(null, errors, true);
    }

    public CoffeeMachine getMachine() {
        return machine;
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean isRejected() {
        return rejected;
    }
}
