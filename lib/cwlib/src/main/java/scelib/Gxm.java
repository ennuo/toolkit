package scelib;

import java.io.File;

import cwlib.CwlibConfiguration;

public final class Gxm
{
    private static boolean loaded = false;
    private static boolean ready = false;

    public static void LoadLibraries()
    {
        if (!loaded)
        {
            var root = new File(CwlibConfiguration.JAR_DIRECTORY, "/sce/");
            final String[] files = new String[] { "psp2cgc.dll", "gxt_conversion.dll", "SceLib.dll" };

            int err = 0;
            for (var dll : files)
            {
                var path = new File(root, dll);
                if (path.exists())
                    System.load(path.getAbsolutePath());
                else
                    err++;

            }
            
            loaded = true;
            ready = err == 0;
        }

    }

    public static boolean IsReady()
    {
        Gxm.LoadLibraries();
        return ready;
    }

    protected Gxm()
    {
        throw new UnsupportedOperationException();
    }

    public static native byte[] compile(String source);

    public static native byte[] convert(byte[] ddsFileData);
}