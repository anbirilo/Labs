package coffeefactory.assembly;

import java.util.Arrays;
import java.util.List;

import coffeefactory.model.MachineType;

public class AssemblyFactory {
    private final List<AbstractAssembler> lines;

    public AssemblyFactory() {
        this.lines = Arrays.asList(
                new EspressoAssembler(),
                new DripAssembler(),
                new CapsuleAssembler(),
                new AutomaticAssembler(),
                new MokaAssembler(),
                new PodAssembler()
        );
    }

    public AssemblyResult assemble(AssemblyRequest request) {
        if (request == null || request.getType() == null) {
            throw new IllegalArgumentException("Не выбран тип кофеварки");
        }
        for (AbstractAssembler line : lines) {
            if (line.supportedType() == request.getType()) {
                return line.assemble(request);
            }
        }
        throw new IllegalArgumentException("Нет линии сборки для типа " + request.getType());
    }

    public MachineType[] availableTypes() {
        MachineType[] types = new MachineType[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            types[i] = lines.get(i).supportedType();
        }
        return types;
    }
}
