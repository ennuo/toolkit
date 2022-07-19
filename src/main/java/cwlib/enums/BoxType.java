package cwlib.enums;

/**
 * Types of material box nodes, incomplete list.
 */
public class BoxType {
    public static final int OUTPUT = 0;
    public static final int TEXTURE_SAMPLE = 1;
    public static final int THING_COLOR = 2; // u32 (8)?
    public static final int COLOR = 3;
    // public static final int TYPE_4 = 4; // f32 (-4), passed into mix parameter of texture sample usually? only used in like 3 shaders, specifically the ones related to paintinator meter bars
    // 5 is not used in any shader
    // 6 is not used in any shader
    // 7 is not used in any shader
    // 8 is not used in any shader
    // 9 is not used in any shader
    public static final int MULTIPLY = 10;
    // public static final int TYPE_11 = 11; // only used in sticker switch?, takes in two inputs
    public static final int SUBTRACT = 12;
}
