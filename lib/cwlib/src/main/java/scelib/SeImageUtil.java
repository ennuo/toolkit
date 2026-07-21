package scelib;

public class SeImageUtil 
{
    static
    {
        Gxm.LoadLibraries();
    }
    
    public static final int DXGI_FORMAT_R8G8B8A8_UNORM = 28;
    public static final int DXGI_FORMAT_BC3_UNORM = 77;
    public static final int DXGI_FORMAT_B8G8R8A8_UNORM = 87;

    public static native byte[] resize(byte[] imageData, int format, int width, int height, int targetWidth, int targetHeight);
    public static native byte[] compress(byte[] imageData, int sourceFormat, int targetFormat, int width, int height);
}
