package cwlib.io.streams;

import java.util.Arrays;

import cwlib.enums.PackType;

public class BitOutputIterator 
{
    private byte[] data;
    private int position;

    public BitOutputIterator()
    {
        data = new byte[16];
    }

    private void ensureCapacity(int bits)
    {
        int bytes = ((position + bits) + 8) >>> 3;
        if (data.length >= bytes) return;
        
        byte[] byteData = new byte[bytes * 2];
        System.arraycopy(data, 0, byteData, 0, data.length);
        data = byteData;
    }

    public void writeBit(int b)
    {
        ensureCapacity(position + 1);
        data[position >>> 3] |= (b & 1) << (position & 7);
        position += 1;
    }

    public void writeShort(short v) { writeShort(v, PackType.DEFAULT, 16); }
    public void writeShort(short v, PackType type) { writeShort(v, type, 16); }
    public void writeShort(short v, PackType type, int bits)
    {
        switch (type)
        {
            case MAX_BITS:
            {
                for (int i = 0; i < bits; ++i)
                    writeBit((v >>> i) & 1);
                break;
            }
            case USUALLY_SMALL_OFTEN_ZERO:
            {
                writeBit(v != 0 ? 1 : 0);
                if (v == 0) return;
                writeByte((byte)(v & 0xff));
                writeBit(v >>> 8);
                if ((v >>> 8) == 0) return;
                writeByte((byte)(v >>> 8));

                break;
            }
            default:
            {
                throw new RuntimeException("Unsupported pack type!");
            }
        }
    }

    public void writeByte(byte b) { writeByte(b, PackType.DEFAULT, 8); }
    public void writeByte(byte b, PackType type) { writeByte(b, type, 8); }
    public void writeByte(byte b, PackType type, int bits)
    {
        switch (type)
        {
            case OFTEN_ZERO:
            {
                writeBit(b != 0 ? 1 : 0);
                if (b != 0)
                    writeByte(b, PackType.DEFAULT);
                break;
            }
            default:
            {
                if (position % 8 == 0)
                {
                    data[position >>> 3] = b;
                    position += 8;
                    return;
                }

                for (int i = 0; i < bits; ++i)
                    writeBit((b >>> i) & 1);
                
                break;
            }
        }
    }
    
    public byte[] flush()
    {
        return Arrays.copyOf(data, position >>> 3);
    }
}
