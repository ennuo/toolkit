package cwlib.enums;

/**
 * Types of material box nodes, incomplete list.
 */
public class BoxType {
    public static final int OUTPUT = 0;
    public static final int TEXTURE_SAMPLE = 1;
    public static final int THING_COLOR = 2; // u32 (8)?
    public static final int COLOR = 3;
    public static final int CONSTANT = 4;
    public static final int CONSTANT2 = 5;
    public static final int CONSTANT3 = 6;
    public static final int CONSTANT4 = 7;
    // 8 is not used in any shader
    public static final int MULTIPLY_ADD = 9; // Multiply by params[0], then add by params[1].
    public static final int MULTIPLY = 10;
    public static final int ADD = 11;
    public static final int MIX = 12;
    public static final int MAKE_FLOAT2 = 13;
    public static final int MAKE_FLOAT3 = 14;
    public static final int MAKE_FLOAT4 = 15;
    public static final int BLEND = 16;
    // 17, something with fur
    // 18 ??? 
    public static final int EXPONENT = 19;

    public static final int PBR = 0x80;
    public static final int VERTEX_TANGENTS = 0x81;
    public static final int TEXTURE_COORDINATE = 0x82;
    public static final int VIEW_VECTOR = 0x83;
    public static final int NORMALS = 0x84;
    public static final int NORMAL_MAP = 0x85;
    public static final int BUMP_MAP = 0x86;
    public static final int MATH = 0x89;
}