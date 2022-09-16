package cwlib.types.mods.patches;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.PatchType;
import cwlib.io.gson.PatchSerializer;

@JsonAdapter(PatchSerializer.class)
public abstract class ModPatch {
    private PatchType type;
    private String name;

    protected ModPatch(PatchType type) {
        if (type == null)
            throw new NullPointerException("Patch type cannot be null!");
        this.type = type;
    }

    public PatchType getType() { return this.type; }
    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }
}
