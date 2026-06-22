package cwlib.enums;

/**
 * Types of material box nodes, incomplete list.
 */
public class BoxType
{
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
    public static final int FRESNEL = 18;
    public static final int EXPONENT = 19;

    public static final int BL_GENERIC = 128;
    public static final int BL_MAPPING = 129;
    public static final int BL_UV_MAP = 130;
    public static final int BL_TEXTURE_SAMPLE = 131;
    public static final int BL_MIX = 132;
    public static final int BL_EXTENDED_INFO = 133;
    public static final int BL_DATA_BLOB = 134;
    public static final int BL_BLENDER_INFO = 135;
    public static final int BL_INVERT_COLOR = 136;
    public static final int BL_TEXTURE_COORDINATE = 137;
    public static final int BL_SEPARATE_XYZ = 138;
    public static final int BL_NORMAL_MAP = 139;
    public static final int BL_VECTORMATH = 140;
    public static final int BL_MATH = 141;
    public static final int BL_VALUE = 142;
}