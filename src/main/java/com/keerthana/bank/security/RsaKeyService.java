
        package com.keerthana.bank.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@Service
public class RsaKeyService {

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);

            return generator.generateKeyPair();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "RSA key generation failed", e);
        }
    }

    public String getPublicKey(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(
                keyPair.getPublic().getEncoded());
    }

    public String getPrivateKey(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(
                keyPair.getPrivate().getEncoded());
    }

    public String encrypt(String message, KeyPair keyPair) {
        try {
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException(
                        "Please enter a message to encrypt.");
            }

            Cipher cipher = Cipher.getInstance(
                    "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    keyPair.getPublic());

            byte[] encrypted = cipher.doFinal(
                    message.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Encryption failed. Please enter a shorter message.",
                    e);
        }
    }

    public String decrypt(String encryptedMessage, KeyPair keyPair) {
        try {
            if (encryptedMessage == null ||
                    encryptedMessage.isBlank()) {
                throw new IllegalArgumentException(
                        "Please enter an encrypted message.");
            }

            byte[] decoded = Base64.getDecoder().decode(
                    encryptedMessage.trim());

            Cipher cipher = Cipher.getInstance(
                    "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keyPair.getPrivate());

            byte[] decrypted = cipher.doFinal(decoded);

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Decryption failed. Check the encrypted message and key.",
                    e);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Decryption failed. The message may belong to a different key pair.",
                    e);
        }
    }
}
