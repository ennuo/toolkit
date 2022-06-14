package cwlib.types.data;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.util.Bytes;
import org.joml.Vector4f;

public class Color implements Serializable {
    public float r, g, b, a;
    
    public Color() {}
    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    public Color(int r, int g, int b, int a) {
        this.r = r / 255.0f;
        this.g = g / 255.0f;
        this.b = b / 255.0f;
        this.a = b / 255.0f;
    }
    
    public Color serialize(Serializer serializer, Serializable structure) {
        Color color = (structure == null) ? new Color() : (Color) structure;
        
        if (serializer.isWriting) {
            if (serializer.revision.head > 0x37b)
                serializer.output.i32(Bytes.toInteger(new byte[] { 
                    (byte) (color.r * 255),
                    (byte) (color.g * 255),
                    (byte) (color.b * 255),
                    (byte) (color.a * 255),
                }));
            else
                serializer.output.v4(new Vector4f(color.r, color.g, color.b, color.a));
        } else {
            if (serializer.revision.head > 0x37b) {
                int bits = serializer.input.i32();
                color.r = ((bits >> 24) & 0xFF) / 255.0f;
                color.g = ((bits >> 16) & 0xFF) / 255.0f;
                color.b = ((bits >> 8) & 0xFF) / 255.0f;
                color.a = ((bits) & 0xFF) / 255.0f;
            } else {
                Vector4f data = serializer.input.v4();
                color.r = data.x;
                color.g = data.y;
                color.b = data.z;
                color.a = data.w;
            }
        }
        
        return color;
    }
    
}
