package cwlib.enums;

/**
 * Flags that contain information about resource descriptors,
 * generally they aren't used often, if at all in serialization,
 * but they're still stored anyway.
 */
public final class ResourceFlags {
    public static final int NONE = 0x0;
    public static final int REF_COUNT_DIRTY = 0x2;
    public static final int CONTAINS_EYETOY = 0x80;
    public static final int DONT_SWIZZLE = 0x100;
    public static final int VOLTEX = 0x200;
    public static final int NOSRGB_TEX = 0x400;
    public static final int BUMP_TEX = 0x800;
    public static final int TEMPORARY = 0x2000;
    public static final int UNSHARED = 0x4000;
    public static final int MAX_MIPS_128 = 0x10000;
}
