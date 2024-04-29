package cwlib.enums;

public class BrdfPort
{
    public static final int DIFFUSE = 0;
    public static final int ALPHA_CLIP = 1;
    public static final int SPECULAR = 2;
    public static final int BUMP = 3;
    public static final int GLOW = 4;
    public static final int REFLECTION = 6;
    public static final int UNKNOWN = 7; // 7, just adds tex * ambcol, to final color

    // 169
    public static final int ANISO = 170;
    public static final int TRANS = 171;
    public static final int COLOR_CORRECTION = 172; // ramp
    public static final int FUZZ = 173;
    public static final int BRDF_REFLECTANCE = 174;
    public static final int TOON_RAMP = 175;
}