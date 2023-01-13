package cwlib.resources;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.JsonAdapter;
import cwlib.ex.SerializationException;
import cwlib.io.gson.TranslationTableSerializer;

import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Resource that holds all translations
 * used in the game.
 */
@JsonAdapter(TranslationTableSerializer.class)
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
    
    public RTranslationTable() {};

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
            
            int stringEnd = stream.getOffset();
            if (stringEnd != dataLength)
                stringEnd -= 2;
            
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
     * Adds an entry to the translation table by tag
     * @param key Translation tag
     * @param value Translated value
     */
    public void add(String key, String value) { 
        this.lookup.put(RTranslationTable.makeLamsKeyID(key), value);
    }
    
    /**
     * Adds an entry to the translation table by ID.
     * @param key LAMS Key ID
     * @param value Translated value
     */
    public void add(long key, String value) {
        this.lookup.put(key, value);
    }

    /**
     * Write the translation table to a JSON file on disk.
     * @param path Path to export to
     */
    public void export(String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileIO.write(gson.toJson(this.lookup).getBytes(StandardCharsets.UTF_8), path);
    }
    
    /**
     * Reads a translation table from a JSON file on disk.
     * @param path Path to import from
     * @return Imported RTranslationTable
     */
    public static RTranslationTable fromJSON(String path) {
        RTranslationTable table = GsonUtils.fromJSON(FileIO.readString(Path.of(path)), RTranslationTable.class);
        return table;
    }
    
    /**
     * Builds the current RTranslationTable instance to a buffer
     * @return Built table
     */
    public byte[] build() {
        int stringTableSize = this.lookup.values()
            .stream()
            .mapToInt(element -> (element.getBytes(StandardCharsets.UTF_16BE).length + 1) * 2)
            .reduce(0, (total, element) -> total + element) - 2;
        
        HashMap<String, Integer> offsets = new HashMap<>();
        MemoryOutputStream stringTable = new MemoryOutputStream(stringTableSize);
        MemoryOutputStream keyTable = new MemoryOutputStream((this.lookup.size() * 8) + 4);
        keyTable.i32(this.lookup.size());
        
        ArrayList<Long> keys = new ArrayList<>(this.lookup.keySet());
        keys.sort((a, z) -> Long.compareUnsigned(a, z));
        for (Long key : keys) {
            String value = this.lookup.get(key);
            keyTable.u32(key);
            if (offsets.containsKey(value)) {
                keyTable.i32(offsets.get(value));
                continue;
            }
            
            int offset = stringTable.getOffset();
            offsets.put(value, offset);
            
            keyTable.i32(offset);
            
            stringTable.u16(0xFEFF);
            stringTable.wstr(value, value.getBytes(StandardCharsets.UTF_16BE).length / 2);
        }
        
        stringTable.shrink();
        return Bytes.combine(keyTable.getBuffer(), stringTable.getBuffer());
    }
}
