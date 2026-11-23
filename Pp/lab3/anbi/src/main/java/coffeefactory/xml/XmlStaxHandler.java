package coffeefactory.xml;

import coffeefactory.common.DataAccessException;
import coffeefactory.common.Io;
import coffeefactory.io.AbstractWriter;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.validation.ErrorLogger;
import coffeefactory.validation.RecordLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XmlStaxHandler extends AbstractWriter<CoffeeMachine> {
    private final RecordLoader loader;

    public XmlStaxHandler(ErrorLogger logger) {
        this.loader = new RecordLoader(logger);
    }

    @Override
    public List<CoffeeMachine> read(Path path) {
        XMLStreamReader reader = null;
        try (InputStream input = Files.newInputStream(path)) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            disable(factory, XMLInputFactory.SUPPORT_DTD);
            disable(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES);
            reader = factory.createXMLStreamReader(input, "UTF-8");
            List<CoffeeMachine> result = new ArrayList<CoffeeMachine>();
            Map<String, String> fields = null;
            StringBuilder text = new StringBuilder();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (XmlNames.ITEM.equals(reader.getLocalName())) {
                        fields = new LinkedHashMap<String, String>();
                        fields.put(XmlNames.ID, reader.getAttributeValue(null, XmlNames.ID));
                    }
                    text.setLength(0);
                } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                    text.append(reader.getText());
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String name = reader.getLocalName();
                    if (XmlNames.ITEM.equals(name)) {
                        if (fields != null) {
                            loader.consume(result, fields, fields.toString());
                        }
                        fields = null;
                    } else if (fields != null) {
                        fields.put(name, text.toString().trim());
                    }
                    text.setLength(0);
                }
            }
            return result;
        } catch (Exception exception) {
            throw new DataAccessException("Ошибка StAX-чтения " + path, exception);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                    // Поток файла уже закрывается отдельно.
                }
            }
        }
    }

    @Override
    public void write(Path path, List<CoffeeMachine> items) {
        XMLStreamWriter writer = null;
        try {
            Io.ensureParent(path);
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            try (OutputStream output = Files.newOutputStream(path)) {
                writer = factory.createXMLStreamWriter(output, "UTF-8");
                writer.writeStartDocument("UTF-8", "1.0");
                writer.writeCharacters("\n");
                writer.writeStartElement(XmlNames.ROOT);
                if (items != null) {
                    for (CoffeeMachine machine : items) {
                        writeMachine(writer, machine);
                    }
                }
                writer.writeCharacters("\n");
                writer.writeEndElement();
                writer.writeEndDocument();
                writer.flush();
            }
        } catch (Exception exception) {
            throw new DataAccessException("Ошибка StAX-записи " + path, exception);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignored) {
                    // Файл уже записан.
                }
            }
        }
    }

    private static void writeMachine(XMLStreamWriter writer, CoffeeMachine machine) throws Exception {
        Map<String, String> fields = machine.toFieldMap();
        writer.writeCharacters("\n  ");
        writer.writeStartElement(XmlNames.ITEM);
        writer.writeAttribute(XmlNames.ID, fields.get(XmlNames.ID));
        for (String column : CoffeeMachine.COLUMNS) {
            if (XmlNames.ID.equals(column)) {
                continue;
            }
            writer.writeCharacters("\n    ");
            writer.writeStartElement(column);
            writer.writeCharacters(fields.get(column));
            writer.writeEndElement();
        }
        writer.writeCharacters("\n  ");
        writer.writeEndElement();
    }

    private static void disable(XMLInputFactory factory, String property) {
        try {
            factory.setProperty(property, false);
        } catch (IllegalArgumentException ignored) {
            // Реализация StAX может не поддерживать свойство.
        }
    }
}
