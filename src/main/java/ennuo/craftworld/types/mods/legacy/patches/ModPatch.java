package ennuo.craftworld.types.mods.legacy.patches;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.resources.enums.PatchType;

public interface ModPatch {
    default public PatchType type() { return PatchType.UNKNOWN; }
    
    public void apply();
    
    public void serialize(Output output);
    public static ModPatch deserialize(Data data) {
        PatchType type = PatchType.getValue(data.i8());
        switch (type) {
            case LAMS: return TranslationPatch.deserialize(data);
            default: throw new Error("Unknown Patch!");
        }
    }
}
