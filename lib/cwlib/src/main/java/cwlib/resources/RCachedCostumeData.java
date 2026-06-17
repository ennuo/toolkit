package cwlib.resources;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.types.SerializedResource;
import cwlib.types.archives.FartManyRO;
import cwlib.types.data.GUID;
import cwlib.types.data.Revision;
import cwlib.types.databases.FileDB;

public class RCachedCostumeData implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public static class CachedCostumeData implements Serializable
    {
        public static final int BASE_ALLOCATION_SIZE = 0x20;

        public GUID plan;
        public int titleKey;
        public int descriptionKey;
        public int type;
        public int subType;
        public int colour;
        public GUID icon;
        public int value;

        @Override public void serialize(Serializer serializer) 
        {
            plan = serializer.guid(plan);
            titleKey = serializer.i32(titleKey);
            descriptionKey = serializer.i32(descriptionKey);
            type = serializer.i32(type);
            subType = serializer.i32(subType);
            colour = serializer.i32(colour);
            icon = serializer.guid(icon);
            value = serializer.i32(value);
        }

        @Override public int getAllocatedSize() 
        {
            return BASE_ALLOCATION_SIZE;
        }
    };

    public ArrayList<CachedCostumeData> data = new ArrayList<>();

    @Override public void serialize(Serializer serializer)
    {
        data = serializer.arraylist(data, CachedCostumeData.class);
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
        serializer.struct(this, RCachedCostumeData.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.CACHED_COSTUME_DATA,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }

    @Override public int getAllocatedSize()
    {
        int size = RCachedCostumeData.BASE_ALLOCATION_SIZE;
        if (data != null) size += CachedCostumeData.BASE_ALLOCATION_SIZE * data.size();
        return size;
    }

    public static void main(String[] args) 
    {
        FileDB database = new FileDB("F:/cache/orbis/orbisguids.map");
        RCachedCostumeData ccd = new SerializedResource("C:/Users/Aidan/Desktop/cached_costume_data.ccd").loadResource(RCachedCostumeData.class);
        for (CachedCostumeData data : ccd.data)
        {
            System.out.println(database.get(data.plan).getPath());
        }

    }




    
}
