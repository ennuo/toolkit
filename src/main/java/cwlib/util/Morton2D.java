package cwlib.util;

public class Morton2D {
    public static int decodeX(int x) {
        return Morton2D.compact1By1(x >> 0);
    }
    
    public static int decodeY(int y) {
        return Morton2D.compact1By1(y >> 1);
    }
    
    private static int compact1By1(int x) {
        x &= 0x55555555;
        x = (x ^ (x >> 1)) & 0x33333333;
        x = (x ^ (x >> 2)) & 0x0f0f0f0f;
        x = (x ^ (x >> 4)) & 0x00ff00ff;
        x = (x ^ (x >> 8)) & 0x0000ffff;
        return x;
    }
}
