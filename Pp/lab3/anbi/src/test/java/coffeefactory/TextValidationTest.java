package coffeefactory;

import coffeefactory.catalog.MachineCatalog;
import coffeefactory.common.Io;
import coffeefactory.io.TextFileHandler;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;
import coffeefactory.storage.ListStorage;
import coffeefactory.storage.MapStorage;
import coffeefactory.validation.CoffeeMachineMapper;
import coffeefactory.validation.ErrorLogger;
import coffeefactory.validation.InputValidator;
import coffeefactory.validation.MappedRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextValidationTest {
    @TempDir
    Path temp;

    @Test
    void invalidIdOrModelDropsWholeRecord() {
        CoffeeMachineMapper mapper = new CoffeeMachineMapper(new InputValidator());
        MappedRecord badId = mapper.map(fields("bad-id", "ESPRESSO", "Модель"), "bad-id;...");
        MappedRecord badModel = mapper.map(fields("9", "DRIP", ""), "9;DRIP;;");
        assertTrue(badId.isRejected());
        assertNull(badId.getMachine());
        assertTrue(badModel.isRejected());
        assertTrue(badId.getErrors().get(0).contains("id"));
    }

    @Test
    void invalidNumbersDateAndLabelsAreClearedButRecordStays() {
        CoffeeMachineMapper mapper = new CoffeeMachineMapper(new InputValidator());
        Map<String, String> fields = fields("11", "ESPRESSO", "Плохие числа");
        fields.put("powerWatts", "мощность");
        fields.put("pressureBar", "давление");
        fields.put("waterTankMl", "-5");
        fields.put("cupsPerCycle", "0");
        fields.put("manufactureDate", "31.02.2020");
        fields.put("price", "-100");
        fields.put("manufacturer", "###");
        fields.put("color", "red!");
        fields.put("warrantyMonths", "999");
        MappedRecord record = mapper.map(fields, "11;плохая строка");
        assertFalse(record.isRejected());
        CoffeeMachine machine = record.getMachine();
        assertEquals(0, machine.getPowerWatts());
        assertEquals(0, machine.getPressureBar(), 0.001);
        assertEquals(0, machine.getWaterTankMl());
        assertEquals(0, machine.getCupsPerCycle());
        assertNull(machine.getManufactureDate());
        assertEquals(0, machine.getPrice(), 0.001);
        assertEquals("", machine.getManufacturer());
        assertEquals("", machine.getColor());
        assertEquals(0, machine.getWarrantyMonths());
        assertTrue(record.getErrors().size() >= 7);
    }

    @Test
    void unknownTypeIsClearedWithoutDroppingMachine() {
        CoffeeMachineMapper mapper = new CoffeeMachineMapper(new InputValidator());
        Map<String, String> fields = fields("10", "ROCKET", "Неизвестный тип");
        MappedRecord record = mapper.map(fields, "10;ROCKET");
        assertFalse(record.isRejected());
        assertNull(record.getMachine().getType());
        assertEquals("Неизвестный тип", record.getMachine().getModel());
    }

    @Test
    void sampleFileReportsBadLinesAndKeepsRepairedMachines() {
        Path copy = temp.resolve("coffeemachines.txt");
        String original = ""
                + "# Каталог\n"
                + "id;type;model;powerWatts;pressureBar;waterTankMl;cupsPerCycle;manufactureDate;price;manufacturer;color;warrantyMonths\n"
                + "1;ESPRESSO;DeLonghi Dedica EC685;1350;15;1100;1;15.03.2022;24990.50;DeLonghi;серебристый;24\n"
                + "2;AUTOMATIC;Philips Series 2200 EP2220;1500;15;1800;2;01.11.2023;39990;Philips;чёрный;24\n"
                + "3;CAPSULE;Nespresso Vertuo Next;1300;2.4;1200;1;20.06.2021;12990;Nespresso;белый;12\n"
                + "4;DRIP;Kitfort KT-719;1000;0;700;12;05.09.2020;4990;Kitfort;чёрный;12\n"
                + "5;MOKA;Bialetti Brikka;0;0;180;2;11.01.2019;3490;Bialetti;серебристый;12\n"
                + "6;AUTOMATIC;Krups Evidence EA891;1450;15;2600;2;18.04.2024;54990;Krups;серый;24\n"
                + "7;ESPRESSO;Polaris PCM 1535E;1400;15;1000;2;30.08.2022;18990;Polaris;чёрный;24\n"
                + "8;POD;Bosch Tassimo Style;1300;3.3;700;1;12.12.2021;7990;Bosch;белый;12\n"
                + "bad-id;ESPRESSO;Сломанный идентификатор;1300;15;1000;1;01.01.2020;10000;Test;красный;12\n"
                + "9;DRIP;;1000;0;500;4;01.01.2020;4500;NoModel;красный;12\n"
                + "10;ROCKET;Неизвестный тип;1500;15;1000;1;01.01.2020;10000;Test;красный;12\n"
                + "11;ESPRESSO;Плохие числа;мощность;давление;-5;0;31.02.2020;-100;###;red!;999\n"
                + "12;ESPRESSO;Garlyn L70;1200;15;900;1;02.02.2023;15990;Garlyn;чёрный;24\n"
                + "12;ESPRESSO;Дубликат Garlyn;1200;15;900;1;02.02.2023;15990;Garlyn;чёрный;24\n"
                + "слишком;мало;полей\n";
        Io.writeString(copy, original);
        ErrorLogger logger = new ErrorLogger(temp.resolve("errors.log"), false);
        TextFileHandler handler = new TextFileHandler(logger);
        List<CoffeeMachine> read = handler.read(copy);
        assertEquals(12, read.size());
        String log = Io.readString(temp.resolve("errors.log"));
        assertTrue(log.contains("Сломанный идентификатор"));
        assertTrue(log.contains("слишком;мало;полей"));
        assertTrue(log.contains("ROCKET"));
        assertTrue(log.contains("31.02.2020"));
        assertTrue(log.contains("###"));

        MachineCatalog catalog = new MachineCatalog(
                new ListStorage(), new MapStorage(), handler, logger, copy);
        catalog.loadText();
        assertEquals(11, catalog.size());
        assertNull(catalog.find(9));
        assertNull(catalog.find(10).getType());
        assertEquals(0, catalog.find(11).getPrice(), 0.001);
        assertEquals("", catalog.find(11).getColor());
        assertEquals("Garlyn L70", catalog.find(12).getModel());
        assertEquals(24990.5, catalog.find(1).getPrice(), 0.001);
        assertEquals(MachineType.CAPSULE, catalog.find(3).getType());
        assertTrue(Io.readString(temp.resolve("errors.log")).contains("Дубликат id=12"));
        assertEquals(original, Io.readString(copy));
    }

    @Test
    void textRoundTripPreservesMachinesAndSkipsComments() {
        ErrorLogger logger = new ErrorLogger(temp.resolve("errors.log"), false);
        TextFileHandler handler = new TextFileHandler(logger);
        Path file = temp.resolve("round.txt");
        List<CoffeeMachine> source = Fixtures.pair();
        handler.write(file, source);
        List<CoffeeMachine> loaded = handler.read(file);
        assertEquals(source, loaded);
        Io.writeString(file, "# comment\n\nid;type;model;powerWatts;pressureBar;waterTankMl;cupsPerCycle;manufactureDate;price;manufacturer;color;warrantyMonths\n"
                + source.get(0).toFileLine() + "\n");
        assertEquals(1, handler.read(file).size());
    }

    @Test
    void missingFileDoesNotThrowAndIsLogged() {
        ErrorLogger logger = new ErrorLogger(temp.resolve("errors.log"), false);
        TextFileHandler handler = new TextFileHandler(logger);
        assertTrue(handler.read(temp.resolve("missing.txt")).isEmpty());
        assertTrue(Io.readString(temp.resolve("errors.log")).contains("не найден"));
    }

    private static Map<String, String> fields(String id, String type, String model) {
        Map<String, String> fields = new LinkedHashMap<String, String>();
        for (String column : CoffeeMachine.COLUMNS) {
            fields.put(column, "0");
        }
        fields.put("id", id);
        fields.put("type", type);
        fields.put("model", model);
        fields.put("powerWatts", "1000");
        fields.put("pressureBar", "9");
        fields.put("waterTankMl", "500");
        fields.put("cupsPerCycle", "1");
        fields.put("manufactureDate", "01.01.2020");
        fields.put("price", "1000");
        fields.put("manufacturer", "Test");
        fields.put("color", "red");
        fields.put("warrantyMonths", "12");
        return fields;
    }
}
