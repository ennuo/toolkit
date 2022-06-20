package cwlib.types.mods.legacy.patches;

import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.enums.PatchType;

public class TranslationPatch implements ModPatch {
    public String key;
    public int ID;
    public String value;

    @Override public PatchType type() { return PatchType.LAMS; }
    @Override public void apply() { throw new UnsupportedOperationException("Not supported yet."); }

    @Override public void serialize(MemoryOutputStream output) {
        output.str(this.key);
        output.i32(this.ID);
        output.wstr(this.value);
    }
    
    public static ModPatch deserialize(MemoryInputStream data) {
        TranslationPatch patch = new TranslationPatch();
        patch.key = data.str();
        patch.ID = data.i32();
        patch.value = data.wstr();
        System.out.println(String.format("Found Translation Patch with key = %s, ID = %d, value = %s", patch.key, patch.ID, patch.value));
        return patch;
    }
    
}
