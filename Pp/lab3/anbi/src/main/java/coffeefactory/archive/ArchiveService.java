package coffeefactory.archive;

import coffeefactory.common.DataAccessException;
import coffeefactory.common.Io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ArchiveService {
    public void zip(Path archive, List<Path> files) {
        writeZip(archive, files, false);
    }

    public void jar(Path archive, List<Path> files) {
        writeZip(archive, files, true);
    }

    public List<String> listEntries(Path archive) {
        List<String> names = new ArrayList<String>();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                names.add(entry.getName());
                zip.closeEntry();
            }
            return names;
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось прочитать архив " + archive, exception);
        }
    }

    public void unzip(Path archive, Path destination) {
        Path root = destination.toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось создать каталог " + root, exception);
        }
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zip.closeEntry();
                    continue;
                }
                Path target = root.resolve(entry.getName()).normalize();
                if (!target.startsWith(root)) {
                    throw new DataAccessException("Недопустимое имя в архиве: " + entry.getName());
                }
                Io.ensureParent(target);
                Files.copy(zip, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                zip.closeEntry();
            }
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось распаковать " + archive, exception);
        }
    }

    private void writeZip(Path archive, List<Path> files, boolean jar) {
        if (files == null || files.isEmpty()) {
            throw new DataAccessException("Нет файлов для архивации");
        }
        Io.ensureParent(archive);
        try (OutputStream output = Files.newOutputStream(archive)) {
            if (jar) {
                writeJar(output, files);
            } else {
                writePlainZip(output, files);
            }
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось создать архив " + archive, exception);
        }
    }

    private void writePlainZip(OutputStream output, List<Path> files) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            for (Path file : files) {
                zip.putNextEntry(new ZipEntry(file.getFileName().toString()));
                Files.copy(file, zip);
                zip.closeEntry();
            }
        }
    }

    private void writeJar(OutputStream output, List<Path> files) throws IOException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.MAIN_CLASS, "coffeefactory.Main");
        try (JarOutputStream jar = new JarOutputStream(output, manifest)) {
            for (Path file : files) {
                jar.putNextEntry(new JarEntry(file.getFileName().toString()));
                Files.copy(file, jar);
                jar.closeEntry();
            }
        }
    }

    public byte[] readEntry(Path archive, String name) {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (name.equals(entry.getName())) {
                    return readAll(zip);
                }
                zip.closeEntry();
            }
        } catch (IOException exception) {
            throw new DataAccessException("Не удалось прочитать запись архива", exception);
        }
        throw new DataAccessException("В архиве нет записи " + name);
    }

    private static byte[] readAll(InputStream input) throws IOException {
        byte[] buffer = new byte[4096];
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        int read;
        while ((read = input.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
