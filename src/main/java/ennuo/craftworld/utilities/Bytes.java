package ennuo.craftworld.utilities;

import ennuo.craftworld.registry.MaterialRegistry;
import ennuo.craftworld.registry.MaterialRegistry.MaterialEntry;
import ennuo.craftworld.resources.GfxMaterial;
import ennuo.craftworld.resources.Plan;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SerializationMethod;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.types.data.GfxMaterialInfo;
import ennuo.craftworld.types.mods.Mod;
import ennuo.toolkit.utilities.Globals;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.joml.Vector3f;

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
    
    public static int toIntegerLE(byte[] bytes) {
        return (bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF) << 0;
    }

    public static int toInteger(byte[] bytes) {
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF) << 0;
    }

    public static int toInteger(String value) {
        return toInteger(toBytes(StringUtils.leftPad(value, 8)));
    }

    public static final byte[] toBytesLE(int value) {
        return new byte[] {
            (byte)(value), (byte)(value >>> 8L), (byte)(value >> 16L), (byte)(value >> 24L)
        };
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
    
    public static final byte[] toBytesLE(long value) {
        return new byte[] {
            (byte)(int)(value), (byte)(int)(value >>> 8L), (byte)(int)(value >> 16L), (byte)(int)(value >> 24L)
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
    
    public static final void swap32(byte[] data, int offset) {
        byte temp = data[offset];
        data[offset] = data[offset + 3];
        data[offset + 3] = temp;
        temp = data[offset + 1];
        data[offset + 1] = data[offset + 2];
        data[offset + 2] = temp;
    }

    public static byte[] createResourceReference(ResourceDescriptor res, Revision revision, byte compressionFlags) {
        Output output = new Output(0x1C + 0x4, revision);
        output.compressionFlags = compressionFlags;
        output.resource(res);
        output.shrink();
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

    /*
    public static byte[] hashinateStreamingChunk(Mod mod, Resource resource, FileEntry entry) {
        byte[] left = resource.bytes(0x36);
        int planSize = resource.i32f();
        Resource plan = new Resource(resource.bytes(planSize));
        plan.revision = 0x270;
        byte[] right = resource.bytes(resource.length - resource.offset);
        plan.getDependencies(entry);
        plan.isStreamingChunk = true;
        if (plan.resources != null) {
            for (int i = 0; i < plan.resources.length; ++i) {
                ResourceDescriptor res = plan.resources[i];
                plan.seek(0x12);
                if (res == null) continue;
                if (plan.dependencies[i] == null)
                    continue;
                if (res.type == ResourceType.SCRIPT) continue;
                byte[] data;
                if (res.hash != null && res.GUID == -1) data = Globals.extractFile(res.hash);
                else data = Globals.extractFile(res.GUID);
                if (data == null) continue;
                Resource dependency = new Resource(data);
                plan.replaceDependency(i, new ResourceDescriptor(hashinate(mod, dependency, plan.dependencies[i]), res.type));
            }
        }

        byte[] result = Bytes.Combine(left, Bytes.toBytes(plan.data.length), plan.data, right);

        byte[] tableOffset = Bytes.toBytes(result.length - 4);
        for (int i = 0; i < 4; ++i)
            result[0x8 + i] = tableOffset[i];

        plan.seek(0x17);
        tableOffset = Bytes.toBytes(0x1b + plan.i32f());
        for (int i = 0; i < 4; ++i)
            result[0x42 + i] = tableOffset[i];



        byte[] SHA1 = Bytes.SHA1(result);
        mod.add("streaming/" + Bytes.toHex(SHA1).toLowerCase(), result);
        return SHA1;
    }
    */
    
    public static void recurse(Mod mod, Resource resource, FileEntry entry) {
        for (int i = 0; i < resource.dependencies.length; ++i) {
            ResourceDescriptor res = resource.dependencies[i];
            if (res == null || res.type == ResourceType.SCRIPT) continue;
            byte[] data = Globals.extractFile(res);
            if (data == null) continue;
            Resource dependency = new Resource(data);
            if (dependency.method != SerializationMethod.BINARY)
                mod.add(entry.path, data, entry.GUID);
            else recurse(mod, new Resource(data), Globals.findEntry(res));
        }
        if (resource.method == SerializationMethod.BINARY)
            mod.add(entry.path, resource.compressToResource(), entry.GUID);
    }

    public static SHA1 hashinate(Mod mod, Resource resource, FileEntry entry) {
        return Bytes.hashinate(mod, resource, entry, null);
    }
    
    public static SHA1 hashinate(Mod mod, Resource resource, FileEntry entry, HashMap<Integer, MaterialEntry> registry) {
        if (resource.method == SerializationMethod.BINARY) {
            if (registry == null || (registry != null && resource.type != ResourceType.GFX_MATERIAL)) {
                for (int i = 0; i < resource.dependencies.length; ++i) {
                    ResourceDescriptor res = resource.dependencies[i];
                    FileEntry dependencyEntry = Globals.findEntry(res);
                    if (res == null) continue;
                    if (res.type == ResourceType.SCRIPT) continue;
                    /*
                    if (res.type == ResourceType.STREAMING_CHUNK) {
                        if (res.GUID == -1) continue;
                        String name = new File(dependencyEntry.path).getName();
                        File file = Toolkit.instance.fileChooser.openFile(name, ".farc", "Streaming Chunk", false);
                        if (file == null) continue;
                        byte[] data = FileIO.read(file.getAbsolutePath());
                        BigProfile profile = new BigProfile(new Data(data), true);
                        for (FileEntry e: profile.entries) {
                            int index = -1;
                            for (int j = 0; j < resource.dependencies.length; ++j) {
                                if (Arrays.equals(resource.dependencies[j].hash, e.SHA1)) {
                                    index = j;
                                    break;
                                }
                            }
                            if (index != -1)
                                resource.replaceDependency(index, new ResourceDescriptor(hashinateStreamingChunk(mod, new Resource(e.data), e), ResourceType.STREAMING_CHUNK));
                        }
                        resource.replaceDependency(i, new ResourceDescriptor(null, ResourceType.STREAMING_CHUNK));
                        continue;
                    }
                    */

                    byte[] data = Globals.extractFile(res);
                    if (data == null) continue;
                    Resource dependency = new Resource(data);

                    if (dependency.method == SerializationMethod.BINARY)
                        resource.replaceDependency(i, new ResourceDescriptor(hashinate(mod, dependency, dependencyEntry), res.type));
                    else {
                        mod.add(dependencyEntry.path, data, dependencyEntry.GUID);
                        resource.replaceDependency(i, new ResourceDescriptor(SHA1.fromBuffer(data), res.type));
                    }
                }
            }
            if (resource.type == ResourceType.PLAN)
                Plan.removePlanDescriptors(resource, entry.GUID);
            byte[] data = null;
            if (resource.type == ResourceType.GFX_MATERIAL && registry != null) {
                GfxMaterialInfo info = new GfxMaterialInfo(new GfxMaterial(resource));
                data = info.build(mod, registry);
            } else data = resource.compressToResource();
            mod.add(entry.path, data, entry.GUID);
            return SHA1.fromBuffer(data);
        }
        return new SHA1();
    }

    public static Vector3f decodeI32(long value) {
        Vector3f output = new Vector3f(0, 0, 0);
        
        float x = (float) (value & 0x3ffl);
        boolean x_sign = ((value >>> 10l) & 1l) > 0l;
        
        float y = (float) ((value >>> 11l) & 0x3ffl);
        boolean y_sign = ((value >>> 21l) & 1l) > 0l;
        
        float z = (float) ((value >>> 22l) & 0x1ffl);
        boolean z_sign = ((value >>> 31l & 1l)) > 0l;

        if (x_sign) output.x = -((1023f - x) / 1023f);
        else output.x = ((x / 1023f));

        if (y_sign) output.y = -((1023f - y) / 1023f);
        else output.y = (y / 1023f);
        
        if (z_sign) output.z = -((511f - z) / 511f);
        else output.z = (z / 511f);
        
        return output;
    }
    
    public static Vector3f decodeI24(int value) {
        Vector3f output = new Vector3f(0, 0, 0);
        
        float x = (float) (value & 0x7ff);
        boolean x_sign = ((value >>> 11) & 1) > 0;
        
        float y = (float) ((value >>> 12) & 0x3ff);
        boolean y_sign = ((value >>> 22) & 1) > 0;
      
        boolean z_sign = ((value >>> 23) & 1) > 0;
        
        if (x_sign) output.x = -((2047f - x) / 2047f);
        else output.x = ((x / 2047f));

        if (y_sign) output.y = -((1023f - y) / 1023f);
        else output.y = (y / 1023f);
       
        output.z = (float) Math.sqrt(1 - ((Math.pow(output.x, 2)) + (Math.pow(output.y, 2))));
        
        if (z_sign)
          output.z = -output.z;
        
        return output;
    }
    
    public static byte[] computeSignature(byte[] data, byte[] key) {
        SecretKey secretKey = new SecretKeySpec(key, "HmacSHA1");
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            System.err.println("An error occurred computing signature.");
        }
        return new byte[14];
    }
}
