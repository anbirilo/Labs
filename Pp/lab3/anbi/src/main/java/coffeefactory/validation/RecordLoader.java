package coffeefactory.validation;

import coffeefactory.model.CoffeeMachine;

import java.util.List;
import java.util.Map;

/**
 * Принимает разобранные поля, пишет ошибки и складывает годные объекты.
 */
public class RecordLoader {
    private final CoffeeMachineMapper mapper;
    private final ErrorLogger logger;

    public RecordLoader(ErrorLogger logger) {
        this(new CoffeeMachineMapper(new InputValidator()), logger);
    }

    public RecordLoader(CoffeeMachineMapper mapper, ErrorLogger logger) {
        this.mapper = mapper;
        this.logger = logger;
    }

    public void consume(List<CoffeeMachine> sink, Map<String, String> fields, String raw) {
        MappedRecord record = mapper.map(fields, raw);
        for (String error : record.getErrors()) {
            logger.log(error, raw);
        }
        if (!record.isRejected() && record.getMachine() != null) {
            sink.add(record.getMachine());
        }
    }
}
