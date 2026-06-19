package cwlib.util;

public class BinaryPrimitives 
{
    public static final int readInt32BigEndian(byte[] source, int offset)
    {
        return (source[offset + 0] & 0xFF) << 24 |
               (source[offset + 1] & 0xFF) << 16 |
               (source[offset + 2] & 0xFF) << 8 |
               (source[offset + 3] & 0xFF);
    }

    public static final int readInt16BigEndian(byte[] source, int offset)
    {
        return (source[offset + 0] & 0xFF) << 8 |
               (source[offset + 1] & 0xFF);
    }

    public static final void writeInt32BigEndian(byte[] source, int offset, int value)
    {
        source[offset + 0] = (byte)(value >>> 24);
        source[offset + 1] = (byte)(value >>> 16);
        source[offset + 2] = (byte)(value >>> 8);
        source[offset + 3] = (byte)(value & 0xff);
    }

    public static final void writeInt64LittleEndian(byte[] source, int offset, long value)
    {
        source[offset + 0] = (byte)(value);
        source[offset + 1] = (byte)(value >>> 8);
        source[offset + 2] = (byte)(value >>> 16);
        source[offset + 3] = (byte)(value >>> 24);
        source[offset + 4] = (byte)(value >>> 32);
        source[offset + 5] = (byte)(value >>> 40);
        source[offset + 6] = (byte)(value >>> 48);
        source[offset + 7] = (byte)(value >>> 56);
    }

    public static final void writeInt64BigEndian(byte[] source, int offset, long value)
    {
        source[offset + 0] = (byte)(value >>> 56);
        source[offset + 1] = (byte)(value >>> 48);
        source[offset + 2] = (byte)(value >>> 40);
        source[offset + 3] = (byte)(value >>> 32);
        source[offset + 4] = (byte)(value >>> 24);
        source[offset + 5] = (byte)(value >>> 16);
        source[offset + 6] = (byte)(value >>> 8);
        source[offset + 7] = (byte)(value);
    }


    public static final void writeInt16BigEndian(byte[] source, int offset, short value)
    {
        source[offset + 0] = (byte)(value >>> 8);
        source[offset + 1] = (byte)(value & 0xff);
    }

    public static final void writeInt16LittleEndian(byte[] source, int offset, short value)
    {
        source[offset + 0] = (byte)(value & 0xff);
        source[offset + 1] = (byte)(value >>> 8);
    }

    public static final void writeInt24BigEndian(byte[] source, int offset, int value)
    {
        source[offset + 0] = (byte)(value >>> 16);
        source[offset + 1] = (byte)(value >>> 8);
        source[offset + 2] = (byte)(value & 0xff);
    }

    public static final void writeInt24LittleEndian(byte[] source, int offset, int value)
    {
        source[offset + 0] = (byte)(value & 0xff);
        source[offset + 1] = (byte)(value >>> 8);
        source[offset + 2] = (byte)(value >>> 16);
    }

    public static final void writeInt32LittleEndian(byte[] source, int offset, int value)
    {
        source[offset + 0] = (byte)(value & 0xFF);
        source[offset + 1] = (byte)(value >>> 8);
        source[offset + 2] = (byte)(value >>> 16);
        source[offset + 3] = (byte)(value >>> 24);
    }

    public static final void writeSingleBigEndian(byte[] source, int offset, float f)
    {
        int value = Float.floatToIntBits(f);
        
        source[offset + 0] = (byte)(value >>> 24);
        source[offset + 1] = (byte)(value >>> 16);
        source[offset + 2] = (byte)(value >>> 8);
        source[offset + 3] = (byte)(value & 0xff);
    }
}
