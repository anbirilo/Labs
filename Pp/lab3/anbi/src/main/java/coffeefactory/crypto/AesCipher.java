package coffeefactory.crypto;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import coffeefactory.common.DataAccessException;
import coffeefactory.common.Io;

public class AesCipher {
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private final SecureRandom random = new SecureRandom();

    public byte[] encrypt(byte[] plain, String key) {
        try {
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(key), new IvParameterSpec(iv));
            byte[] body = cipher.doFinal(plain == null ? new byte[0] : plain);
            byte[] payload = new byte[iv.length + body.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(body, 0, payload, iv.length, body.length);
            return payload;
        } catch (GeneralSecurityException exception) {
            throw new DataAccessException("Не удалось зашифровать данные", exception);
        }
    }

    public byte[] decrypt(byte[] payload, String key) {
        if (payload == null || payload.length <= 16) {
            throw new DataAccessException("Повреждённые зашифрованные данные");
        }
        try {
            byte[] iv = Arrays.copyOfRange(payload, 0, 16);
            byte[] body = Arrays.copyOfRange(payload, 16, payload.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(key), new IvParameterSpec(iv));
            return cipher.doFinal(body);
        } catch (GeneralSecurityException exception) {
            throw new DataAccessException("Не удалось расшифровать данные. Проверьте ключ.", exception);
        }
    }

    public String encryptToBase64(String text, String key) {
        byte[] plain = text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(encrypt(plain, key));
    }

    public String decryptFromBase64(String encoded, String key) {
        return new String(decrypt(Base64.getDecoder().decode(encoded), key), StandardCharsets.UTF_8);
    }

    public void encryptFile(Path source, Path target, String key) {
        Io.writeBytes(target, encrypt(Io.readBytes(source), key));
    }

    public void decryptFile(Path source, Path target, String key) {
        Io.writeBytes(target, decrypt(Io.readBytes(source), key));
    }

    private SecretKeySpec secretKey(String key) throws GeneralSecurityException {
        if (key == null || key.isEmpty()) {
            throw new DataAccessException("Пустой ключ шифрования");
        }
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(Arrays.copyOf(digest, 16), "AES");
    }
}
