package coffeefactory.app;

import java.nio.file.Path;

public final class DataPaths {
    private final Path root;

    public DataPaths(Path root) {
        this.root = root;
    }

    public Path root() {
        return root;
    }

    public Path text() {
        return root.resolve("coffeemachines.txt");
    }

    public Path xml() {
        return root.resolve("coffeemachines.xml");
    }

    public Path json() {
        return root.resolve("coffeemachines.json");
    }

    public Path xsd() {
        return root.resolve("schema").resolve("coffeemachine.xsd");
    }

    public Path dtd() {
        return root.resolve("schema").resolve("coffeemachine.dtd");
    }

    public Path log() {
        return root.resolve("errors.log");
    }

    public Path password() {
        return root.resolve("password.md5");
    }

    public Path encrypted() {
        return root.resolve("coffeemachines.enc");
    }

    public Path restored() {
        return root.resolve("coffeemachines.restored.txt");
    }

    public Path zip() {
        return root.resolve("coffeemachines.zip");
    }

    public Path jar() {
        return root.resolve("coffeemachines.jar");
    }
}
