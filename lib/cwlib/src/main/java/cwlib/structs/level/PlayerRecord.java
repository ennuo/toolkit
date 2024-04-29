package cwlib.structs.level;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.NetworkPlayerID;

public class PlayerRecord implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x800;

    private NetworkPlayerID[] playerIDs = new NetworkPlayerID[32];
    private int[] playerNumbers = new int[32];
    private int offset;

    public PlayerRecord()
    {
        for (int i = 0; i < this.playerNumbers.length; ++i)
            playerNumbers[i] = -1;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        playerIDs = serializer.array(playerIDs, NetworkPlayerID.class);
        playerNumbers = serializer.intarray(playerNumbers);
        offset = serializer.i32(offset);
    }

    @Override
    public int getAllocatedSize()
    {
        return PlayerRecord.BASE_ALLOCATION_SIZE;
    }

    public NetworkPlayerID[] getPlayerIDs()
    {
        return this.playerIDs;
    }

    public int[] getPlayerNumbers()
    {
        return this.playerNumbers;
    }

    public int getOffset()
    {
        return this.offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }
}
