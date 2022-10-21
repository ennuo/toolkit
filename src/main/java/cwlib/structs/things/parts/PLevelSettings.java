package cwlib.structs.things.parts;

import java.util.ArrayList;

import cwlib.enums.Branch;
import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.LevelSettings;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import editor.gl.MeshInstance;

public class PLevelSettings extends LevelSettings {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    @GsonRevision(min=0x153)
    public ArrayList<LevelSettings> presets = new ArrayList<>();

    @GsonRevision(branch=0x4431,min=0x78)
    public boolean nonLinearFog;
    public String backdropAmbience;
    @GsonRevision(min=0x2f3)
    public ResourceDescriptor backdropMesh;
    @GsonRevision(min=0xaf,lbp3=true)
    public int backgroundRepeatFlags;
    @GsonRevision(min=0xaf,lbp3=true)
    public float backgroundSkyHeight;

    public transient MeshInstance backdropInstance;

    @SuppressWarnings("unchecked")
    @Override public PLevelSettings serialize(Serializer serializer, Serializable structure) {
        PLevelSettings settings = (structure == null) ? new PLevelSettings() : (PLevelSettings) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        super.serialize(serializer, settings);

        if (version >= 0x153)
            settings.presets = serializer.arraylist(settings.presets, LevelSettings.class);

        if (0x152 < version && version < 0x15a)
            serializer.f32(0);

        if (revision.has(Branch.DOUBLE11, 0x78))
            settings.nonLinearFog = serializer.bool(settings.nonLinearFog);

        // 0x154 -> 0x155
        if (!(version < 0x154 || 0x155 < version)) {
            throw new SerializationException("Unsupported serialization object!");
            // matrix[0]
            // matrix[1]
            // colPick
            // vignetteTop
            // vignetteBottom
        }

        settings.backdropAmbience = serializer.str(settings.backdropAmbience);

        if (version < 0x156)
            serializer.resource(null, ResourceType.TEXTURE);
        
        if (version >= 0x2f3)
            settings.backdropMesh = serializer.resource(settings.backdropMesh, ResourceType.STATIC_MESH);

        if (subVersion >= 0xaf) {
            settings.backgroundRepeatFlags = serializer.i32(settings.backgroundRepeatFlags);
            settings.backgroundSkyHeight = serializer.f32(settings.backgroundSkyHeight);
        }

        return settings;
    }
    
    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;

        return size;
    }
}
