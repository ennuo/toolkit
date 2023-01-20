package configurations;

import java.io.File;

/**
 * Flags controlling whether or not certain
 * features should be enabled or not, usually
 * for in-development features.
 */
public class ApplicationFlags {
    /**
     * Enables Ghostbusters resource loading.
     */
    public static boolean ENABLE_GHOSTBUSTERS = false;

    /**
     * Path to PS3 shader compiler executable, you'll have to
     * get this from a PS3 SDK yourself.
     */
    public static File SCE_CGC_EXECUTABLE = 
        new File(Config.jarDirectory, "sce/sce-cgc.exe");

    /**
     * Path to CG shader stripper, optional.
     */
    public static File SCE_CGC_STRIP_EXECUTABLE =
        new File(Config.jarDirectory, "sce/sce-cgcstrip.exe");
    
    /**
     * Path to PS4 shader compiler executable, you'll have to
     * get this from a PS4 SDK yourself.
     */
    public static File SCE_PSSL_EXECUTABLE = 
        new File(Config.jarDirectory, "sce/orbis-wave-psslc.exe");

    
    /**
     * Whether or not Toolkit can compile PS3 shaders, requires
     * SCE_CGC_EXECUTABLE to be set.
     */
    public static boolean CAN_COMPILE_CELL_SHADERS = false;

    /**
     * Whether or not Toolkit can compile PS4 shaders, requires
     * SCE_PSSL_EXECUTABLE to be set.
     */
    public static boolean CAN_COMPILE_ORBIS_SHADERS = false;
    
    /**
     * Whether or not Toolkit is running on Windows
     */
    public static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    
    /**
     * 3D is only supported on Windows (and maybe Linux)
     */
    public static boolean CAN_USE_3D = !IS_WINDOWS;

    static {
        if (SCE_PSSL_EXECUTABLE != null)
            ApplicationFlags.CAN_COMPILE_ORBIS_SHADERS = SCE_PSSL_EXECUTABLE.exists();
        if (SCE_CGC_EXECUTABLE != null)
            ApplicationFlags.CAN_COMPILE_CELL_SHADERS = SCE_CGC_EXECUTABLE.exists();
    }
}
