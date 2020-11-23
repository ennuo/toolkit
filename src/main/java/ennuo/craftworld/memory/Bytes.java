package ennuo.craftworld.memory;

import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.Mod;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.types.BigProfile;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.utilities.KMPMatchUtilities;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Bytes {
    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            hex[i * 2] = HEX_ARRAY[b >>> 4];
            hex[i * 2 + 1] = HEX_ARRAY[b & 0xF];
        }
        return new String(hex);
    }

    public static String toHex(int value) {
        return toHex(toBytes(value));
    }

    public static String toHex(long value) {
        return toHex(toBytes(value));
    }

    public static short toShort(byte[] bytes) {
        return (short)((bytes[0] & 0xFF) << 8 | (bytes[1] & 0xFF) << 0);
    }

    public static int toInteger(byte[] bytes) {
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF) << 0;
    }

    public static int toInteger(String value) {
        return toInteger(toBytes(Strings.leftPad(value, 8)));
    }

    public static final byte[] toBytes(int value) {
        return new byte[] {
            (byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >> 8), (byte) value
        };
    }

    public static final byte[] toBytes(long value) {
        return new byte[] {
            (byte)(int)(value >>> 24L), (byte)(int)(value >>> 16L), (byte)(int)(value >> 8L), (byte)(int) value
        };
    }

    public static final byte[] toBytes(short value) {
        return new byte[] {
            (byte)(value >>> 8), (byte) value
        };
    }

    public static byte[] toBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] =
            (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        return data;
    }

    public static byte[] createResourceReference(ResourcePtr res, int revision) {
        Output output = new Output(0x1C + 0x4, revision);
        output.resource(res, true);
        output.shrinkToFit();
        return output.buffer;
    }

    public static byte[] reverseShort(byte[] bytes) {
        byte[] buffer = new byte[2];
        buffer[0] = bytes[1];
        buffer[1] = bytes[0];
        return buffer;
    }

    public static byte[] reverseInteger(byte[] bytes) {
        byte[] buffer = new byte[4];
        buffer[0] = bytes[3];
        buffer[1] = bytes[2];
        buffer[2] = bytes[1];
        buffer[3] = bytes[0];
        return buffer;
    }

    public static byte[] encode(long value) {
        byte[] temp = new byte[5];
        int size = 0;
        while (value > 127L) {
            temp[size] = (byte)(value & 0x7FL | 0x80L);
            value >>= 7L;
            size++;
        }
        temp[size++] = (byte)(value & 0x7FL);
        return Arrays.copyOfRange(temp, 0, size);
    }

    public static int decode(byte[] bytes) {
        int result = 0, shift = 0, i = 0;
        while (true) {
            int b = bytes[i];
            result |= (b & 0x7F) << shift;
            shift += 7;
            if ((b & 0x80) == 0)
                break;
            i++;
        }
        return result;
    }

    public static byte[] SHA1(byte[] bytes) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, (String) null, ex);
            return null;
        }
        return digest.digest(bytes);
    }

    public static byte[][] Split(byte[] data, int size) {
        byte[][] out = new byte[(int) Math.ceil(data.length / (double) size)][size];
        int start = 0;
        for (int i = 0; i < out.length; ++i) {
            int end = Math.min(data.length, start + size);
            out[i] = Arrays.copyOfRange(data, start, end);
            start += size;
        }
        return out;
    }

    public static byte[] Combine(byte[]...arrays) {
        int totalLength = 0;
        for (int i = 0; i < arrays.length; i++)
            totalLength += arrays[i].length;
        byte[] result = new byte[totalLength];

        int currentIndex = 0;
        for (int i = 0; i < arrays.length; i++) {
            System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
            currentIndex += arrays[i].length;
        }

        return result;
    }

    public static SecretKeySpec GenerateKey(String key, byte[] IV) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), IV, 65536, 256);
            SecretKey temp = factory.generateSecret(spec);
            return new SecretKeySpec(temp.getEncoded(), "AES");
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public static byte[] Decrypt(String key, byte[] data) {
        try {
            Data input = new Data(data);
            byte[] IV = input.bytes(16);
            IvParameterSpec IVSpec = new IvParameterSpec(IV);
            byte[] buffer = input.bytes(input.length - 16);
            SecretKeySpec spec = GenerateKey(key, IV);
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

    public static byte[] Encrypt(String key, byte[] data) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] IV = new byte[16];
            random.nextBytes(IV);
            SecretKeySpec spec = GenerateKey(key, IV);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

            cipher.init(Cipher.ENCRYPT_MODE, spec, new IvParameterSpec(IV));

            byte[] buffer = cipher.doFinal(data);

            Output output = new Output(buffer.length + 16);
            output.bytes(IV);
            output.bytes(buffer);
            return output.buffer;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Bytes.class.getName()).log(Level.SEVERE, null, ex);
        }


        return null;
    }

    public static int occurences(byte[] data, byte[] pattern) {
        int offset = 0, occurrences = 0;
        while (offset != -1) {
            offset = KMPMatchUtilities.indexOf(data, pattern, offset);
            if (offset != -1) occurrences++;
        }
        return occurrences;
    }

    public static void ReplaceAll(Data data, byte[] original, byte[] replacement) {
        if (Arrays.equals(original, replacement)) return;

        data.seek(0);

        int offset = KMPMatchUtilities.indexOf(data.data, original);
        int found = 0;
        while (offset != -1) {
            found++;

            byte[] left = data.bytes(offset);
            data.offset += original.length;
            byte[] right = data.bytes(data.length - data.offset);

            data.setData(Bytes.Combine(
                left,
                replacement,
                right

            ));

            offset = KMPMatchUtilities.indexOf(data.data, original);
        }
    }

    public static byte[] hashinateStreamingChunk(Mod mod, Resource resource, FileEntry entry) {
        byte[] left = resource.bytes(0x36);
        int planSize = resource.int32f();
        Resource plan = new Resource(resource.bytes(planSize));
        plan.revision = 0x270;
        byte[] right = resource.bytes(resource.length - resource.offset);
        plan.getDependencies(entry);
        plan.isStreamingChunk = true;
        if (plan.resources != null) {
            for (int i = 0; i < plan.resources.length; ++i) {
                ResourcePtr res = plan.resources[i];
                plan.seek(0x12);
                if (res == null) continue;
                if (plan.dependencies[i] == null)
                    continue;
                if (res.type == RType.SCRIPT) continue;
                byte[] data;
                if (res.hash != null && res.GUID == -1) data = Globals.extractFile(res.hash);
                else data = Globals.extractFile(res.GUID);
                if (data == null) continue;
                Resource dependency = new Resource(data);
                plan.replaceDependency(i, new ResourcePtr(hashinate(mod, dependency, plan.dependencies[i]), res.type), false);
            }
        }

        byte[] result = Bytes.Combine(left, Bytes.toBytes(plan.data.length), plan.data, right);

        byte[] tableOffset = Bytes.toBytes(result.length - 4);
        for (int i = 0; i < 4; ++i)
            result[0x8 + i] = tableOffset[i];

        plan.seek(0x17);
        tableOffset = Bytes.toBytes(0x1b + plan.int32f());
        for (int i = 0; i < 4; ++i)
            result[0x42 + i] = tableOffset[i];



        byte[] SHA1 = Bytes.SHA1(result);
        mod.add("streaming/" + Bytes.toHex(SHA1).toLowerCase(), result);
        return SHA1;
    }


    public static byte[] hashinate(Mod mod, Resource resource, FileEntry entry) {
        boolean isBin = entry.path.toLowerCase().contains(".bin");
        if (resource.dependencies == null || resource.resources == null)
            resource.getDependencies(entry);
        if (resource.resources != null && resource.resources.length != 0) {
            resource.decompress(true);
            for (int i = 0; i < resource.resources.length; ++i) {
                ResourcePtr res = resource.resources[i];
                if (res == null) continue;
                if (res.type == RType.SCRIPT) continue;
                if (res.type == RType.PLAN && res.GUID != -1) resource.removePlanDescriptors(res.GUID, false);
                if (res.type == RType.STREAMING_CHUNK) {
                    if (res.GUID == -1) continue;
                    String name = new File(resource.dependencies[i].path).getName();
                    File file = Toolkit.instance.fileChooser.openFile(name, ".farc", "Streaming Chunk", false);
                    if (file == null) continue;
                    byte[] data = FileIO.read(file.getAbsolutePath());
                    BigProfile profile = new BigProfile(new Data(data), true);
                    for (FileEntry e: profile.entries) {
                        int index = -1;
                        for (int j = 0; j < resource.resources.length; ++j) {
                            if (Arrays.equals(resource.resources[j].hash, e.hash)) {
                                index = j;
                                break;
                            }
                        }
                        if (index != -1)
                            resource.replaceDependency(index, new ResourcePtr(hashinateStreamingChunk(mod, new Resource(e.data), e), RType.STREAMING_CHUNK), false);
                    }
                    resource.replaceDependency(i, new ResourcePtr(null, RType.STREAMING_CHUNK), false);
                    continue;
                }
                byte[] data;
                if (res.hash != null) data = Globals.extractFile(res.hash);
                else data = Globals.extractFile(res.GUID);
                if (data == null) continue;
                Resource dependency = new Resource(data);
                resource.replaceDependency(i, new ResourcePtr(hashinate(mod, dependency, resource.dependencies[i]), res.type), false);
            }
            resource.removePlanDescriptors(entry.GUID, false);
            resource.setData(Compressor.Compress(resource.data, resource.magic, resource.revision, resource.resources));
        }
        mod.add(entry.path, resource.data, entry.GUID);
        return Bytes.SHA1(resource.data);
    }

}