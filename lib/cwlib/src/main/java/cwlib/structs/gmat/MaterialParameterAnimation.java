package cwlib.structs.gmat;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import org.joml.Vector4f;

public class MaterialParameterAnimation implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;
    public static final int NAME_SIZE = 0x3;

    public Vector4f baseValue;
    public Vector4f[] keys;
    private String name = "";
    public byte componentsAnimated;

    @Override
    public void serialize(Serializer serializer)
    {
        baseValue = serializer.v4(baseValue);

        if (serializer.isWriting())
        {
            MemoryOutputStream stream = serializer.getOutput();
            componentsAnimated = calculateComponentsAnimated();
            if (keys == null || keys.length == 0) stream.i32(0);
            else
            {
                int count = getNumberOfComponentsAnimated() * keys.length;
                stream.i32(count);
                for (int i = 0; i < 4; ++i)
                {
                    if ((componentsAnimated & (1 << i)) != 0)
                    {
                        for (Vector4f key : keys) stream.f32(key.get(i));
                    }
                }
            }

            stream.i32(3);
            stream.str(name, 3);
            stream.i8(componentsAnimated);
        }
        else
        {
            MemoryInputStream stream = serializer.getInput();
            float[] components = stream.floatarray();
            name = stream.str(stream.i32());
            componentsAnimated = stream.i8();
            if (components.length == 0) return;

            int numKeys = components.length / getNumberOfComponentsAnimated();
            keys = new Vector4f[numKeys];
            for (int i = 0; i < keys.length; ++i) keys[i] = new Vector4f(baseValue);

            int offset = 0;
            for (int i = 0; i < 4; ++i)
            {
                if ((componentsAnimated & (1 << i)) != 0)
                {
                    for (Vector4f key : keys) key.setComponent(i, components[offset++]);
                }
            }
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = MaterialParameterAnimation.BASE_ALLOCATION_SIZE;
        if (this.keys != null)
            size += (this.keys.length * 0x10);
        return size;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        if (name == null)
            throw new NullPointerException("Name cannot be null!");
        if (name.length() > NAME_SIZE)
            throw new IllegalArgumentException("Name cannot be longer than 3 characters!");
        this.name = name;
    }

    public int getNumberOfComponentsAnimated()
    {
        int count = 0;
        if ((componentsAnimated & 1) != 0) count++;
        if ((componentsAnimated & 2) != 0) count++;
        if ((componentsAnimated & 4) != 0) count++;
        if ((componentsAnimated & 8) != 0) count++;
        return count;
    }

    public boolean IsComponentAnimated(int c)
    {
        if (keys == null) return false;
        for (Vector4f key : keys)
        {
            if (key.get(c) != baseValue.get(c))
                return true;
        }
        return false;
    }

    private byte calculateComponentsAnimated()
    {
        byte flags = 0;
        if (IsComponentAnimated(0)) flags |= 0x1;
        if (IsComponentAnimated(1)) flags |= 0x2;
        if (IsComponentAnimated(2)) flags |= 0x4;
        if (IsComponentAnimated(3)) flags |= 0x8;
        return flags;
    }
}
