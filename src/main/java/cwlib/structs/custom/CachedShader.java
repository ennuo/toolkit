package cwlib.structs.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.joml.Vector2f;
import org.joml.Vector3f;

import cwlib.enums.Branch;
import cwlib.enums.CacheFlags;
import cwlib.enums.ParameterType;
import cwlib.enums.Revisions;
import cwlib.enums.TextureType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class CachedShader implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public short flags = CacheFlags.NONE;
    @GsonRevision(min=4,max=0xa, branch=0x4d5a) private boolean swizzled = false;
    @GsonRevision(max=0xa, branch=0x4d5a) private Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);
    @GsonRevision(max=0xa, branch=0x4d5a) private Vector2f scale = new Vector2f(1.0f, 1.0f);
    @GsonRevision(min=0xb, branch=0x4d5a) public ArrayList<ParameterOffset> offsets = new ArrayList<>();
    public byte[] lookup = new byte[TextureType.MAX];
    @GsonRevision(min=3,branch=0x4d5a) public String path;
    public byte[] shader;

    public CachedShader() {
        for (int i = 0; i < this.lookup.length; ++i)
            this.lookup[i] = -1;
    }
    
    @SuppressWarnings("unchecked")
    @Override public CachedShader serialize(Serializer serializer, Serializable structure) {
        CachedShader entry = (structure == null) ? new CachedShader() : (CachedShader) structure;

        Revision revision = serializer.getRevision();

        if (revision.has(Branch.MIZUKI, Revisions.MZ_CGC_SHORT_FLAGS))
            entry.flags = serializer.i16(entry.flags);
        else
            entry.flags = serializer.i8((byte) entry.flags);

        if (revision.before(Branch.MIZUKI, Revisions.MZ_CGC_OFFSETS)) {
            if (revision.has(Branch.MIZUKI, Revisions.MZ_CGC_SWIZZLE))
                entry.swizzled = serializer.bool(entry.swizzled);

            entry.color = serializer.v3(entry.color);
            entry.scale = serializer.v2(entry.scale);
        }

        if (revision.has(Branch.MIZUKI, Revisions.MZ_CGC_OFFSETS))
            entry.offsets = serializer.arraylist(entry.offsets, ParameterOffset.class);
        
        entry.lookup = serializer.bytearray(entry.lookup);

        if (revision.has(Branch.MIZUKI, Revisions.MZ_CGC_PATH))
            entry.path = serializer.str(entry.path);

        entry.shader = serializer.bytearray(entry.shader);

        return entry;
    }

    public boolean has(ParameterType type) {
        for (ParameterOffset offset : this.offsets)
            if (offset.type == type)
                return true;
        return false;
    }

    public ParameterOffset[] get(ParameterType type) {
        ArrayList<ParameterOffset> parameters = new ArrayList<>();
        for (ParameterOffset parameter : this.offsets)
            if (parameter.type == type)
                parameters.add(parameter);
        return parameters.toArray(ParameterOffset[]::new);
    }

    @Override public int getAllocatedSize() {
        int size = CachedShader.BASE_ALLOCATION_SIZE;
        if (this.lookup != null) size += this.lookup.length;
        if (this.shader != null) size += this.shader.length;
        if (this.path != null) size += this.path.length();
        return size;
    }
}
