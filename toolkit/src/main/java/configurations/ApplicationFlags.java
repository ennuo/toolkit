package configurations;

import cwlib.CwlibConfiguration;

/**
 * Flags controlling whether or not certain
 * features should be enabled or not, usually
 * for in-development features.
 */
public class ApplicationFlags
{
    /**
     * Enables Ghostbusters resource loading.
     */
    public static boolean ENABLE_GHOSTBUSTERS = false;

    /**
     * 3D is only supported on Windows (and maybe Linux)
     */
    public static boolean CAN_USE_3D = CwlibConfiguration.IS_WINDOWS;

    /**
     * Whether or not Alear server operations are allowed.
     */
    public static boolean ALEAR_INTEGRATION = false;
}
