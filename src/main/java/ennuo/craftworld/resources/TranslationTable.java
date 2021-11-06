package ennuo.craftworld.resources;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.toolkit.utilities.Globals;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TranslationTable {
    /**
     * Calculates LAMS key ID from translation tag.
     * @param tag Translation tag
     * @return Hashed key from translation tag
     */
    public static long makeLamsKeyID(String tag) {
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
    
    private Map<Long, String> lookup;

    /**
     * Creates translation table from data instance.
     * @param data Translation table data source
     */
    public TranslationTable(Data data) { this.process(data); }

    /**
     * Parses RTranslationTable from data instance.
     * @param data Translation table data source
     */    
    private void process(Data data) {
        System.out.println("Started processing Translation Table...");
        long begin = System.currentTimeMillis();

        int count = data.i32();
        System.out.println(String.format("Entry Count: %d", count));

        int tableOffset = 4 + (count * 8);
        this.lookup = new HashMap<Long, String>(count);
        for (int i = 0; i < count; ++i) {
            long key = data.u32();
            int offset = data.i32();
            int old = data.offset;

            data.seek(tableOffset + offset + 2);
            while (data.offset != data.length) {
                if (data.i16() == -257)
                    break;
            }

            int dest = data.offset - 2;
            data.seek(tableOffset + offset);

            String str = new String(data.bytes(dest - data.offset), StandardCharsets.UTF_16);

            data.seek(old);

            this.lookup.put(key, str);
        }

        long end = System.currentTimeMillis();
        System.out.println(
            String.format("Finished processing %s! (%s s %s ms)",
                "TranslationTable",
                ((end - begin) / 1000),
                (end - begin))
        );
    }
    
    /**
     * Gets translated string from translation tag, e.g: COSTUME_TORSO
     * @param tag Translation tag
     * @return Translated string
     */
    public String translate(String tag) {
        return this.translate(TranslationTable.makeLamsKeyID(tag));
    }

    /**
     * Gets translated string from key.
     * @param key LAMS Key ID
     * @return Translated string
     */
    public String translate(int key) { return this.translate((long) key); }
    
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
     * Exports Translation Table as a text document.
     * @return UTF-8 byte buffer
     */
    public byte[] export() {
        int dataSize = this.lookup.values()
                .stream()
                .mapToInt(element -> element.length())
                .reduce(0, (total, element) -> total + element) * 2;
        Output output = new Output(dataSize);
        for (Map.Entry<Long, String> entry : this.lookup.entrySet())
            output.str(entry.getKey() + "\n\t" + entry.getValue() + "\n");
        output.shrink();
        return output.buffer;
    }
}