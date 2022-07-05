package cwlib.structs.things.components;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.parts.PRenderMesh;
import org.joml.Matrix4f;

public class Decoration implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    PRenderMesh renderMesh;
    Matrix4f offset = new Matrix4f().identity();
    int parentBone = -1;
    int parentTriVert = -1;
    public float baryU, baryV;
    public float decorationAngle;
    public int onCostumePiece = -1;
    public int earthDecoration = -1;
    public float decorationScale = 1.0f;
    public short placedBy = -1;
    public boolean reversed;
    public boolean hasShadow = true;
    public boolean isQuest = false;
    public int playModeFrame;
    public int planGUID;
    
    @SuppressWarnings("unchecked")
    @Override public Decoration serialize(Serializer serializer, Serializable structure) {
        Decoration decoration = (structure == null) ? new Decoration() : (Decoration) structure;
        
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        decoration.renderMesh = serializer.reference(decoration.renderMesh, PRenderMesh.class);
        
        decoration.offset = serializer.m44(decoration.offset);
        decoration.parentBone = serializer.i32(decoration.parentBone);
        decoration.parentTriVert = serializer.i32(decoration.parentTriVert);
        
        decoration.baryU = serializer.f32(decoration.baryU);
        decoration.baryV = serializer.f32(decoration.baryV);
        
        decoration.decorationAngle = serializer.f32(decoration.decorationAngle);
        decoration.onCostumePiece = serializer.s32(decoration.onCostumePiece);
        decoration.earthDecoration = serializer.s32(decoration.earthDecoration);
        decoration.decorationScale = serializer.f32(decoration.decorationScale);
        
        if (version >= 0x214)
            decoration.placedBy = serializer.i16(decoration.placedBy);
        decoration.reversed = serializer.bool(decoration.reversed);

        if (subVersion >= 0xc4)
            decoration.hasShadow = serializer.bool(decoration.hasShadow);

        if (subVersion >= 0x16c)
            decoration.isQuest = serializer.bool(decoration.isQuest);
        
        if (version >= 0x215)
            decoration.playModeFrame = serializer.i32(decoration.playModeFrame);

        if (version >= 0x25b)
            decoration.planGUID = serializer.i32(decoration.planGUID);
        
        return decoration;
    }

    @Override public int getAllocatedSize() {
        int size = Decoration.BASE_ALLOCATION_SIZE;
        if (this.renderMesh != null)
            size += this.renderMesh.getAllocatedSize();
        return size;
    }
}
