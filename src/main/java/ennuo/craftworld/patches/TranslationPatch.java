package ennuo.craftworld.patches;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.resources.enums.PatchType;

public class TranslationPatch implements ModPatch {
    public String key;
    public int ID;
    public String value;

    @Override
    public PatchType type() {
        return PatchType.LAMS;
    }

    @Override
    public void apply() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void serialize(Output output) {
        output.str8(this.key);
        output.i32(this.ID);
        output.str16(this.value);
    }
    
    public static ModPatch deserialize(Data data) {
        TranslationPatch patch = new TranslationPatch();
        patch.key = data.str8();
        patch.ID = data.i32();
        patch.value = data.str16();
        System.out.println(String.format("Found Translation Patch with key = %s, ID = %d, value = %s", patch.key, patch.ID, patch.value));
        return patch;
    }
    
}
