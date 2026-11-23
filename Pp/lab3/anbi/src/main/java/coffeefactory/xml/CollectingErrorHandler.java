package coffeefactory.xml;

import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class CollectingErrorHandler extends DefaultHandler {
    private final List<String> errors = new ArrayList<String>();

    @Override
    public void warning(SAXParseException exception) {
        errors.add(format(exception));
    }

    @Override
    public void error(SAXParseException exception) {
        errors.add(format(exception));
    }

    @Override
    public void fatalError(SAXParseException exception) {
        errors.add(format(exception));
    }

    public List<String> getErrors() {
        return errors;
    }

    private static String format(SAXParseException exception) {
        return "строка " + exception.getLineNumber() + ": " + exception.getMessage();
    }
}
