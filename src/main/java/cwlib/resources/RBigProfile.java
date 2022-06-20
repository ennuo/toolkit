package cwlib.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.profile.Challenge;
import cwlib.structs.profile.DataLabel;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.profile.StringLookupTable;
import cwlib.structs.profile.Treasure;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;

/**
 * A resource used to contain all user generated content,
 * including your saved objects, costumes, etc, as well
 * as saved levels (slots).
 */
public class RBigProfile implements Compressable, Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    /**
     * List of items this user has.
     */
    public ArrayList<InventoryItem> inventory = new ArrayList<>();
    
    public ArrayList<SHA1> vitaCrossDependencyHashes = new ArrayList<>();

    public ArrayList<DataLabel> creatorDataLabels = new ArrayList<>();

    /**
     * Stores categories and locations referenced by items.
     */
    public StringLookupTable stringTable = new StringLookupTable();

    /**
     * Whether or not this profile was from a production build.
     */
    public boolean fromProductionBuild = true;

    /**
     * Slots created by the user on their moon.
     */
    public HashMap<SlotID, Slot> myMoonSlots = new HashMap<>();

    /* Near nonsense, don't even remember what it was used for. */
    public ArrayList<Challenge> nearMyChallengeDataLog = new ArrayList<>();
    public ArrayList<Challenge> nearMyChallengeDataOpen = new ArrayList<>();
    public ArrayList<Treasure> nearMyTreasureLog = new ArrayList<>();

    /**
     * List of slots that a user has downloaded in LittleBigPlanet Vita.
     */
    public ArrayList<SlotID> downloadedSlots = new ArrayList<>();

    /**
     * Locally stored planet decorations in LittleBigPlanet Vita.
     */
    public ResourceDescriptor planetDecorations;

    @SuppressWarnings("unchecked")
    @Override public RBigProfile serialize(Serializer serializer, Serializable structure) {
        RBigProfile profile = (structure == null) ? new RBigProfile() : (RBigProfile) structure;

        profile.inventory = serializer.arraylist(profile.inventory, InventoryItem.class);

        Revision revision = serializer.getRevision();
        int head = revision.getVersion();

        if (head > 0x3ea) {
            if (serializer.isWriting()) {
                if (profile.vitaCrossDependencyHashes != null) {
                    MemoryOutputStream stream = serializer.getOutput();
                    stream.i32(profile.vitaCrossDependencyHashes.size());
                    for (SHA1 hash : profile.vitaCrossDependencyHashes)
                        stream.sha1(hash);
                } else serializer.i32(0);
            } else {
                MemoryInputStream stream =  serializer.getInput();
                int size = stream.i32();
                profile.vitaCrossDependencyHashes = new ArrayList<SHA1>(size);
                for (int i = 0; i < size; ++i)
                    profile.vitaCrossDependencyHashes.add(stream.sha1());
            }
        }

        if (head >= 0x3ef)
            profile.creatorDataLabels = serializer.arraylist(profile.creatorDataLabels, DataLabel.class);

        profile.stringTable = serializer.struct(profile.stringTable, StringLookupTable.class);

        if (head > 0x3b5)
            profile.fromProductionBuild = serializer.bool(profile.fromProductionBuild);
        
        if (serializer.isWriting()) {
            Set<SlotID> keys = profile.myMoonSlots.keySet();
            serializer.getOutput().i32(keys.size());
            for (SlotID key : keys) {
                serializer.struct(key, SlotID.class);
                serializer.struct(profile.myMoonSlots.get(key), Slot.class);
            }
        } else {
            int count = serializer.getInput().i32();
            profile.myMoonSlots = new HashMap<SlotID, Slot>(count);
            for (int i = 0; i < count; ++i)
                profile.myMoonSlots.put(
                        serializer.struct(null, SlotID.class), 
                        serializer.struct(null, Slot.class));
        }

        if (revision.isAfterVitaRevision(0x2d))
            profile.creatorDataLabels = serializer.arraylist(profile.creatorDataLabels, DataLabel.class);

        if (revision.isAfterVitaRevision(0x56)) {
            profile.nearMyChallengeDataLog = 
                serializer.arraylist(profile.nearMyChallengeDataLog, Challenge.class);

            profile.nearMyChallengeDataOpen = 
                serializer.arraylist(profile.nearMyChallengeDataOpen, Challenge.class);
        }

        if (revision.isAfterVitaRevision(0x58))
            profile.nearMyTreasureLog = serializer.arraylist(profile.nearMyTreasureLog, Treasure.class);

        if (revision.isAfterVitaRevision(0x59))
            profile.downloadedSlots = serializer.arraylist(profile.downloadedSlots, SlotID.class);

        if (revision.isAfterVitaRevision(0x7a))
            profile.planetDecorations = serializer.resource(profile.planetDecorations, ResourceType.LEVEL, true);
        
        return profile;
    }

    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        size += this.stringTable.getAllocatedSize();
        if (this.inventory != null)
            for (int i = 0; i < this.inventory.size(); ++i)
                size += this.inventory.get(i).getAllocatedSize();
        for (Slot slot : this.myMoonSlots.values())
            size += (slot.getAllocatedSize() + SlotID.BASE_ALLOCATION_SIZE);
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RBigProfile.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags, 
            ResourceType.BIG_PROFILE,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }
}
