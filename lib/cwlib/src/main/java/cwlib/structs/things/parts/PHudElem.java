package cwlib.structs.things.parts;

import java.util.ArrayList;

import org.joml.Matrix4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.HUDDecal;

public class PHudElem implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x90;

    public int layer, inputType;
    public int transmitMode, renderMode, zTest;

    @GsonRevision(lbp3 = true, min = 0x37)
    public float scaleX = 1.0f, scaleY = 1.0f, offsetX, offsetY;

    public float brightness = 1.0f, alpha = 1.0f;
    public int color = 0xFFFFFFFF;

    @GsonRevision(lbp3 = true, min = 0x37)
    public Matrix4f transform;

    public int colorIndex;
    public String name;

    public ArrayList<HUDDecal> decals = new ArrayList<>();

    @GsonRevision(lbp3 = true, min = 0x47)
    public int onAnim, offAnim, highlightAnim;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x23)
        {
            layer = serializer.i32(layer);
            inputType = serializer.i32(inputType);
            transmitMode = serializer.i32(transmitMode);
        }

        if (subVersion < 0x47)
        {
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
        }

        if (subVersion > 0x23)
        {
            renderMode = serializer.i32(renderMode);
            zTest = serializer.i32(zTest);
        }

        if (subVersion > 0x36)
        {
            scaleX = serializer.f32(scaleX);
            scaleY = serializer.f32(scaleY);
            offsetX = serializer.f32(offsetX);
            offsetY = serializer.f32(offsetY);
        }

        if (subVersion > 0x23)
        {
            brightness = serializer.f32(brightness);
            alpha = serializer.f32(alpha);
            color = serializer.i32(color);
        }

        if (subVersion > 0x36)
            transform = serializer.m44(transform);

        if (subVersion >= 0x24)
        {
            colorIndex = serializer.s32(colorIndex);
            name = serializer.wstr(name);
        }

        if (subVersion > 0x23)
            decals = serializer.arraylist(decals, HUDDecal.class);

        if (subVersion < 0x55)
        {
            serializer.u8(0);
            serializer.u8(0);
            serializer.u8(0);
            serializer.u8(0);
            serializer.u16(0);
            serializer.f32(0);
            serializer.i32(0);
        }

        if (subVersion >= 0x47)
        {
            onAnim = serializer.i32(onAnim);
            offAnim = serializer.i32(offAnim);
            highlightAnim = serializer.i32(highlightAnim);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PHudElem.BASE_ALLOCATION_SIZE;
        if (this.decals != null)
            size += (this.decals.size() * HUDDecal.BASE_ALLOCATION_SIZE);
        return size;
    }
}
