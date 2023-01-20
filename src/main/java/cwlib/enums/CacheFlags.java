package cwlib.enums;

/**
 * Cache flags in RShaderCache
 */
public class CacheFlags {
    public static final int NONE = 0;
    
    public static final int DIFFUSE = (1 << 0);
    public static final int SPECULAR = (1 << 1);
    public static final int BUMP = (1 << 2);
    public static final int GLOW = (1 << 3);
    public static final int REFLECTION = (1 << 4);
    public static final int ALPHA_CLIP = (1 << 5);
    public static final int DIRT = (1 << 9);
    @Deprecated public static final int PROCEDURAL = (1 << 6);

    public static final int DIFFUSE_COLOR = CacheFlags.DIFFUSE | (1 << 7);
    public static final int SPECULAR_COLOR = CacheFlags.SPECULAR | (1 << 8);

}
