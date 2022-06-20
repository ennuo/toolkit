package toolkit.configurations;

/**
 * Flags controlling whether or not certain
 * features should be enabled or not, usually
 * for in-development features.
 */
public class ApplicationFlags {
    /**
     * Enables load option for big/local profiles combined.
     * Mostly non-functional, will populate entries, but not
     * much else.
     */
    public static boolean ENABLE_NEW_SAVEDATA = false;

    /**
     * Enables Ghostbusters resource loading.
     */
    public static boolean ENABLE_GHOSTBUSTERS = false;

    /**
     * Path to shader compiler executable, you'll have to
     * get this from a PS3 SDK yourself.
     */
    public static String SCE_CGC_EXECUTABLE = "";
}
