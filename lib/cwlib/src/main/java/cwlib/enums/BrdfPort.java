package cwlib.enums;

public class BrdfPort
{
    public static final int DIFFUSE = 0;
    public static final int OPACITY = 1; // OPACITY
    public static final int SPECULAR = 2;
    public static final int BUMP = 3;
    public static final int SELF_ILLUMINATION = 4; // SELF ILLUMINATION
    public static final int RIM_LIGHT = 5;
    public static final int REFLECT = 6;
    public static final int UNKNOWN = 7; // 7, just adds tex * ambcol, to final color // ambient color map

    // 169
    public static final int ANISO = 170;
    public static final int TRANS = 171;
    public static final int COLOR_CORRECTION = 172; // ramp
    public static final int FUZZ = 173;
    public static final int BRDF_REFLECTANCE = 174;
    public static final int TOON_RAMP = 175;
}