package coffeefactory.xml;

import coffeefactory.common.DataAccessException;
import coffeefactory.io.AbstractReader;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.validation.ErrorLogger;
import coffeefactory.validation.RecordLoader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Чтение XML событийным SAX-парсером.
 */
public class XmlSaxHandler extends AbstractReader<CoffeeMachine> {
    private final RecordLoader loader;

    public XmlSaxHandler(ErrorLogger logger) {
        this.loader = new RecordLoader(logger);
    }

    @Override
    public List<CoffeeMachine> read(Path path) {
        try (InputStream input = Files.newInputStream(path)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            try {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            } catch (ParserConfigurationException | SAXException ignored) {
                // Флаги безопасности зависят от реализации парсера.
            }
            SAXParser parser = factory.newSAXParser();
            MachineHandler handler = new MachineHandler();
            parser.parse(new InputSource(input), handler);
            List<CoffeeMachine> result = new ArrayList<CoffeeMachine>();
            for (Map<String, String> fields : handler.records) {
                loader.consume(result, fields, raw(fields));
            }
            return result;
        } catch (Exception exception) {
            throw new DataAccessException("Ошибка SAX-чтения " + path, exception);
        }
    }

    private static String raw(Map<String, String> fields) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < CoffeeMachine.COLUMNS.length; i++) {
            if (i > 0) {
                builder.append(';');
            }
            String value = fields.get(CoffeeMachine.COLUMNS[i]);
            builder.append(value == null ? "" : value);
        }
        return builder.toString();
    }

    private static final class MachineHandler extends DefaultHandler {
        private final List<Map<String, String>> records = new ArrayList<Map<String, String>>();
        private Map<String, String> current;
        private StringBuilder text = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (XmlNames.ITEM.equals(qName)) {
                current = new LinkedHashMap<String, String>();
                current.put(XmlNames.ID, attributes.getValue(XmlNames.ID));
            }
            text.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            text.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (XmlNames.ITEM.equals(qName)) {
                if (current != null) {
                    records.add(current);
                }
                current = null;
            } else if (current != null) {
                current.put(qName, text.toString().trim());
            }
            text.setLength(0);
        }
    }
}
