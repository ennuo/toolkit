package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.decals.Decal;
import cwlib.structs.things.components.decals.PaintControlPoint;
import cwlib.types.data.ResourceDescriptor;
import cwlib.structs.inventory.EyetoyData;

/**
 * Part that contains all the stickers
 * placed on an object.
 */
public class PStickers implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    /**
     * Decals that are on the primary thing.
     */
    public Decal[] decals;

    /**
     * Decals on specific costume pieces.
     */
    public Decal[][] costumeDecals = new Decal[14][];

    /**
     * Paint control data, depreciated.
     */
    @GsonRevision(min = 0x158, max = 0x3ba)
    public PaintControlPoint[] paintControl;

    /**
     * Eyetoy related information for decal placed.
     */
    @GsonRevision(min = 0x15d)
    public EyetoyData[] eyetoyData;

    public PStickers() { }

    public PStickers(ResourceDescriptor sticker)
    {
        this.decals = new Decal[] { new Decal(sticker) };
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        decals = serializer.array(decals, Decal.class);

        if (!serializer.isWriting())
            costumeDecals = new Decal[serializer.getInput().i32()][];
        else serializer.getOutput().i32(costumeDecals.length);
        for (int i = 0; i < costumeDecals.length; ++i)
            costumeDecals[i] = serializer.array(costumeDecals[i], Decal.class);

        if (version >= 0x158 && version <= 0x3ba)
            paintControl = serializer.array(paintControl,
                PaintControlPoint.class);

        if (version >= 0x15d)
            eyetoyData = serializer.array(eyetoyData, EyetoyData.class);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.decals != null)
            size += (this.decals.length * Decal.BASE_ALLOCATION_SIZE);
        for (Decal[] decals : this.costumeDecals)
            if (decals != null)
                size += (decals.length * Decal.BASE_ALLOCATION_SIZE);
        if (this.paintControl != null)
            size += (this.paintControl.length * PaintControlPoint.BASE_ALLOCATION_SIZE);
        if (this.eyetoyData != null)
            size += (this.eyetoyData.length * EyetoyData.BASE_ALLOCATION_SIZE);
        return size;
    }
}