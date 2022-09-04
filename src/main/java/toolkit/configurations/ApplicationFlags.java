package toolkit.configurations;

import java.io.File;

import cwlib.util.FileIO;

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
     * Path to shader compiler executable, you'll have to
     * get this from a PS3 SDK yourself.
     */
    public static File SCE_CGC_EXECUTABLE = new File("E:/util/sce-cgc.exe");

    /**
     * Whether or not Toolkit can compile shaders, requires
     * SCE_CGC_EXECUTABLE to be set.
     */
    public static boolean CAN_COMPILE_SHADERS = false;
    static {
        if (SCE_CGC_EXECUTABLE != null)
            ApplicationFlags.CAN_COMPILE_SHADERS = SCE_CGC_EXECUTABLE.exists();
    }
}
