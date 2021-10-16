package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class SkinWeight {
    public byte[] boneIndex;
    public Vector4f weights = new Vector4f(0, 0, 0, 0);
    public Vector3f normal = new Vector3f(0, 0, 0);
    public Vector3f unknown = new Vector3f(0, 0, 0);
    public Vector3f tangent = new Vector3f(0, 0, 0);
    
    public static Vector3f decodeI32(long value) {
        Vector3f output = new Vector3f(0, 0, 0);
        
        float x = (float) (value & 0x3ffl);
        boolean x_sign = ((value >>> 10l) & 1l) > 0l;
        
        float y = (float) ((value >>> 11l) & 0x3ffl);
        boolean y_sign = ((value >>> 21l) & 1l) > 0l;
        
        float z = (float) ((value >>> 22l) & 0x1ffl);
        boolean z_sign = ((value >>> 31l & 1l)) > 0l;

        if (x_sign) output.x = -((1023f - x) / 1023f);
        else output.x = ((x / 1023f));

        if (y_sign) output.y = -((1023f - y) / 1023f);
        else output.y = (y / 1023f);
        
        if (z_sign) output.z = -((511f - z) / 511f);
        else output.z = (z / 511f);
        
        return output;
    }
    
    public static Vector3f decodeI24(int value) {
        Vector3f output = new Vector3f(0, 0, 0);
        
        float x = (float) (value & 0x7ff);
        boolean x_sign = ((value >>> 11) & 1) > 0;
        
        float y = (float) ((value >>> 12) & 0x3ff);
        boolean y_sign = ((value >>> 22) & 1) > 0;
      
        boolean z_sign = ((value >>> 23) & 1) > 0;
        
        if (x_sign) output.x = -((2047f - x) / 2047f);
        else output.x = ((x / 2047f));

        if (y_sign) output.y = -((1023f - y) / 1023f);
        else output.y = (y / 1023f);
       
        output.z = (float) Math.sqrt(1 - ((Math.pow(output.x, 2)) + (Math.pow(output.y, 2))));
        
        if (z_sign)
          output.z = -output.z;
        
        return output;
    }
    
    public SkinWeight(Data data) {
        
        float[] weight = new float[4];
        weight[3] = 0;
        weight[2] = (float) ((int) (data.i8() & 0xFF));
        weight[1] = (float) ((int) (data.i8() & 0xFF));
        weight[0] = (float) ((int) (data.i8() & 0xFF));
        
        if (weight[0] != 0xFF) {
            weight[3] = 0xFE - weight[2] - weight[1] - weight[0];
            this.weights = new Vector4f(weight[0] / 0xFE, weight[1] / 0xFE, weight[2] / 0xFE, weight[3] / 0xFE);
        } else this.weights = new Vector4f(1.0f, 0, 0, 0);
        
        this.boneIndex = new byte[4];
        
        this.boneIndex[0] = data.i8();
        this.normal = decodeI24(data.i24());
        this.boneIndex[1] = data.i8();
        this.unknown = decodeI24(data.i24());
        this.boneIndex[2] = data.i8();
        this.tangent = decodeI24(data.i24());
        this.boneIndex[3] = data.i8();
    }
    
    public void serialize(Output output) {
        output.bytes(new byte[] { 0x00, 0x00, (byte) Math.round(this.weights.x * 255.0f), this.boneIndex[0] });
        output.pad(0xC);
    }
}
