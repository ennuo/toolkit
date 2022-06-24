package cwlib.types;

import cwlib.io.streams.MemoryOutputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import java.util.ArrayList;

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
        MemoryOutputStream stream = new MemoryOutputStream(0x1000);
        stream.i32(0x00505346); // magic
        stream.i32(0x01010000); // version
        stream.setLittleEndian(true);
        int keysOffset = 0x14 + this.entries.size() * 0x10;
        stream.u32(keysOffset);
        int valuesOffset = keysOffset + this.entries
                .stream()
                .mapToInt(element -> element.key.length())
                .reduce(0, (total, element) -> total + element + 1);
        if (valuesOffset % 4 != 0)
            valuesOffset += (4 - (valuesOffset % 4));
        stream.u32(valuesOffset);
        stream.u32(this.entries.size());
        
        int lastKeyOffset = keysOffset, lastValueOffset = valuesOffset;
        for (int i = 0; i < this.entries.size(); ++i) {
            SfoEntry entry = this.entries.get(i);
            
            stream.seek(0x14 + i * 0x10, SeekMode.Begin);
            stream.i16((short) (lastKeyOffset - keysOffset));
            stream.i16(entry.format);
            stream.u32(entry.length);
            stream.u32(entry.maxLength);
            stream.u32(lastValueOffset - valuesOffset);
            
            stream.seek(lastKeyOffset, SeekMode.Begin);
            stream.str(entry.key + '\0', entry.key.length() + 1);
            lastKeyOffset = stream.getOffset();
            
            stream.seek(lastValueOffset, SeekMode.Begin);
            if (entry.value == null) stream.bytes(new byte[entry.maxLength]);
            else {
                stream.bytes(entry.value);
                if (entry.value.length < entry.maxLength)
                    stream.bytes(new byte[entry.maxLength - entry.value.length]);
            }
            lastValueOffset = stream.getOffset();
        }
        
        stream.shrink();
        return stream.getBuffer();
    }
}
