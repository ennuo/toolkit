package cwlib.structs.things.components;

import cwlib.enums.Branch;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.types.data.GUID;
import cwlib.types.data.Revision;

import org.joml.Matrix4f;

public class Decoration implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public PRenderMesh renderMesh;
    public Matrix4f offset = new Matrix4f().identity();
    public int parentBone = -1;
    public int parentTriVert = -1;
    public float baryU, baryV;
    public float decorationAngle;
    public int onCostumePiece = -1;
    public int earthDecoration = -1;
    public float decorationScale = 1.0f;
    @GsonRevision(min = 0x214)
    public short placedBy = -1;
    public boolean reversed;
    @GsonRevision(lbp3 = true, min = 0xc4)
    public boolean hasShadow = true;
    @GsonRevision(lbp3 = true, min = 0x16c)
    public boolean isQuest = false;
    @GsonRevision(min = 0x215)
    public int playModeFrame;
    @GsonRevision(min = 0x25b)
    public GUID planGUID;
    @GsonRevision(branch = 0x4431, min = 0x7c)
    public float zBias; // Vita

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        renderMesh = serializer.reference(renderMesh, PRenderMesh.class);

        offset = serializer.m44(offset);
        parentBone = serializer.i32(parentBone);
        parentTriVert = serializer.i32(parentTriVert);

        baryU = serializer.f32(baryU);
        baryV = serializer.f32(baryV);

        decorationAngle = serializer.f32(decorationAngle);
        onCostumePiece = serializer.s32(onCostumePiece);
        earthDecoration = serializer.s32(earthDecoration);
        decorationScale = serializer.f32(decorationScale);

        if (version >= 0x214)
            placedBy = serializer.i16(placedBy);
        reversed = serializer.bool(reversed);

        if (subVersion >= 0xc4)
            hasShadow = serializer.bool(hasShadow);

        if (subVersion >= 0x16c)
            isQuest = serializer.bool(isQuest);

        if (version >= 0x215)
            playModeFrame = serializer.i32(playModeFrame);

        if (version >= 0x25b)
            planGUID = serializer.guid(planGUID);

        if (revision.has(Branch.DOUBLE11, 0x7c))
            zBias = serializer.f32(zBias);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = Decoration.BASE_ALLOCATION_SIZE;
        if (this.renderMesh != null)
            size += this.renderMesh.getAllocatedSize();
        return size;
    }
}
