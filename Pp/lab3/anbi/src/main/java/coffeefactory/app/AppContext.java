package coffeefactory.app;

import java.nio.file.Path;

import coffeefactory.archive.ArchiveService;
import coffeefactory.assembly.AssemblyFactory;
import coffeefactory.catalog.MachineCatalog;
import coffeefactory.crypto.AesCipher;
import coffeefactory.crypto.Md5Hasher;
import coffeefactory.io.TextFileHandler;
import coffeefactory.json.JsonHandler;
import coffeefactory.storage.ListStorage;
import coffeefactory.storage.MapStorage;
import coffeefactory.validation.ErrorLogger;
import coffeefactory.validation.InputValidator;
import coffeefactory.xml.DtdValidator;
import coffeefactory.xml.XmlDomHandler;
import coffeefactory.xml.XmlSaxHandler;
import coffeefactory.xml.XmlStaxHandler;
import coffeefactory.xml.XsdValidator;
public final class AppContext {
    private final DataPaths paths;
    private final ErrorLogger logger;
    private final InputValidator validator;
    private final MachineCatalog catalog;
    private final XmlDomHandler domHandler;
    private final XmlSaxHandler saxHandler;
    private final XmlStaxHandler staxHandler;
    private final JsonHandler jsonHandler;
    private final XsdValidator xsdValidator;
    private final DtdValidator dtdValidator;
    private final Md5Hasher hasher;
    private final AesCipher cipher;
    private final ArchiveService archiveService;
    private final AssemblyFactory assemblyFactory;

    private AppContext(DataPaths paths, ErrorLogger logger, InputValidator validator, MachineCatalog catalog,
                       XmlDomHandler domHandler, XmlSaxHandler saxHandler, XmlStaxHandler staxHandler,
                       JsonHandler jsonHandler, XsdValidator xsdValidator, DtdValidator dtdValidator,
                       Md5Hasher hasher, AesCipher cipher, ArchiveService archiveService,
                       AssemblyFactory assemblyFactory) {
        this.paths = paths;
        this.logger = logger;
        this.validator = validator;
        this.catalog = catalog;
        this.domHandler = domHandler;
        this.saxHandler = saxHandler;
        this.staxHandler = staxHandler;
        this.jsonHandler = jsonHandler;
        this.xsdValidator = xsdValidator;
        this.dtdValidator = dtdValidator;
        this.hasher = hasher;
        this.cipher = cipher;
        this.archiveService = archiveService;
        this.assemblyFactory = assemblyFactory;
    }

    public static AppContext create(Path root, boolean echoErrors) {
        DataPaths paths = new DataPaths(root);
        ErrorLogger logger = new ErrorLogger(paths.log(), echoErrors);
        InputValidator validator = new InputValidator();
        TextFileHandler textFileHandler = new TextFileHandler(logger);
        MachineCatalog catalog = new MachineCatalog(
                new ListStorage(), new MapStorage(), textFileHandler, logger, paths.text());
        return new AppContext(
                paths,
                logger,
                validator,
                catalog,
                new XmlDomHandler(logger),
                new XmlSaxHandler(logger),
                new XmlStaxHandler(logger),
                new JsonHandler(logger),
                new XsdValidator(),
                new DtdValidator(),
                new Md5Hasher(),
                new AesCipher(),
                new ArchiveService(),
                new AssemblyFactory()
        );
    }

    public DataPaths getPaths() {
        return paths;
    }

    public ErrorLogger getLogger() {
        return logger;
    }

    public InputValidator getValidator() {
        return validator;
    }

    public MachineCatalog getCatalog() {
        return catalog;
    }

    public XmlDomHandler getDomHandler() {
        return domHandler;
    }

    public XmlSaxHandler getSaxHandler() {
        return saxHandler;
    }

    public XmlStaxHandler getStaxHandler() {
        return staxHandler;
    }

    public JsonHandler getJsonHandler() {
        return jsonHandler;
    }

    public XsdValidator getXsdValidator() {
        return xsdValidator;
    }

    public DtdValidator getDtdValidator() {
        return dtdValidator;
    }

    public Md5Hasher getHasher() {
        return hasher;
    }

    public AesCipher getCipher() {
        return cipher;
    }

    public ArchiveService getArchiveService() {
        return archiveService;
    }

    public AssemblyFactory getAssemblyFactory() {
        return assemblyFactory;
    }
}
