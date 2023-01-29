package cwlib.structs.things.parts;

import java.util.ArrayList;

import org.joml.Matrix4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.HUDDecal;

public class PHudElem implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x90;

    public int layer, inputType;
    public int transmitMode, renderMode, zTest;

    @GsonRevision(lbp3=true, min=0x37)
    public float scaleX = 1.0f, scaleY = 1.0f, offsetX, offsetY;

    public float brightness = 1.0f, alpha = 1.0f;
    public int color = 0xFFFFFFFF;

    @GsonRevision(lbp3=true, min=0x37)
    public Matrix4f transform;

    public int colorIndex;
    public String name;

    public ArrayList<HUDDecal> decals = new ArrayList<>();

    @GsonRevision(lbp3=true, min=0x47)
    public int onAnim, offAnim, highlightAnim;
    
    @SuppressWarnings("unchecked")
    @Override public PHudElem serialize(Serializer serializer, Serializable structure) {
        PHudElem hud = (structure == null) ? new PHudElem() : (PHudElem) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x23) {
            hud.layer = serializer.i32(hud.layer);
            hud.inputType = serializer.i32(hud.inputType);
            hud.transmitMode = serializer.i32(hud.transmitMode);
        }

        if (subVersion < 0x47) {
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
        }

        if (subVersion > 0x23) {
            hud.renderMode = serializer.i32(hud.renderMode);
            hud.zTest = serializer.i32(hud.zTest);
        }

        if (subVersion > 0x36) {
            hud.scaleX = serializer.f32(hud.scaleX);
            hud.scaleY = serializer.f32(hud.scaleY);
            hud.offsetX = serializer.f32(hud.offsetX);
            hud.offsetY = serializer.f32(hud.offsetY);
        }

        if (subVersion > 0x23) {
            hud.brightness = serializer.f32(hud.brightness);
            hud.alpha = serializer.f32(hud.alpha);
            hud.color = serializer.i32(hud.color);
        }

        if (subVersion > 0x36)
            hud.transform = serializer.m44(hud.transform);

        if (subVersion >= 0x24) {
            hud.colorIndex = serializer.s32(hud.colorIndex);
            hud.name = serializer.wstr(hud.name);
        }

        if (subVersion > 0x23)
            hud.decals = serializer.arraylist(hud.decals, HUDDecal.class);
        
        if (subVersion < 0x55) {
            serializer.u8(0);
            serializer.u8(0);
            serializer.u8(0);
            serializer.u8(0);
            serializer.u16(0);
            serializer.f32(0);
            serializer.i32(0);
        }

        if (subVersion >= 0x47) {
            hud.onAnim = serializer.i32(hud.onAnim);
            hud.offAnim = serializer.i32(hud.offAnim);
            hud.highlightAnim = serializer.i32(hud.highlightAnim);
        }

        return hud;
    }

    @Override public int getAllocatedSize() {
        int size = PHudElem.BASE_ALLOCATION_SIZE;
        if (this.decals != null)
            size += (this.decals.size() * HUDDecal.BASE_ALLOCATION_SIZE);
        return size;
    }
}
