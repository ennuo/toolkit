package cwlib.structs.things.parts;

import java.nio.charset.StandardCharsets;

import com.google.gson.annotations.JsonAdapter;

import cwlib.io.Serializable;
import cwlib.io.gson.ScriptNameSerializer;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

/**
 * Identifies this Thing in the game world by a name accessible by scripts.
 * Additionally used to serialize extra instance data in modified versions of the game.
 */
@JsonAdapter(ScriptNameSerializer.class)
public class PScriptName implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    private byte[] rawData = {};

    public PScriptName() {}
    public PScriptName(String name) { setName(name); }


    public int getLength()
    {
        for (int i = 0; i < rawData.length; ++i)
        {
            if (rawData[i] == 0)
                return i;
        }
        
        return rawData.length;
    }

    public int getCapacity()
    {
        return rawData.length;
    }

    public String getName()
    {
        if (rawData.length == 0) return "";
        return new String(rawData, 0, getLength(), StandardCharsets.US_ASCII);
    }

    public void setName(String name)
    {
        rawData = name.getBytes(StandardCharsets.US_ASCII);
    }
    
    public byte[] getData() { return rawData; }
    public void setData(byte[] data) { rawData = data; }

    @Override public void serialize(Serializer serializer)
    {
        if (serializer.isWriting())
        {
            MemoryOutputStream stream = serializer.getOutput();
            stream.s32(rawData.length);
            stream.bytes(rawData);
            return;
        }

        MemoryInputStream stream = serializer.getInput();
        rawData = stream.bytes(stream.s32());
    }

    @Override
    public int getAllocatedSize()
    {
        return PScriptName.BASE_ALLOCATION_SIZE + rawData.length;
    }
}
