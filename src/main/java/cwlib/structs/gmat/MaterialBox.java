package cwlib.structs.gmat;

import java.util.Arrays;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.BoxType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RGfxMaterial;
import cwlib.util.XmlFormatter;

public class MaterialBox implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    /**
     * Number of parameters used before r0x2a4
     */
    public static final int LEGACY_PARAMETER_COUNT = 0x6;

    public static final int PARAMETER_COUNT = 0x8;

    public int type;
    private int[] params = new int[PARAMETER_COUNT];
    public float x, y, w, h;
    public int subType;
    public MaterialParameterAnimation anim = new MaterialParameterAnimation();
    public MaterialParameterAnimation anim2 = new MaterialParameterAnimation();

    /**
     * Creates an output node
     */
    public MaterialBox() {};

    /**
     * Creates a texture sample node
     */
    public MaterialBox(Vector2f scale, Vector2f offset, int channel, int texture) {
        this.type = BoxType.TEXTURE_SAMPLE;
        this.params[0] = Float.floatToIntBits(scale.x);
        this.params[1] = Float.floatToIntBits(scale.y);
        this.params[2] = Float.floatToIntBits(offset.x);
        this.params[3] = Float.floatToIntBits(offset.y);
        this.params[4] = channel;
        this.params[5] = texture;
    }

    /**
     * Creates a texture sample node
     */
    public MaterialBox(Vector4f transform, int channel, int texture) {
        this.type = BoxType.TEXTURE_SAMPLE;
        this.params[0] = Float.floatToIntBits(transform.x);
        this.params[1] = Float.floatToIntBits(transform.y);
        this.params[2] = Float.floatToIntBits(transform.z);
        this.params[3] = Float.floatToIntBits(transform.w);
        this.params[4] = channel;
        this.params[5] = texture;
    }

    /***
     * Creates a color node
     */
    public MaterialBox(Vector4f color) {
        this.type = BoxType.COLOR;
        this.params[0] = Float.floatToIntBits(color.x);
        this.params[1] = Float.floatToIntBits(color.y);
        this.params[2] = Float.floatToIntBits(color.z);
        this.params[3] = Float.floatToIntBits(color.w);
    }

    @SuppressWarnings("unchecked")
    @Override public MaterialBox serialize(Serializer serializer, Serializable structure) {
        MaterialBox box = 
            (structure == null) ? new MaterialBox() : (MaterialBox) structure;

        box.type = serializer.i32(box.type);

        int head = serializer.getRevision().getVersion();

        if (!serializer.isWriting()) box.params = new int[PARAMETER_COUNT];
        if (head < 0x2a4) {
            for (int i = 0; i < LEGACY_PARAMETER_COUNT; ++i)
                box.params[i] = serializer.i32(box.params[i]);
        } else {
            serializer.i32(PARAMETER_COUNT);
            for (int i = 0; i < PARAMETER_COUNT; ++i)
                box.params[i] = serializer.i32(box.params[i]);
        }

        box.x = serializer.f32(box.x);
        box.y = serializer.f32(box.y);
        box.w = serializer.f32(box.w);
        box.h = serializer.f32(box.h);

        if (head > 0x2a3) 
            box.subType = serializer.i32(box.subType);
        
        if (head > 0x2a1)
            box.anim = serializer.struct(box.anim, MaterialParameterAnimation.class);
        if (head > 0x2a3)
            box.anim2 = serializer.struct(box.anim2, MaterialParameterAnimation.class);
        
        return box;
    }

    @Override public int getAllocatedSize() { 
        int size = MaterialBox.BASE_ALLOCATION_SIZE;
        size += this.anim.getAllocatedSize();
        size += this.anim2.getAllocatedSize();
        return size;
    }

    public int[] getParameters() { return this.params; }

    public boolean isColor() { return this.type == BoxType.COLOR; }
    public boolean isTexture() { return this.type == BoxType.TEXTURE_SAMPLE; }

    public void toXML(RGfxMaterial material, XmlFormatter xml)
    {
        switch (type)
        {
            case BoxType.OUTPUT: return;
            case BoxType.TEXTURE_SAMPLE: {

                xml.startTag("Texture");

                float rotation = 0.0f;
                int channel = params[4];
                int texunit = params[5];
                Vector2f scale, offset;

                if (subType == 0)
                {
                    scale = new Vector2f(
                        Float.intBitsToFloat(params[0]),
                        Float.intBitsToFloat(params[1])
                    );

                    offset = new Vector2f(
                        Float.intBitsToFloat(params[2]),
                        Float.intBitsToFloat(params[3])  
                    );
                }
                else
                {
                    float[] rotation_matrix = new float[] 
                    {
                        Float.intBitsToFloat(params[0]),
                        Float.intBitsToFloat(params[3]),
                        Float.intBitsToFloat(params[1]),
                        Float.intBitsToFloat(params[6]),
                        Float.intBitsToFloat(params[2]),
                        Float.intBitsToFloat(params[7])
                    };

                    Vector2f col0 = new Vector2f(rotation_matrix[0], rotation_matrix[1]);
                    Vector2f col1 = new Vector2f(rotation_matrix[2], rotation_matrix[3]);

                    scale = new Vector2f(col0.length(), col1.length());

                    col0.div(scale.x);
                    col1.div(scale.y);

                    rotation = (float) Math.acos(col0.x);

                    offset = new Vector2f(rotation_matrix[4], rotation_matrix[5]);

                    xml.startTag("RotationMatrix");
                        xml.addTag("Row", String.format("%f, %f, %f", rotation_matrix[0], rotation_matrix[2], rotation_matrix[4]));
                        xml.addTag("Row", String.format("%f, %f, %f", rotation_matrix[1], rotation_matrix[3], rotation_matrix[5]));
                    xml.endTag();
                }

                offset.x = (float) (offset.x + scale.y * Math.sin(rotation));
                offset.y = (float) (1.0f - offset.y - scale.y * Math.cos(rotation));

                xml.startTag("Scale");
                    xml.addTag("x", scale.x);
                    xml.addTag("y", scale.y);
                xml.endTag();

                xml.startTag("Offset");
                    xml.addTag("x", offset.x);
                    xml.addTag("y", offset.y);
                xml.endTag();

                xml.addTag("Rotation", String.format("%.1fdeg", Math.toDegrees(rotation)));

                xml.addTag("Channel", channel);
                xml.addTag("Texture", texunit);

                xml.endTag();

                break;
            }
            case BoxType.THING_COLOR:
            {
                xml.addTag("Color", "type=\"Thing\"", null);
                break;
            }
            case BoxType.COLOR:
            {
                float[] rgba = new float[] 
                {
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]),
                    Float.intBitsToFloat(params[2]),
                    Float.intBitsToFloat(params[3]),
                };

                String hex = String.format("#%02X%02X%02X%02X", 
                    (int)(rgba[0] * 255.0f),
                    (int)(rgba[1] * 255.0f),
                    (int)(rgba[2] * 255.0f),
                    (int)(rgba[3] * 255.0f)
                );

                xml.startTag("Color", "type=\"RGBA\"");
                    xml.addTag("Float4", String.format("%f, %f, %f, %f", rgba[0], rgba[1], rgba[2], rgba[3]));
                    xml.addTag("Hex", hex);
                xml.endTag();

                break;
            }

            case BoxType.CONSTANT:
            {
                xml.addTag("Float", params[0]);
                break;
            }

            case BoxType.CONSTANT2:
            {
                xml.addTag("Float2", String.format("%f, %f", params[0], params[1]));
                break;
            }

            case BoxType.CONSTANT3:
            {
                xml.addTag("Float3", String.format("%f, %f", params[0], params[1], params[2]));
                break;
            }

            case BoxType.CONSTANT4:
            {
                xml.addTag("Float4", String.format("%f, %f, %f, %f", params[0], params[1], params[2], params[3]));
                break;
            }

            case BoxType.MULTIPLY_ADD:
            {
                xml.startTag("MultiplyAdd");
                    xml.startTag("Value");
                        material.getBoxConnectedToPort(this, 0).toXML(material, xml);
                    xml.endTag();
                    xml.addTag("Multiplier", Float.intBitsToFloat(params[0]));
                    xml.addTag("Addend", Float.intBitsToFloat(params[1]));
                xml.endTag();

                break;
            }

            case BoxType.MULTIPLY:
            {
                xml.startTag("Multiply");
                    xml.startTag("Value");
                        material.getBoxConnectedToPort(this, 0).toXML(material, xml);
                    xml.endTag();
                    xml.startTag("Value");
                        material.getBoxConnectedToPort(this, 1).toXML(material, xml);
                    xml.endTag();
                xml.endTag();
                break;
            }

            case BoxType.ADD:
            {
                xml.startTag("Add");
                    xml.startTag("Value");
                        material.getBoxConnectedToPort(this, 0).toXML(material, xml);
                    xml.endTag();
                    xml.startTag("Value");
                        material.getBoxConnectedToPort(this, 1).toXML(material, xml);
                    xml.endTag();
                xml.endTag();
                break;
            }

            default:
            {
                xml.startTag("Unsupported");
                    xml.addTag("Message", "This node isn\'t supported, go yell at Aidan");
                xml.endTag();
            }
        }
    }
}
