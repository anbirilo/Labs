package coffeefactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import coffeefactory.common.DataAccessException;
import coffeefactory.common.Io;
import coffeefactory.json.JsonCodec;
import coffeefactory.json.JsonHandler;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.model.MachineType;
import coffeefactory.validation.ErrorLogger;
import coffeefactory.xml.DtdValidator;
import coffeefactory.xml.XmlDomHandler;
import coffeefactory.xml.XmlSaxHandler;
import coffeefactory.xml.XmlStaxHandler;
import coffeefactory.xml.XsdValidator;

class XmlJsonTest {
    @TempDir
    Path temp;

    private final ErrorLogger logger = ErrorLogger.silent();

    @Test
    void domSaxAndStaxRoundTripSpecialCharacters() {
        List<CoffeeMachine> source = Arrays.asList(
                Fixtures.special(),
                Fixtures.machine(2, MachineType.AUTOMATIC, "Philips", 39990)
        );
        Path xml = temp.resolve("machines.xml");
        XmlDomHandler dom = new XmlDomHandler(logger);
        dom.write(xml, source);
        assertEquals(source, dom.read(xml));
        assertEquals(source, new XmlSaxHandler(logger).read(xml));
        assertEquals(source, new XmlStaxHandler(logger).read(xml));

        Path staxFile = temp.resolve("stax.xml");
        new XmlStaxHandler(logger).write(staxFile, source);
        assertEquals(source, new XmlSaxHandler(logger).read(staxFile));
        assertEquals(source, dom.read(staxFile));
    }

    @Test
    void validDocumentMatchesXsdAndDtd() {
        Path xml = temp.resolve("valid.xml");
        new XmlDomHandler(logger).write(xml, Collections.singletonList(
                Fixtures.machine(1, MachineType.ESPRESSO, "Dedica", 24990.5)));
        assertTrue(new XsdValidator().validate(xml, Paths.get("data", "schema", "coffeemachine.xsd")).isEmpty());
        assertTrue(new DtdValidator().validate(xml, Paths.get("data", "schema", "coffeemachine.dtd")).isEmpty());
    }

    @Test
    void brokenXmlFailsXsdAndDtd() {
        Path xsdFile = temp.resolve("bad-xsd.xml");
        Io.writeString(xsdFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<coffeeMachines><coffeeMachine id=\"0\"><type>ESPRESSO</type></coffeeMachine></coffeeMachines>\n");
        assertFalse(new XsdValidator().validate(xsdFile, Paths.get("data", "schema", "coffeemachine.xsd")).isEmpty());

        Path dtdFile = temp.resolve("bad-dtd.xml");
        Io.writeString(dtdFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<coffeeMachines><coffeeMachine id=\"1\"></coffeeMachine></coffeeMachines>\n");
        assertFalse(new DtdValidator().validate(dtdFile, Paths.get("data", "schema", "coffeemachine.dtd")).isEmpty());
    }

    @Test
    void jsonRoundTripAndEscapes() {
        JsonCodec codec = new JsonCodec();
        String raw = "[{\"id\":1,\"type\":\"ESPRESSO\",\"model\":\"A \\\"Quote\\\"\","
                + "\"powerWatts\":1000,\"pressureBar\":9.5,\"waterTankMl\":500,\"cupsPerCycle\":1,"
                + "\"manufactureDate\":null,\"price\":10.5,\"manufacturer\":\"\u0417\u0430\u0432\u043e\u0434\","
                + "\"color\":\"red\",\"warrantyMonths\":12}]";
        Map<String, String> fields = codec.parse(raw).get(0);
        assertEquals("A \"Quote\"", fields.get("model"));
        assertNull(fields.get("manufactureDate"));
        assertEquals("Завод", fields.get("manufacturer"));

        JsonHandler handler = new JsonHandler(logger);
        Path file = temp.resolve("machines.json");
        List<CoffeeMachine> source = Arrays.asList(Fixtures.special(), Fixtures.machine(3, MachineType.POD, "Tassimo", 7990));
        handler.write(file, source);
        assertEquals(source, handler.read(file));
        assertTrue(Io.readString(file).contains("\\\""));
    }

    @Test
    void jsonRejectsSyntaxAndKeepsRepairedValues() {
        JsonHandler handler = new JsonHandler(new ErrorLogger(temp.resolve("errors.log"), false));
        Path broken = temp.resolve("broken.json");
        Io.writeString(broken, "{");
        assertThrows(DataAccessException.class, () -> handler.read(broken));

        Path repaired = temp.resolve("repaired.json");
        Io.writeString(repaired, "[{\"id\":4,\"type\":\"капельная\",\"model\":\"Фильтр\",\"powerWatts\":-5,"
                + "\"pressureBar\":0,\"waterTankMl\":700,\"cupsPerCycle\":4,\"manufactureDate\":\"05.09.2020\","
                + "\"price\":4990,\"manufacturer\":\"Kitfort\",\"color\":\"чёрный\",\"warrantyMonths\":12}]");
        List<CoffeeMachine> machines = handler.read(repaired);
        assertEquals(1, machines.size());
        assertEquals(MachineType.DRIP, machines.get(0).getType());
        assertEquals(0, machines.get(0).getPowerWatts());
        assertTrue(Io.readString(temp.resolve("errors.log")).contains("-5"));
    }

    @Test
    void emptyJsonIsAnEmptyCatalog() {
        Path file = temp.resolve("empty.json");
        Io.writeString(file, "  \n");
        assertTrue(new JsonHandler(logger).read(file).isEmpty());
        assertEquals(0, new JsonCodec().parse("[]").size());
    }
}
