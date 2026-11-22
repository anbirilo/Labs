package coffeefactory.xml;

import coffeefactory.common.DataAccessException;
import coffeefactory.common.Io;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Проверка XML-документа по DTD.
 */
public class DtdValidator {
    public List<String> validate(Path xml, Path dtd) {
        List<String> errors = new ArrayList<String>();
        Path wrapped = null;
        try {
            wrapped = Files.createTempFile("coffee-dtd-", ".xml");
            Io.writeString(wrapped, withDoctype(xml, dtd));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            CollectingErrorHandler handler = new CollectingErrorHandler();
            builder.setErrorHandler(handler);
            final Path dtdPath = dtd;
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    InputSource source = new InputSource(Files.newInputStream(dtdPath));
                    source.setSystemId(dtdPath.toUri().toString());
                    return source;
                }
            });
            builder.parse(wrapped.toFile());
            errors.addAll(handler.getErrors());
        } catch (SAXException exception) {
            if (errors.isEmpty()) {
                errors.add(exception.getMessage());
            }
        } catch (Exception exception) {
            throw new DataAccessException("Не удалось проверить XML по DTD", exception);
        } finally {
            if (wrapped != null) {
                try {
                    Files.deleteIfExists(wrapped);
                } catch (IOException ignored) {
                    // Временный файл не мешает результату проверки.
                }
            }
        }
        return errors;
    }

    private static String withDoctype(Path xml, Path dtd) {
        String original = new String(Io.readBytes(xml), StandardCharsets.UTF_8);
        if (original.startsWith("\uFEFF")) {
            original = original.substring(1);
        }
        String body = original;
        if (body.startsWith("<?xml")) {
            int end = body.indexOf("?>");
            if (end >= 0) {
                body = body.substring(end + 2);
            }
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE " + XmlNames.ROOT + " SYSTEM \"" + dtd.toUri().toASCIIString() + "\">\n"
                + body;
    }
}
