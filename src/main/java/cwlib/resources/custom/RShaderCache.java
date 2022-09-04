package cwlib.resources.custom;

import java.util.ArrayList;

import cwlib.enums.Branch;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.custom.CachedShader;
import cwlib.types.data.Revision;

public class RShaderCache implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    @GsonRevision(min=2,branch=0x4d5a)
    private boolean orbis;
    
    private ArrayList<CachedShader> shaders = new ArrayList<>();

    public RShaderCache() { this.orbis = false; };
    public RShaderCache(boolean orbis) { this.orbis = orbis; }

    @SuppressWarnings("unchecked")
    @Override public RShaderCache serialize(Serializer serializer, Serializable structure) {
        RShaderCache cache = (structure == null) ? new RShaderCache() : (RShaderCache) structure;

        if (serializer.getRevision().has(Branch.MIZUKI, Revisions.MZ_CGC_ORBIS))
            cache.orbis = serializer.bool(cache.orbis);

        cache.shaders = serializer.arraylist(cache.shaders, CachedShader.class);

        return cache;
    }

    @Override public int getAllocatedSize() {
        int size = RShaderCache.BASE_ALLOCATION_SIZE;
        for (CachedShader shader : shaders)
            size += shader.getAllocatedSize();
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RShaderCache.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags, 
            ResourceType.SHADER_CACHE,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    public boolean isOrbis() { return this.orbis; }
    public ArrayList<CachedShader> getShaders() { return this.shaders; }
}
