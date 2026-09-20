package scelib;

import java.io.File;

import cwlib.CwlibConfiguration;

public class SeImageUtil 
{
    private static boolean loaded = false;
    private static boolean ready = false;

    public static void LoadLibraries()
    {
        if (!loaded)
        {
            var root = new File(CwlibConfiguration.JAR_DIRECTORY, "/sce/");
            final String[] files = new String[] { "SceLib.Images.dll" };

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
        SeImageUtil.LoadLibraries();
        return ready;
    }

    static
    {
        SeImageUtil.LoadLibraries();
    }
    
    public static final int DXGI_FORMAT_R8G8B8A8_UNORM = 28;
    public static final int DXGI_FORMAT_BC3_UNORM = 77;
    public static final int DXGI_FORMAT_B8G8R8A8_UNORM = 87;

    public static native byte[] resize(byte[] imageData, int format, int width, int height, int targetWidth, int targetHeight);
    public static native byte[] compress(byte[] imageData, int sourceFormat, int targetFormat, int width, int height);
}
