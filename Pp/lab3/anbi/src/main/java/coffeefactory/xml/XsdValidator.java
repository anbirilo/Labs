package coffeefactory.xml;

import coffeefactory.common.DataAccessException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;

/**
 * Проверка XML-документа по XSD-схеме.
 */
public class XsdValidator {
    public List<String> validate(Path xml, Path xsd) {
        List<String> errors = new ArrayList<String>();
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsd.toFile());
            Validator validator = schema.newValidator();
            CollectingErrorHandler handler = new CollectingErrorHandler();
            validator.setErrorHandler(handler);
            validator.validate(new StreamSource(xml.toFile()));
            errors.addAll(handler.getErrors());
        } catch (SAXException exception) {
            if (errors.isEmpty()) {
                errors.add(exception.getMessage());
            }
        } catch (Exception exception) {
            throw new DataAccessException("Не удалось проверить XML по XSD", exception);
        }
        return errors;
    }
}
