package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * An OpenGL primitive type,
 * refer to: https://www.khronos.org/opengl/wiki/Primitive
 */
public enum PrimitiveType implements ValueEnum<Byte> {
    GL_POINTS(1),
    GL_LINES(2),
    GL_LINE_LOOP(3),
    GL_LINE_STRIP(4),
    GL_TRIANGLES(5),
    GL_TRIANGLE_STRIP(6),
    GL_TRIANGLE_FAN(7),
    GL_LINES_ADJACENCY(8),
    GL_LINE_STRIP_ADJACENCY(9),
    GL_TRIANGLES_ADJACENCY(10),
    GL_TRIANGLE_STRIP_ADJACENCY(11);

    private final byte value;
    private PrimitiveType(int value) {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue() { return this.value; }

    public static PrimitiveType fromValue(int value) {
        for (PrimitiveType type : PrimitiveType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
