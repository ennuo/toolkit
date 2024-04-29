package cwlib;

import java.io.File;

public class CwlibConfiguration
{
    public static File JAR_DIRECTORY;

    static
    {
        try
        {
            JAR_DIRECTORY =
                new File(CwlibConfiguration.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        }
        catch (Exception e)
        {
            JAR_DIRECTORY = new File("./");
        }
    }

    /**
     * Path to PS3 shader compiler executable, you'll have to
     * get this from a PS3 SDK yourself.
     */
    public static File SCE_CGC_EXECUTABLE =
        new File(JAR_DIRECTORY, "sce/sce-cgc.exe");

    /**
     * Path to CG shader stripper, optional.
     */
    public static File SCE_CGC_STRIP_EXECUTABLE =
        new File(JAR_DIRECTORY, "sce/sce-cgcstrip.exe");

    /**
     * Path to the texconv executable, used in Windows instead of default DDS conversion if
     * available.
     */
    public static File TEXCONV_EXECUTABLE =
        new File(JAR_DIRECTORY, "bin/texconv.exe");

    /**
     * Path to PS4 shader compiler executable, you'll have to
     * get this from a PS4 SDK yourself.
     */
    public static File SCE_PSSL_EXECUTABLE =
        new File(JAR_DIRECTORY, "sce/orbis-wave-psslc.exe");


    /**
     * Whether Toolkit can compile PS3 shaders, requires
     * SCE_CGC_EXECUTABLE to be set.
     */
    public static boolean CAN_COMPILE_CELL_SHADERS = false;

    /**
     * Whether Toolkit can compile PS4 shaders, requires
     * SCE_PSSL_EXECUTABLE to be set.
     */
    public static boolean CAN_COMPILE_ORBIS_SHADERS = false;

    /**
     * Whether Toolkit is running on Windows
     */
    public static boolean IS_WINDOWS =
        System.getProperty("os.name").toLowerCase().contains("win");

    static
    {
        if (SCE_PSSL_EXECUTABLE != null)
            CAN_COMPILE_ORBIS_SHADERS = SCE_PSSL_EXECUTABLE.exists();
        if (SCE_CGC_EXECUTABLE != null)
            CAN_COMPILE_CELL_SHADERS = SCE_CGC_EXECUTABLE.exists();
    }
}
