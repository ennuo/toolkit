package cwlib.structs.custom;

import org.joml.Vector2f;
import org.joml.Vector3f;

import cwlib.enums.Branch;
import cwlib.enums.CacheFlags;
import cwlib.enums.Revisions;
import cwlib.enums.TextureType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

public class CachedShader implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public short flags = CacheFlags.NONE;
    public boolean swizzled = false;
    public Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);
    public Vector2f scale = new Vector2f(1.0f, 1.0f);
    public byte[] lookup = new byte[TextureType.MAX];
    public String path;
    public byte[] shader;
    
    @SuppressWarnings("unchecked")
    @Override public CachedShader serialize(Serializer serializer, Serializable structure) {
        CachedShader entry = (structure == null) ? new CachedShader() : (CachedShader) structure;

        Revision revision = serializer.getRevision();

        if (revision.has(Branch.MIZUKI, Revisions.MZ_CGC_SHORT_FLAGS))
            entry.flags = serializer.i16(entry.flags);
        else
            entry.flags = serializer.i8((byte) entry.flags);

        if (revision.has(Branch.MIZUKI, Revisions.MZ_CGC_SWIZZLE))
            entry.swizzled = serializer.bool(entry.swizzled);

        entry.color = serializer.v3(entry.color);
        entry.scale = serializer.v2(entry.scale);
        entry.lookup = serializer.bytearray(entry.lookup);

        if (revision.has(Branch.MIZUKI, Revisions.MZ_CGC_PATH))
            entry.path = serializer.str(entry.path);

        entry.shader = serializer.bytearray(entry.shader);

        return entry;
    }

    @Override public int getAllocatedSize() {
        int size = CachedShader.BASE_ALLOCATION_SIZE;
        if (this.lookup != null) size += this.lookup.length;
        if (this.shader != null) size += this.shader.length;
        if (this.path != null) size += this.path.length();
        return size;
    }
}
