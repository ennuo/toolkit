package cwlib.types;

import cwlib.io.streams.MemoryOutputStream;
import cwlib.util.Bytes;
import java.util.ArrayList;
import java.util.Random;

public class ParamSFO {
    public static final short FORMAT_UTF8 = 0x0004;
    public static final short FORMAT_UTF8_NULL = 0x0204;
    public static final short FORMAT_INT32 = 0x0404;
    
    public class SfoEntry {
        String key;
        short format;
        int length;
        int maxLength;
        byte[] value;
        
        public SfoEntry(String key, short format, int maxLength, byte[] value) {
            this.key = key;
            this.format = format;
            this.maxLength = maxLength;
            if (value != null)
                this.length = value.length;
            this.value = value;
        }
    }
    
    private ArrayList<SfoEntry> entries = new ArrayList<SfoEntry>();
    
    public ParamSFO(String titleID, String name) {
        this.addKey("ACCOUNT_ID", FORMAT_UTF8, 0x10, "0000000000000000".getBytes());
        this.addKey("ATTRIBUTE", FORMAT_INT32, 0x4, new byte[0x4]);
        this.addKey("CATEGORY", FORMAT_UTF8_NULL, 0x4, ("SD" + '\0').getBytes());
        this.addKey("DETAIL", FORMAT_UTF8_NULL, 0x400, ("Automated export from Craftworld Toolkit" + '\0').getBytes());
        this.addKey("PARAMS", FORMAT_UTF8, 0x400, new byte[0x400]);
        this.addKey("PARAMS2", FORMAT_UTF8, 0xC, new byte[0xC]);
        this.addKey("PARENTAL_LEVEL", FORMAT_INT32, 4, new byte[0x4]);
        this.addKey("SAVEDATA_DIRECTORY", FORMAT_UTF8_NULL, 0x40, (titleID + '\0').getBytes());
        this.addKey("SAVEDATA_LIST_PARAM", FORMAT_UTF8_NULL, 0x8, new byte[1]);
        this.addKey("SUB_TITLE", FORMAT_UTF8_NULL, 0x80, (name + '\0').getBytes());
        this.addKey("TITLE", FORMAT_UTF8_NULL, 0x80, ("LittleBigPlanet Backup" + '\0').getBytes());
    }
    
    public void addKey(String key, short format, int maxLength, byte[] value) {
        this.entries.add(
                new SfoEntry(key, format, maxLength, value)
        );
    }
    
    
    public byte[] build() {
        MemoryOutputStream output = new MemoryOutputStream(0x1000);
        output.i32(0x00505346); // magic
        output.i32(0x01010000); // version
        int keysOffset = 0x14 + this.entries.size() * 0x10;
        output.u32LE(keysOffset);
        int valuesOffset = keysOffset + this.entries
                .stream()
                .mapToInt(element -> element.key.length())
                .reduce(0, (total, element) -> total + element + 1);
        if (valuesOffset % 4 != 0)
            valuesOffset += (4 - (valuesOffset % 4));
        output.u32LE(valuesOffset);
        output.u32LE(this.entries.size());
        
        int lastKeyOffset = keysOffset, lastValueOffset = valuesOffset;
        for (int i = 0; i < this.entries.size(); ++i) {
            SfoEntry entry = this.entries.get(i);
            
            output.offset = 0x14 + i * 0x10;
            output.i16LE((short) (lastKeyOffset - keysOffset));
            output.i16LE(entry.format);
            output.u32LE(entry.length);
            output.u32LE(entry.maxLength);
            output.u32LE(lastValueOffset - valuesOffset);
            
            output.offset = lastKeyOffset;
            output.str(entry.key + '\0');
            lastKeyOffset = output.offset;
            
            output.offset = lastValueOffset;
            if (entry.value == null) output.bytes(new byte[entry.maxLength]);
            else {
                output.bytes(entry.value);
                if (entry.value.length < entry.maxLength)
                    output.bytes(new byte[entry.maxLength - entry.value.length]);
            }
            lastValueOffset = output.offset;
        }
        
        output.shrink();
        return output.buffer;
    }
}
