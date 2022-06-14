package cwlib.util;

import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

public class TEA {
    private static int[] KEY = { 28773565, 345376726, 133778901, 282823840 };
    private static int DELTA = 0x9e3779b9;
    
    private static int MX(int sum, int y, int z, int p, int e) {
        return (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (KEY[p & 3 ^ e] ^ z);
    }
    
    private static int[] toUIntArray(byte[] data) {
        MemoryInputStream memory = new MemoryInputStream(data);
        int[] array = new int[memory.length / 4];
        for (int i = 0; i < memory.length / 4; ++i)
            array[i] = (int) memory.u32f();
        return array;
    }
    
    private static byte[] fromUIntArray(int[] data) {
        MemoryOutputStream memory = new MemoryOutputStream(data.length * 4);
        for (int i = 0; i < data.length; ++i)
            memory.u32f(data[i]);
        return memory.buffer;
    }
    
    public static byte[] encrypt(byte[] data) {
        int[] v = TEA.toUIntArray(data);
        
        int n = v.length - 1;

        if (n < 1) 
            return data;
        int p, q = 6 + 52 / (n + 1);
        int z = v[n], y, sum = 0, e;

        while (q-- > 0) {
            sum = sum + DELTA;
            e = sum >>> 2 & 3;
            for (p = 0; p < n; p++) {
                y = v[p + 1];
                z = v[p] += MX(sum, y, z, p, e);
            }
            y = v[0];
            z = v[n] += MX(sum, y, z, p, e);
        }
        
        return TEA.fromUIntArray(v);
    }
    
    public static byte[] decrypt(byte[] data) {
        int[] v = TEA.toUIntArray(data);
        int n = v.length - 1;

        if (n < 1) 
            return data;
        
        int p, q = 6 + 52 / (n + 1);
        int z, y = v[0], sum = q * DELTA, e;

        while (sum != 0) {
            e = sum >>> 2 & 3;
            for (p = n; p > 0; p--) {
                z = v[p - 1];
                y = v[p] -= MX(sum, y, z, p, e);
            }
            z = v[n];
            y = v[0] -= MX(sum, y, z, p, e);
            sum = sum - DELTA;
        }
        
        return TEA.fromUIntArray(v);
    }
    
}
