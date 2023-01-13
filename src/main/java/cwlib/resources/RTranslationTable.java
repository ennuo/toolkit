package cwlib.resources;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cwlib.ex.SerializationException;

import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Resource that holds all translations
 * used in the game.
 */
public class RTranslationTable {
    /**
     * Calculates LAMS key ID from translation tag.
     * @param tag Translation tag
     * @return Hashed key from translation tag
     */
    public static final long makeLamsKeyID(String tag) {
        if (tag == null) tag = "";
        long v0 = 0, v1 = 0xC8509800L;
        for (int i = 32; i > 0; --i) {
            long c = 0x20;
            if ((i - 1) < tag.length())
                c = tag.charAt(i - 1);
            v0 = v0 * 0x1b + c;
        }
        
        if (tag.length() > 32) {
            v1 = 0;
            for (int i = 64; i > 32; --i) {
                long c = 0x20;
                if ((i - 1) < tag.length())
                    c = tag.charAt(i - 1);
                v1 = v1 * 0x1b + c;
            }
        }
        
        return (v0 + v1 * 0xDEADBEEFL) & 0xFFFFFFFFL;
    }

    /**
     * LAMS -> Text pairs
     */
    private HashMap<Long, String> lookup = new HashMap<>();

    /**
     * Processes a translation table from a buffer.
     * @param data Buffer to process
     */
    public RTranslationTable(byte[] data) {
        // Legacy RTranslationTable is just a text file.
        if (Bytes.toShortBE(data) == ((short) 0xFEFF)) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_16))) {
                String key;
                while ((key = reader.readLine()) != null) {
                    if (key.isEmpty()) continue;
                    String value = reader.readLine();
                    this.lookup.put(RTranslationTable.makeLamsKeyID(key), value);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new SerializationException("Failed to parse RTranslationTable due to IOException!");
            }
            return;
        }
        
        MemoryInputStream stream = new MemoryInputStream(data);
        int dataLength = stream.getLength();

        int count = stream.i32();

        int tableOffset = 0x4 + (count * 0x8);
        this.lookup = new HashMap<>(count);
        for (int i = 0; i < count; ++i) {
            long key = stream.u32();
            int stringStart = stream.i32();

            int oldOffset = stream.getOffset();

            stream.seek(tableOffset + stringStart + 2, SeekMode.Begin);
            while (stream.getOffset() != dataLength)
                if (stream.u16() == 0xFEFF)
                    break;
            
            int stringEnd = stream.getOffset() - 2;
            stream.seek(tableOffset + stringStart, SeekMode.Begin);

            String text = new String(stream.bytes(stringEnd - stream.getOffset()), StandardCharsets.UTF_16);
            this.lookup.put(key, text);

            stream.seek(oldOffset, SeekMode.Begin);
        }
    }

    /**
     * Gets translated string from translation tag, e.g: COSTUME_TORSO.
     * @param tag Translation tag
     * @return Translated string
     */
    public String translate(String tag) {
        return this.translate(RTranslationTable.makeLamsKeyID(tag));
    }

    /**
     * Gets translated string from key.
     * @param key LAMS Key ID
     * @return Translated string
     */
    public String translate(int key) { return this.translate(key & 0xFFFFFFFFl); }

    /**
     * Gets translated string from key.
     * @param key LAMS Key ID
     * @return Translated string
     */
    public String translate(long key) {
        if (this.lookup.containsKey(key))
            return this.lookup.get(key);
        return null;
    }

    /**
     * Write the translation table to a JSON file on disk.
     * @param path Path to export to
     */
    public void export(String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileIO.write(gson.toJson(this.lookup).getBytes(StandardCharsets.UTF_8), path);
    }
}
