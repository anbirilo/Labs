package coffeefactory;

import coffeefactory.Application;
import coffeefactory.app.AppContext;
import coffeefactory.archive.ArchiveService;
import coffeefactory.common.DataAccessException;
import coffeefactory.common.Io;
import coffeefactory.crypto.AesCipher;
import coffeefactory.crypto.Md5Hasher;
import coffeefactory.menu.ConsoleMenu;
import coffeefactory.model.MachineType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoArchiveMenuTest {
    @TempDir
    Path temp;

    @Test
    void md5MatchesKnownVectorsAndStoredFile() {
        Md5Hasher hasher = new Md5Hasher();
        assertEquals("900150983cd24fb0d6963f7d28e17f72", hasher.hash("abc"));
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", hasher.hash(""));
        assertEquals(hasher.hash("кофе"), hasher.hash("кофе"));
        assertFalse(hasher.hash("кофе").equals(hasher.hash("coffee")));
        Path file = temp.resolve("password.md5");
        hasher.saveHash(file, "factory");
        assertTrue(hasher.matchesFile(file, "factory"));
        assertFalse(hasher.matchesFile(file, "other"));
        assertTrue(hasher.matches("abc", "900150983CD24FB0D6963F7D28E17F72"));
    }

    @Test
    void aesEncryptsAndDecryptsTextAndFileWithKey() {
        AesCipher cipher = new AesCipher();
        String secret = "Кофеварка №1 — линия эспрессо";
        String encoded = cipher.encryptToBase64(secret, "line-key");
        assertEquals(secret, cipher.decryptFromBase64(encoded, "line-key"));
        assertFalse(encoded.equals(cipher.encryptToBase64(secret, "line-key")));
        assertThrows(DataAccessException.class, () -> cipher.decryptFromBase64(encoded, "wrong-key"));
        assertThrows(DataAccessException.class, () -> cipher.encrypt(new byte[]{1}, ""));

        Path source = temp.resolve("plain.txt");
        Path encodedFile = temp.resolve("plain.enc");
        Path restored = temp.resolve("plain.restored.txt");
        byte[] payload = "id;type;model\n1;ESPRESSO;Dedica\n".getBytes(StandardCharsets.UTF_8);
        Io.writeBytes(source, payload);
        cipher.encryptFile(source, encodedFile, "line-key");
        cipher.decryptFile(encodedFile, restored, "line-key");
        assertArrayEquals(payload, Io.readBytes(restored));
        assertFalse(Arrays.equals(payload, Io.readBytes(encodedFile)));
    }

    @Test
    void zipAndJarContainSourceBytes() throws Exception {
        Path first = temp.resolve("coffeemachines.txt");
        Path second = temp.resolve("coffeemachines.json");
        Io.writeString(first, "txt-body");
        Io.writeString(second, "{\"ok\":true}");
        ArchiveService archive = new ArchiveService();
        Path zip = temp.resolve("catalog.zip");
        Path jar = temp.resolve("catalog.jar");
        archive.zip(zip, Arrays.asList(first, second));
        archive.jar(jar, Arrays.asList(first));

        assertTrue(archive.listEntries(zip).contains("coffeemachines.txt"));
        assertTrue(archive.listEntries(zip).contains("coffeemachines.json"));
        assertArrayEquals(Io.readBytes(first), archive.readEntry(zip, "coffeemachines.txt"));

        Path unpacked = temp.resolve("unpacked");
        archive.unzip(zip, unpacked);
        assertEquals("txt-body", Io.readString(unpacked.resolve("coffeemachines.txt")));
        assertEquals("{\"ok\":true}", Io.readString(unpacked.resolve("coffeemachines.json")));

        assertTrue(archive.listEntries(jar).contains("META-INF/MANIFEST.MF"));
        assertTrue(archive.listEntries(jar).contains("coffeemachines.txt"));
        String manifest = new String(archive.readEntry(jar, "META-INF/MANIFEST.MF"), StandardCharsets.UTF_8);
        assertTrue(manifest.contains("Main-Class: coffeefactory.Main"));
        assertArrayEquals(Io.readBytes(first), archive.readEntry(jar, "coffeemachines.txt"));
        assertThrows(DataAccessException.class, () -> archive.zip(temp.resolve("empty.zip"), Arrays.asList()));
    }

    @Test
    void menuOffersRequiredActionsAndAssemblesMachineFromConsole() {
        AppContext context = AppContext.create(temp, false);
        ConsoleMenu menu = new ConsoleMenu(context, scanner("0\n"));
        String text = menu.menuText();
        assertTrue(text.contains("List"));
        assertTrue(text.contains("Map"));
        assertTrue(text.contains("DOM"));
        assertTrue(text.contains("SAX"));
        assertTrue(text.contains("StAX"));
        assertTrue(text.contains("XSD"));
        assertTrue(text.contains("DTD"));
        assertTrue(text.contains("JSON"));
        assertTrue(text.contains("MD5"));
        assertTrue(text.contains("AES"));
        assertTrue(text.contains("ZIP"));
        assertTrue(text.contains("JAR"));

        Application application = new Application(temp);
        application.start(scanner(String.join("\n",
                "3",
                "100",
                "ESPRESSO",
                "Test Model",
                "1300",
                "15",
                "1000",
                "1",
                "01.01.2024",
                "19990",
                "Garlyn",
                "black",
                "24",
                "0"
        ) + "\n"));
        assertEquals(1, application.getCatalog().size());
        assertEquals("Test Model", application.getCatalog().find(100).getModel());
        assertEquals(MachineType.ESPRESSO, application.getCatalog().find(100).getType());
        assertEquals(15, application.getCatalog().find(100).getPressureBar(), 0.001);
        assertTrue(Io.readString(temp.resolve("coffeemachines.txt")).contains("Test Model"));
        assertTrue(application.getCatalog().getMapStorage().asSortedMap().containsKey(100L));
    }

    @Test
    void mainOnlyCreatesApplicationAndStartsIt() {
        String main = Io.readString(Paths.get("src", "main", "java", "coffeefactory", "Main.java"));
        String body = main.substring(main.indexOf("public static void main"));
        assertTrue(body.contains("new Application()"));
        assertTrue(body.contains("application.start()"));
        assertFalse(body.contains("if"));
        assertFalse(body.contains("for"));
        assertFalse(body.contains("while"));
        String application = Io.readString(Paths.get("src", "main", "java", "coffeefactory", "Application.java"));
        assertTrue(application.contains("ConsoleMenu createMenu"));
    }

    @Test
    void unknownMenuItemDoesNotCorruptCatalog() {
        Application application = new Application(temp);
        application.start(scanner("99\n0\n"));
        assertEquals(0, application.getCatalog().size());
        assertTrue(Files.exists(temp.resolve("errors.log")));
    }

    private static Scanner scanner(String script) {
        return new Scanner(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8.name());
    }
}
