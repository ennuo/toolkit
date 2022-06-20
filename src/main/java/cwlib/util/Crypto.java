package cwlib.util;

import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import cwlib.types.data.SHA1;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilities relating to computing signatures
 * and encrypting/decrypting data.
 */
public final class Crypto {
    /**
     * Used as a HMAC key for ensuring the legitimacy of profile backups.
     */
    public static final byte[] HASHINATE_KEY = {
        0x2A, (byte) 0xFD, (byte) 0xA3, (byte) 0xCA, (byte) 0x86, 0x02, 0x19, (byte) 0xB3, (byte) 0xE6, (byte) 0x8A, (byte) 0xFF, (byte) 0xCC, (byte) 0x82, (byte) 0xC7, 0x6B, (byte) 0x8A,
        (byte) 0xFE, 0x0A, (byte) 0xD8, 0x13, 0x5F, 0x60, 0x47, 0x5B, (byte) 0xDF, 0x5D, 0x37, (byte) 0xBC, 0x57, 0x1C, (byte) 0xB5, (byte) 0xE7, 
        (byte) 0x96, (byte) 0x75, (byte) 0xD5, 0x28, (byte) 0xA2, (byte) 0xFA, (byte) 0x90, (byte) 0xED, (byte) 0xDF, (byte) 0xA3, 0x45, (byte) 0xB4, 0x1F, (byte) 0xF9, 0x1F, 0x25,
        (byte) 0xE7, 0x42, 0x45, 0x3B, 0x2B, (byte) 0xB5, 0x3E, 0x16, (byte) 0xC9, 0x58, 0x19, 0x7B, (byte) 0xE7, 0x18, (byte) 0xC0, (byte) 0x80
    };

    /**
     * Used for encrypting/decrypting RLocalProfile and profile backups.
     */
    public static final int[] TEA_KEY = { 0x1B70CBD, 0x149607D6, 0x7F94DD5, 0x10DB8CA0 };

    /**
     * Used in XXTEA encryption/decryption.
     */
    public static final int DELTA = 0x9e3779b9;

    /**
     * Computes a SHA1 hash from the buffer.
     * @param b Buffer to hash
     * @return SHA1 instance from resource hash
     */
    public static SHA1 SHA1(byte[] b) {
        if (b == null)
            throw new NullPointerException("Data buffer provided to SHA1 hasher cannot be null!");
        MessageDigest hasher;
        try { hasher = MessageDigest.getInstance("SHA-1"); } 
        catch (NoSuchAlgorithmException e) { return null; }
        return new SHA1(hasher.digest(b));
    }

    /**
     * Encrypts or decrypts a byte array with XXTEA.
     * @param data Data to encrypt/decrypt
     * @param shouldDecrypt Whether this array should be decrypted
     * @return Encrypted/decrypted buffer
     */
    public static byte[] XXTEA(byte[] data, boolean shouldDecrypt) {
        
        // Left pad the data in case it's not divisibly by 4.
        if (data.length % 4 != 0) {
            int padding = 4 - (data.length % 4);
            byte[] paddedData = new byte[padding + data.length];
            System.arraycopy(data, 0, paddedData, padding, data.length);
            data = paddedData;
        }

        int[] v = Bytes.toIntArrayBE(data);


        int n = v.length - 1;
        if (n < 1)
            return data;
        
        int p, q = 6 + 52 / (n + 1);

        if (shouldDecrypt) {
            int z, y = v[0], sum = q * Crypto.DELTA, e;
            while (sum != 0) {
                e = sum >>> 2 & 3;
                for (p = n; p > 0; p--) {
                    z = v[p - 1];
                    y = v[p] -= 
                        ((z >>> 5 ^ y << 2) +  (y >>> 3 ^ z << 4) ^ (sum ^ y) + (Crypto.TEA_KEY[p & 3 ^ e] ^ z));
                }
                z = v[n];
                y = v[0] -= ((z >>> 5 ^ y << 2) +  (y >>> 3 ^ z << 4) ^ (sum ^ y) + (Crypto.TEA_KEY[p & 3 ^ e] ^ z));
                sum = sum - Crypto.DELTA;
            }
        } else {
            int z = v[n], y, sum = 0, e;
            while (q-- > 0) {
                sum = sum + Crypto.DELTA;
                e = sum >>> 2 & 3;
                for (p = 0; p < n; p++) {
                    y = v[p + 1];
                    z = v[p] += ((z >>> 5 ^ y << 2) +  (y >>> 3 ^ z << 4) ^ (sum ^ y) + (Crypto.TEA_KEY[p & 3 ^ e] ^ z));
                }
                y = v[0];
                z = v[n] += ((z >>> 5 ^ y << 2) +  (y >>> 3 ^ z << 4) ^ (sum ^ y) + (Crypto.TEA_KEY[p & 3 ^ e] ^ z));
            }
        }

        return Bytes.fromIntArrayBE(v);
    }
    
    /**
     * Generates a secret key from key and IV.
     * @param key Secret key string
     * @param IV IV buffers
     * @return Secret key
     */
    public static SecretKeySpec generateKey(String key, byte[] IV) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), IV, 65536, 256);
            SecretKey temp = factory.generateSecret(spec);
            return new SecretKeySpec(temp.getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }
    
    /**
     * Decrypt AES-CBC encrypted data.
     * @param key AES key
     * @param data Data to decrypt
     * @return Decrypted data
     */
    public static byte[] decrypt(String key, byte[] data) {
        try {
            MemoryInputStream input = new MemoryInputStream(data);
            byte[] IV = input.bytes(16);
            IvParameterSpec IVSpec = new IvParameterSpec(IV);
            byte[] buffer = input.bytes(input.getLength() - 16);
            SecretKeySpec spec = Crypto.generateKey(key, IV);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, spec, IVSpec);
            return cipher.doFinal(buffer);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Encrypt data with AES-CBC.
     * @param key AES key
     * @param data Data to encrypt
     * @return Encrypted data
     */
    public static byte[] encrypt(String key, byte[] data) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] IV = new byte[16];
            random.nextBytes(IV);
            SecretKeySpec spec = Crypto.generateKey(key, IV);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

            cipher.init(Cipher.ENCRYPT_MODE, spec, new IvParameterSpec(IV));

            byte[] buffer = cipher.doFinal(data);

            MemoryOutputStream output = new MemoryOutputStream(buffer.length + 16);
            output.bytes(IV);
            output.bytes(buffer);
            return output.getBuffer();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    /**
     * Computes an HMAC-SHA1 of a given buffer.
     * @param data Data to hash
     * @param key Secret key
     * @return Resulting hash
     */
    public static SHA1 HMAC(byte[] data, byte[] key) {
        SecretKey secretKey = new SecretKeySpec(key, "HmacSHA1");
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            return new SHA1(mac.doFinal(data));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalArgumentException("Secret key was invalid!");    
        }
    }
}
