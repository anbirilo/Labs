package coffeefactory.crypto;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import coffeefactory.common.DataAccessException;
import coffeefactory.common.Io;


public class Md5Hasher {
    public String hash(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Нечего хешировать");
        }
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte piece : digest) {
                builder.append(String.format("%02x", piece & 0xff));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("MD5 недоступен", exception);
        }
    }

    public boolean matches(String value, String expectedHash) {
        if (expectedHash == null) {
            return false;
        }
        return hash(value).equalsIgnoreCase(expectedHash.trim());
    }

    public void saveHash(Path file, String password) {
        Io.writeString(file, hash(password));
    }

    public boolean matchesFile(Path file, String password) {
        try {
            return matches(password, Io.readString(file));
        } catch (DataAccessException exception) {
            throw new DataAccessException("Не удалось прочитать хеш пароля", exception);
        }
    }
}
