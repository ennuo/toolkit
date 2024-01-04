package cwlib.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.structs.things.components.decals.Decal;

public class RQuest implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public int state; 
    public ResourceDescriptor[] prizes; // RPlan
    public ResourceDescriptor[] prizeIcons;
    // objectives
        // short count
        // short required
        // boolean hidden
        // int key
    public Decal[] decals;
    // decorations
        // PDecoration decoration
        // ResourceDescriptor mesh
    public boolean isPrimary;
    public boolean isInvisible;
    public SlotID[] deactivatedInLevels;
    public SlotID[] activatedInLevels;
    public SlotID[] completedInLevels;
    public boolean stateUpdateViewed;
    public int key;
    public int availabilityDependsUponKey;
    // newDecorations
        // s32 state
        // PDecoration decoration
        // ResourceDescriptor mesh
        // s32 objectiveIdx
        // s32 areaIdx
    // newStickers
        // s32 state
        // Decal sticker
        // s32 objectiveIdx
        // s32 areaIdx
    // potentiallyCompletedObjectsInLevels;
    // items
    //    SlotID
    //    int[]

    @SuppressWarnings("unchecked")
    @Override public RQuest serialize(Serializer serializer, Serializable structure) {
        RQuest quest = (structure == null) ? new RQuest() : (RQuest) structure;



        return quest;
    }
    
    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RQuest.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.QUEST,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }
}
