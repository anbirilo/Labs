package coffeefactory.assembly;

import coffeefactory.model.CoffeeMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AssemblyResult {
    private final CoffeeMachine machine;
    private final List<String> notes;

    public AssemblyResult(CoffeeMachine machine, List<String> notes) {
        this.machine = machine;
        this.notes = Collections.unmodifiableList(new ArrayList<String>(notes));
    }

    public CoffeeMachine getMachine() {
        return machine;
    }

    public List<String> getNotes() {
        return notes;
    }
}
