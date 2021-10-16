package ennuo.craftworld.resources;

import ennuo.craftworld.memory.Data;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TranslationTable {

    public static long MakeLamsKeyID(String key) {
        long v0 = 0, v1 = 0xC8509800L;
        for (int i = 32; i > 0; --i) {
            long c = 0x20;
            if ((i - 1) < key.length())
                c = key.charAt(i - 1);
            v0 = v0 * 0x1b + c;
        }
        
        if (key.length() > 32) {
            v1 = 0;
            for (int i = 64; i > 32; --i) {
                long c = 0x20;
                if ((i - 1) < key.length())
                    c = key.charAt(i - 1);
                v1 = v1 * 0x1b + c;
            }
        }
        
        return (v0 + v1 * 0xDEADBEEFL) & 0xFFFFFFFFL;
    }
    
    
    public Map<Long, String> map;

    public TranslationTable(Data data) {
        process(data);
    }

    private void process(Data data) {
        System.out.println("Started processing Translation Table...");
        long begin = System.currentTimeMillis();

        int count = data.i32();
        System.out.println("Entry Count: " + count);

        int tableOffset = 4 + (count * 8);
        map = new HashMap<Long, String>(count);
        for (int i = 0; i < count; ++i) {
            long key = data.u32();
            int offset = data.i32();
            int old = data.offset;

            data.seek(tableOffset + offset + 2);
            while (data.offset != data.length) {
                if (data.i16() == -257)
                    break;
            };

            int dest = data.offset - 2;
            data.seek(tableOffset + offset);

            String str = new String(data.bytes(dest - data.offset), StandardCharsets.UTF_16);

            data.seek(old);

            map.put(key, str);
        }

        long end = System.currentTimeMillis();
        System.out.println("Finished processing Translation Table! (" + ((end - begin) / 1000L) + "s : " + (end - begin) + "ms)");
    }

    public String Translate(int key) {
        return Translate((long) key);
    }
    public String Translate(long key) {
        if (map.containsKey(key))
            return map.get(key);
        return null;
    }

}