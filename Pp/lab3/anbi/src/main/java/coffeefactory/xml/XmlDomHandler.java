package coffeefactory.xml;

import coffeefactory.common.DataAccessException;
import coffeefactory.io.AbstractWriter;
import coffeefactory.model.CoffeeMachine;
import coffeefactory.validation.ErrorLogger;
import coffeefactory.validation.RecordLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Чтение и запись XML через DOM.
 */
public class XmlDomHandler extends AbstractWriter<CoffeeMachine> {
    private final RecordLoader loader;

    public XmlDomHandler(ErrorLogger logger) {
        this.loader = new RecordLoader(logger);
    }

    @Override
    public List<CoffeeMachine> read(Path path) {
        List<CoffeeMachine> result = new ArrayList<CoffeeMachine>();
        try (InputStream input = Files.newInputStream(path)) {
            DocumentBuilder builder = secureBuilder();
            Document document = builder.parse(input);
            document.getDocumentElement().normalize();
            NodeList nodes = document.getElementsByTagName(XmlNames.ITEM);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                Map<String, String> fields = new LinkedHashMap<String, String>();
                fields.put(XmlNames.ID, element.getAttribute(XmlNames.ID));
                for (String column : CoffeeMachine.COLUMNS) {
                    if (XmlNames.ID.equals(column)) {
                        continue;
                    }
                    fields.put(column, textOf(element, column));
                }
                loader.consume(result, fields, elementToRaw(element));
            }
            return result;
        } catch (Exception exception) {
            throw new DataAccessException("Ошибка DOM-чтения " + path, exception);
        }
    }

    @Override
    public void write(Path path, List<CoffeeMachine> items) {
        try {
            Document document = secureBuilder().newDocument();
            Element root = document.createElement(XmlNames.ROOT);
            document.appendChild(root);
            if (items != null) {
                for (CoffeeMachine machine : items) {
                    root.appendChild(toElement(document, machine));
                }
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            coffeefactory.common.Io.ensureParent(path);
            try (OutputStream output = Files.newOutputStream(path)) {
                transformer.transform(new DOMSource(document), new StreamResult(output));
            }
        } catch (Exception exception) {
            throw new DataAccessException("Ошибка DOM-записи " + path, exception);
        }
    }

    private static Element toElement(Document document, CoffeeMachine machine) {
        Element element = document.createElement(XmlNames.ITEM);
        Map<String, String> fields = machine.toFieldMap();
        element.setAttribute(XmlNames.ID, fields.get(XmlNames.ID));
        for (String column : CoffeeMachine.COLUMNS) {
            if (XmlNames.ID.equals(column)) {
                continue;
            }
            Element child = document.createElement(column);
            child.appendChild(document.createTextNode(fields.get(column)));
            element.appendChild(child);
        }
        return element;
    }

    private static String textOf(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0 || list.item(0).getTextContent() == null) {
            return "";
        }
        return list.item(0).getTextContent().trim();
    }

    private static String elementToRaw(Element element) {
        StringBuilder builder = new StringBuilder(element.getAttribute(XmlNames.ID));
        for (String column : CoffeeMachine.COLUMNS) {
            if (XmlNames.ID.equals(column)) {
                continue;
            }
            builder.append(';').append(textOf(element, column));
        }
        return builder.toString();
    }

    private static DocumentBuilder secureBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        tryFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        tryFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder();
    }

    private static void tryFeature(DocumentBuilderFactory factory, String name, boolean value) {
        try {
            factory.setFeature(name, value);
        } catch (ParserConfigurationException ignored) {
            // Конкретный JDK-парсер может не знать отдельный флаг.
        }
    }
}
