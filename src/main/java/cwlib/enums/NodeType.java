package cwlib.enums;

import java.util.ArrayList;

import cwlib.structs.gmat.MaterialBox;

public enum NodeType {
    TEXTURE_SAMPLE(1, "Image Texture", "Texture", new float[] { 1.0f, 1.0f }),
    EDITOR_COLOR(2, "Editor Color", "Input"),
    RGBA(3, "RGBA", "Input", new float[] { 1.0f, 1.0f, 1.0f, 1.0f }),
    VALUE(4, "Value", "Input"),
    VALUE2(5, "Value2", "Input", true),
    VALUE3(6, "Value3", "Input", true),
    VALUE4(7, "Value4", "Input", true),

    MULTIPLY_ADD(9, "Multiply Add", "Math", true),
    MULTIPLY(10, "Multiply", "Math", true),
    ADD(11, "Add", "Math", true),
    
    MIX(12, "MixRGB", "Color"),

    COMBINE_XY(13, "Combine XY", "Converter", true),
    COMBINE_XYZ(14, "Combine XYZ", "Converter", true),
    COMBINE_XYZW(15, "Combine XYZW", "Converter", true),

    BLEND(16, "Blend", "Color"),

    EXPONENT(19, "Power", "Math", true),

    // Custom Nodes
    
    VERTEX_TANGENTS(0x81, "Vertex Tangents", "Input"),
    TEXTURE_COORDINATE(0x82, "Texture Coordinate", "Input"),
    VIEW_VECTOR(0x83, "Camera View Vector", "Input"),
    VERTEX_NORMAL(0x84, "Normals", "Input"),

    NORMAL_MAP(0x85, "Normal Map", "Vector"),
    BUMP_MAP(0x86, "Bump Map", "Vector"),

    COMBINE_RGB(0x87, "Combine RGB", "Converter"),

    NOISE_TEXTURE(0x88, "Noise Texture", "Texture"),


    // 0 = ADD
    // 1 = SUBTRACT
    // 2 = MULTIPLY
    // 3 = DIVIDE
    // 4 = MULTIPLY ADD
    // 5 = POWER
    // 6 = LOGARITHM
    // 7 = SQUARE ROOT
    // 8 = INVERSE SQUARE ROOT
    // 9 = ABSOLUTE
    // 10 = EXPONENT
    // 11 = MINIMUM
    // 12 = MAXIMUM
    // 13 = SIN
    // 14 = COS
    // 15 = TAN
    // 16 = ARCSIN
    // 17 = ARCOS
    // 18 = ARCTAN
    // 19 = ARCTAN2
    // 20 = HYPERBOLIC SINE
    // 21 = HYPERBOLIC COSINE
    // 22 = HYPERBOLIC TANGENT

    // params[0->5] inputs
    // params[6] = type
    // params[7] = saturate
    MATH(0x89, "Math", "Converter");

    public static final String[] MATH_MODES = {
        "Add",
        "Subtract",
        "Multiply",
        "Divide",
        "Multiply Add",
        "Power",
        // "Logarithm",
        // "Square Root",
        // "Inverse Square Root",
        // "Absolute",
        // "Exponent",
        // "Minimum",
        // "Maximum",
        // "Sine",
        // "Cosine",
        // "Tangent",
        // "Arcsine",
        // "Arccosine",
        // "Arctangent",
        // "Arctan2",
        // "Hyperbolic Sine",
        // "Hyperbolic Cosine",
        // "Hyperbolic Tangent"
    };


    private final int value;
    private final String name;
    private final String folder;
    private final int[] parameters = new int[0x8];
    private final boolean isHidden;

    private NodeType(int value, String name, String folder) {
        this.value = value;
        this.name = name;
        this.folder = folder;
        this.isHidden = false;
    }

    private NodeType(int value, String name, String folder, boolean isHidden) {
        this.value = value;
        this.name = name;
        this.folder = folder;
        this.isHidden = isHidden;
    }

    private NodeType(int value, String name, String folder, int[] parameters) {
        this.value = value;
        this.name = name;
        this.folder = folder;
        for (int i = 0; i < parameters.length; ++i)
            this.parameters[i] = parameters[i];
        this.isHidden = false;
    }

    private NodeType(int value, String name, String folder, float[] parameters) {
        this.value = value;
        this.name = name;
        this.folder = folder;
        for (int i = 0; i < parameters.length; ++i)
            this.parameters[i] = Float.floatToIntBits(parameters[i]);
        this.isHidden = false;
    }

    public int getValue() { return this.value; }
    public String getName() { return this.name; }
    public String getFodler() { return this.folder; }
    public boolean isHidden() { return this.isHidden; }

    public MaterialBox create() {
        MaterialBox box = new MaterialBox();
        box.type = this.value;

        int[] parameters = box.getParameters();
        for (int i = 0; i < this.parameters.length; ++i)
            parameters[i] = this.parameters[i];
        
        return box;
    }

    public static NodeType[] getNodesInFolder(String folder) {
        ArrayList<NodeType> nodes = new ArrayList<>();
        for (NodeType type : NodeType.values()) {
            if (type.isHidden) continue;
            if (type.folder.equals(folder))
                nodes.add(type);
        }
        return nodes.toArray(NodeType[]::new);
    }

    public static NodeType getNode(int index) {
        for (NodeType type : NodeType.values()) {
            if (type.value == index)
                return type;
        }
        return null;
    }
}
