package cwlib.io.streams;

import cwlib.enums.PackType;

public class BitInputIterator 
{
    private byte[] data;
    private int position;

    public BitInputIterator(byte[] data)
    {
        this.data = data;
        this.position = 0;
    }

    public boolean readBit()
    {
        byte b = (byte)(data[position >>> 3] & 1 << (position & 7));
        position += 1;
        return b != 0;
    }

    public short readShort() { return readShort(PackType.DEFAULT, 16); }
    public short readShort(PackType type) { return readShort(type, 16); }
    public short readShort(PackType type, int bits)
    {
        short value = 0;

        switch (type)
        {
            case MAX_BITS:
            {
                for (int i = 0; i < bits; ++i)
                {
                    boolean b = readBit();
                    value |= (short)((b ? 1 : 0) << i);
                }

                break;
            }
            case USUALLY_SMALL_OFTEN_ZERO:
            {
                if (!readBit()) return value;
                value |= readByte();
                if (!readBit()) return value;
                value |= (readByte() & 0xff) << 8;
                
                break;
            }
            default:
            {
                throw new RuntimeException("Unsupported pack type!");
            }
        }

        return value;
    }

    public byte readByte() { return readByte(PackType.DEFAULT, 8); }
    public byte readByte(PackType type) { return readByte(type, 8); }
    public byte readByte(PackType type, int bits)
    {
        byte value = 0;

        switch (type)
        {
            case OFTEN_ZERO:
            {
                if (!readBit()) return value;
                return readByte(PackType.DEFAULT);
            }
            default:
            {
                if (position % 8 == 0)
                {
                    value = data[position >>> 3];
                    position += 8;
                    return value;
                }
        
                for (int i = 0; i < bits; ++i)
                {
                    boolean b = readBit();
                    value |= (byte)((b ? 1 : 0) << (i & 7));
                }
            }
        }

        return value;
    }

    public int getLength()
    {
        return data.length * 8;
    }

    public int getPosition()
    {
        return position;
    }

    public void setBytePosition(int pos)
    {
        position = pos * 8;
    }

    public int getBytePosition()
    {
        return position >>> 3;
    }

    public int getByteLength()
    {
        return data.length;
    }

    public int getBitsRemaining()
    {
        return (data.length * 8) - position;
    }
}
