package cwlib.types.mods.legacy.patches;

import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.enums.PatchType;

public interface ModPatch {
    default public PatchType type() { return PatchType.UNKNOWN; }
    
    public void apply();
    
    public void serialize(MemoryOutputStream output);
    public static ModPatch deserialize(MemoryInputStream data) {
        PatchType type = PatchType.getValue(data.i8());
        switch (type) {
            case LAMS: return TranslationPatch.deserialize(data);
            default: throw new Error("Unknown Patch!");
        }
    }
}
