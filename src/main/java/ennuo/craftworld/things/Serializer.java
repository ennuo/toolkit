package ennuo.craftworld.things;

import ennuo.craftworld.resources.enums.ItemType;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.resources.TranslationTable;
import ennuo.craftworld.resources.enums.ItemSubType;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.enums.ToolType;
import ennuo.craftworld.resources.structs.Copyright;
import ennuo.craftworld.resources.structs.EyetoyData;
import ennuo.craftworld.resources.structs.PhotoData;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.resources.structs.UserCreatedDetails;
import ennuo.craftworld.things.parts.ScriptInstance;
import java.util.ArrayList;

public class Serializer {
    public static String[] PARTS = { 
        "Body", "Joint", "World", "RenderMesh", "Pos", 
        "Trigger", "Yellowhead", "AudioWorld", "Animation", 
        "GeneratedMesh", "LevelSettings", "SpriteLight",
        "ScriptName", "Creature", "Checkpoint", "Stickers",
        "Decorations", "Script", "Shape", "Effector", "Emitter",
        "Ref", "Metadata", "Costume", "CameraTweak", "Switch",
        "SwitchKey", "GameplayData", "Enemy", "Group", "PhysicsTweak",
        "Npc", "SwitchInput", "Microchip", "MaterialTweak", "MaterialOveride",
        "Instrument", "Sequencer", "Controlinator", "PoppetPowerup", "PocketItem",
        "Transition", "Fader", "AnimationTweak", "WindTweak", "PowerUp", "HudElem",
        "TagSynchronizer", "Wormhole", "Quest", "ConnectorHook", "AtmosphericTweak",
        "StreamingData", "StreamingHint" };
    
    public static int LEGACY_THING = 0x25b;
    
    private TranslationTable LAMS;
    
    public boolean isWriting = true;
    
    public int gameRevision = 0x1A003F9;
    public int partsRevision = 0x7e;
    
    ArrayList<ThingPtr> things = new ArrayList<ThingPtr>();
    int sp = 0;
    
    ArrayList<Integer> components = new ArrayList<Integer>();
    int currentComponent = 0;
    
    public Data input;
    public Output output;
    
    boolean stopParsing = false;
    
    public Serializer(Data data) {
        this.input = data;
        this.isWriting = false;
    }
    
    public Serializer(Output output) {
        this.output = output;
        this.isWriting = false;
    }
    
    public Serializer(Data data, TranslationTable LAMS) {
        setLAMS(LAMS);
        input = data;
        isWriting = false;
    }
    
    public void setLAMS(TranslationTable LAMS) { this.LAMS = LAMS; }
    
    private boolean checkBit(long flags, long position) {
        return (flags & (1L << position)) > 0;
    }
    
    private void printParts(long flags) {
        String out = "Parts (";
        for (long i = 0; i < PARTS.length; ++i)
            if (checkBit(flags, i))
                out += PARTS[(int) i] + ", ";    
        System.out.println(out.substring(0, out.length() - 2) + ")");
    }
    
    private boolean checkIfSerialized() {
        int component = input.int32();
        if (component == 0) return false;
        if (components.contains(component)) return false;
        components.add(component);
        return true;
    }
    
    public void serializeMetadata(InventoryMetadata metadata) { serializeMetadata(metadata, false); }
    public void serializeMetadata(InventoryMetadata metadata, boolean item) {
        output.varint(metadata.dateAdded);
        metadata.levelUnlockSlotID.serialize(output);
        output.varint(metadata.highlightSound);
        output.varint(metadata.colour);
        
        output.varint(metadata.type.getValue(output.revision));
        output.varint(metadata.subType.value);
        
        output.varint(metadata.titleKey);
        output.varint(metadata.descriptionKey);
        
        if (metadata.creationHistory != null && metadata.creationHistory.length != 0) {
            currentComponent++; output.varint(currentComponent);
            output.int8(metadata.creationHistory.length);
            for (String creator : metadata.creationHistory)
                output.string(creator, 0x14);
        } else output.int32(0);
        
        output.resource(metadata.icon, true);
        
        if (metadata.userCreatedDetails != null) {
                currentComponent++; output.varint(currentComponent);
                metadata.userCreatedDetails.serialize(output);
        } else output.int32(0);
        
        if (metadata.photoData  != null) {
            currentComponent++; output.varint(currentComponent);
            metadata.photoData.serialize(output);
        } else output.int32(0);
        
        if (metadata.eyetoyData != null) {
            currentComponent++; output.varint(currentComponent);
            metadata.eyetoyData.serialize(output);
        } else output.int32(0);
        
        output.int16(metadata.locationIndex);
        output.int16(metadata.categoryIndex);
        output.int16(metadata.primaryIndex);
        
        if (metadata.creator != null) {
            currentComponent++; output.varint(currentComponent);
            metadata.creator.serialize(output);
        } else output.int32(0);
        
        output.int8(metadata.toolType.value);
        output.int8(metadata.flags);
        
        if (!item) return;
        
        output.varint(metadata.location);
        output.varint(metadata.category);
    }
    
    public void serializeLegacyMetadata(InventoryMetadata metadata) { serializeLegacyMetadata(metadata, false); }
    public void serializeLegacyMetadata(InventoryMetadata metadata, boolean item) {
        output.pad(0xC);
        
        output.int32f(metadata.locationIndex);
        output.int32f(metadata.categoryIndex);
        output.int32f(metadata.primaryIndex);
        
        output.pad(0x8);
        
        output.uint32f(metadata.colour);
        
        output.pad(0x4);
        
        output.uint32f(metadata.dateAdded);
        output.uint32f(metadata.highlightSound);
        
        for (int i = 0; i < 4; ++i)
            output.int8(metadata.flags);
        
        output.uint32f(metadata.type.getValue(output.revision));
        output.uint32f(metadata.subType.value);
        output.uint32f(metadata.toolType.value);
        
        metadata.creator.serialize(output);
        
        output.pad(0x3);
        if (output.revision >= 0x33a) output.pad(0x1);
        
        output.varint(metadata.titleKey);
        output.varint(metadata.descriptionKey);
        
        if (metadata.userCreatedDetails != null)
            metadata.userCreatedDetails.serialize(output);
        else output.int16((short) 0);
        
        if (metadata.creationHistory != null) {
            output.int8(metadata.creationHistory.length);
            for (int i = 0; i < metadata.creationHistory.length; ++i)
                output.str16(metadata.creationHistory[i]);
        } else output.int32(0);
        
        output.resource(metadata.icon, true);
        
        if (metadata.photoData != null) {
            currentComponent++; output.int32(currentComponent);
            metadata.photoData.serialize(output);
        } else output.int32(0);
        
        if (metadata.eyetoyData != null) {
            currentComponent++; output.int32(currentComponent);
            metadata.eyetoyData.serialize(output);
        } else output.int32(0);
        
        if (!item) return;
        
        if (output.revision >= 0x272) {
            output.uint32(metadata.location);
            output.uint32(metadata.category);
        } else {
            output.str8(metadata.legacyLocationKey);
            output.str8(metadata.legacyCategoryKey);
        }
    }
    
    public InventoryMetadata ParseLBP1BPRMetadata() { return ParseLBP1BPRMetadata(false); }
    public InventoryMetadata ParseLBP1BPRMetadata(boolean isItem) {
        System.out.println("Parsing some legacy InventoryItem metadata... This will not be complete.");
        InventoryMetadata metadata = new InventoryMetadata();
       
        input.forward(0xC);
        
        metadata.locationIndex = (short) input.int32f();
        metadata.categoryIndex = (short) input.int32f();
        metadata.primaryIndex = (short) input.int32f();
        
        input.forward(0x8);
        
        metadata.colour = input.uint32f();
        
        input.forward(0x4);
        
        metadata.dateAdded = input.uint32f();
        
        metadata.highlightSound = input.int32f();
        
        for (int i = 0; i < 4; ++i)
            metadata.flags = input.int8();
        
        metadata.type = ItemType.getValue(input.uint32f(), input.revision);
        metadata.subType = ItemSubType.getValue(input.uint32f(), metadata.type);
        metadata.toolType = ToolType.getValue((byte) input.int32f());
        
        metadata.creator = new Copyright(input);
        
        input.forward(0x3);
        
        if (input.revision >= 0x33a) input.forward(0x1);
        
        if (input.revision >= 0x272) {
            metadata.titleKey = input.varint();
            metadata.descriptionKey = input.varint();   
        } else
            metadata.translationKey = input.str8();
        
        String title = input.str16();
        String description = input.str16();
        if (title.equals("") && description.equals(""))
            metadata.userCreatedDetails = null;
        else {
            metadata.userCreatedDetails = new UserCreatedDetails();
            metadata.userCreatedDetails.title = title;
            metadata.userCreatedDetails.description = description;
        }
        
        int creatorCount = input.int32();
        
        if (creatorCount == 0) metadata.creationHistory = null;
        else {
            metadata.creationHistory = new String[creatorCount];
            for (int i = 0; i < creatorCount; ++i)
                metadata.creationHistory[i] = input.str16();
        }
        
        metadata.icon = input.resource(RType.TEXTURE, true);
        
        if (checkIfSerialized())
            metadata.photoData = new PhotoData(input);
        
        if (checkIfSerialized())
            metadata.eyetoyData = new EyetoyData(input);
        
        if (!isItem) return metadata;
        
        if (input.revision >= 0x272) {
            metadata.location = input.uint32();
            metadata.category = input.uint32();
        } else {
            metadata.legacyLocationKey = input.str8();
            metadata.legacyCategoryKey = input.str8();
        }
        

        return metadata;
    }
    
    
    public InventoryMetadata ParseLBP1Metadata() {
        System.out.println("Parsing some legacy InventoryItem metadata... This will not be complete.");
        InventoryMetadata metadata = new InventoryMetadata();
        
        metadata.translationKey = input.str(input.int32f());
        metadata.locationIndex = (short) input.int32f();
        metadata.categoryIndex = (short) input.int32f();
        
        
        input.forward(0x8);
        
        metadata.type = ItemType.getValue(input.int32(), input.revision);
        
        input.forward(0x8);
        
        metadata.icon = input.resource(RType.TEXTURE, true);
        
        input.seek(input.length - 1);
        
        String category = "";
        if (input.data[input.offset] != 0) {
            while (category.length() != input.data[input.offset]) {
                category = (char) input.data[input.offset] + category;
                input.offset--;
            }
        }
        
        metadata.legacyCategoryKey = category;
        
        return metadata;
    }
    
    
    public InventoryMetadata ParseMetadata() { return ParseMetadata(true); }
    public InventoryMetadata ParseMetadata(boolean item) {
        System.out.println("Parsing InventoryItem metadata...");
        
        InventoryMetadata metadata = new InventoryMetadata();
        
        metadata.dateAdded = input.uint32(); 
        metadata.levelUnlockSlotID = new SlotID(input);
        metadata.highlightSound = input.uint32();
        metadata.colour = input.uint32();
        
        metadata.type = ItemType.getValue(input.uint32(), input.revision);
        
        metadata.subType = ItemSubType.getValue(input.uint32(), metadata.type);
        
        System.out.println(String.format("InventoryItem has type = %d (%s), subtype = %d (%s)", metadata.type.getValue(input.revision), metadata.type.name(), metadata.subType.value, metadata.subType.name()));
        
        metadata.titleKey = input.uint32();
        metadata.descriptionKey = input.uint32();
        
        System.out.println(String.format("InventoryItem has titleKey = %d, descriptionKey = %d", metadata.titleKey, metadata.descriptionKey));
        if (LAMS != null) {
            if (metadata.titleKey != 0) {
                String translated = LAMS.Translate(metadata.titleKey);
                if (translated != null)
                    System.out.println(String.format("InventoryItem has title = %s", translated));
                else System.err.println("== COULD NOT TRANSLATE TITLE KEY, DOES IT EXIST? ==");
            }
            if (metadata.descriptionKey != 0) {
                String translated = LAMS.Translate(metadata.descriptionKey);
                if (translated != null)
                    System.out.println(String.format("InventoryItem has description = %s", translated));
                else System.err.println("== COULD NOT TRANSLATE DESCRIPTION KEY, DOES IT EXIST? ==");
            }
        }
        
        if (checkIfSerialized()) {
            int historyCount = input.int32();
            if (historyCount != 0) {
                metadata.creationHistory = new String[historyCount];
                for (int i = 0; i < historyCount; ++i)
                    metadata.creationHistory[i] = input.str(0x14);
                System.out.println("InventoryItem has " + historyCount + " creators = " + metadata.creationHistory[0]);
            }
        } else metadata.creationHistory = null;
        
        metadata.icon = input.resource(RType.TEXTURE, true);
        
        if (metadata.icon != null) {
            if (metadata.icon.GUID != -1)
                System.out.println("InventoryItem has icon = g" + metadata.icon.GUID);
            else if(metadata.icon.hash != null) 
                System.out.println("InventoryItem has icon = h" + Bytes.toHex(metadata.icon.hash));
            else
                System.out.println("InventoryItem does not have an icon.");   
        } else System.out.println("InventoryItem does not have an icon.");   
        
        if (checkIfSerialized()) {
            System.out.println("InventoryItem has user generated metadata");
            metadata.userCreatedDetails = new UserCreatedDetails(input);
            System.out.println(String.format("title = %s, description = %s", metadata.userCreatedDetails.title, metadata.userCreatedDetails.description));
        } else metadata.userCreatedDetails = null;
        
        
        if (checkIfSerialized())
            metadata.photoData = new PhotoData(input);
        
        if (checkIfSerialized()) 
            metadata.eyetoyData = new EyetoyData(input);
        
        metadata.locationIndex = input.int16();
        metadata.categoryIndex = input.int16();
        metadata.primaryIndex = input.int16();
        
        if (checkIfSerialized()) {
            metadata.creator = new Copyright(input);
            System.out.println("Copyright belongs to " + metadata.creator.PSID);
        }
        
        metadata.toolType = ToolType.getValue(input.int8());  
        metadata.flags = input.int8();
        
        if (!item) return metadata;
        
        System.out.println(String.format("InventoryItem has toolType = %d, flags = %d", metadata.toolType.value, metadata.flags));
        
        metadata.location = input.uint32();
        metadata.category = input.uint32(); 
        
        System.out.println(String.format("InventoryItem has locationKey = %d, categoryKey = %d", metadata.location, metadata.category));   
        
        if (LAMS != null) {
            if (metadata.location != 0) {
                String translated = LAMS.Translate(metadata.location);
                if (translated != null) {
                    System.out.println(String.format("InventoryItem has theme = %s", translated));
                    metadata.translatedLocation = translated;
                }
                else System.err.println("== COULD NOT TRANSLATE LOCATION KEY, DOES IT EXIST? ==");
            }
            if (metadata.category != 0) {
                String translated = LAMS.Translate(metadata.category);
                if (translated != null) {
                    System.out.println(String.format("InventoryItem has category = %s", translated));
                    metadata.translatedCategory = translated;
                }
                else System.err.println("== COULD NOT TRANSLATE CATEGORY KEY, DOES IT EXIST? ==");
            }
        }
        
        return metadata;
        
        
    }
    
    
    public ThingPtr isThingSerialized(int sp) {
        if (things.size() == 0) return null;
        for (ThingPtr ptr : things)
            if (sp == ptr.index)
                return ptr;
        return null;
    }
    
    public void serializeThing(ThingPtr thing) {}
    public ThingPtr deserializeThing() {
        if (stopParsing) return null;
        sp = input.int32();
        if (sp == 0) return new ThingPtr();
        
        ThingPtr isSerialized = isThingSerialized(sp);
        if (isSerialized != null) return isSerialized;
        
        ThingPtr thingPtr = new ThingPtr();
        thingPtr.index = sp;
        
        things.add(thingPtr);
        
        if (gameRevision > LEGACY_THING)
            input.int8();
        
        Thing thing = new Thing();
        thingPtr.thing = thing;
        
        if (gameRevision <= 0x272)
            thing.parent = deserializeThing();
        thingPtr.UID = input.int32();
        
        
        if (gameRevision > 0x272)
            thing.parent = deserializeThing();
        thing.group = deserializeThing();
        deserializeThing();
        
        long flags = 0;
        if (gameRevision > 0x25b) {
            thing.createdBy = input.int16();
            thing.changedBy = input.int16();
            
            if (gameRevision > 0x272) {
                thing.planGUID = input.int32();
                thing.extraFlags = (gameRevision > 0x010503ef) ? input.int16() : (short) input.int32();
            } else {
                thing.extraFlags = (short) input.int32();
                thing.planGUID = input.int32();
            }
            
            partsRevision = input.int8();
            flags = input.uint32();
        } else {
            partsRevision = 0x3e;
            if (gameRevision > 0x210) {
                thing.createdBy = input.int16();
                thing.changedBy = input.int16();
                input.bool();
            }
            if (gameRevision > 0x25b)
                input.int32();
            input.int32();
        }
        
        System.out.println(String.format("Thing (UID = 0x%s, Parent = 0x%s, Group = 0x%s, GUID = 0x%s, Version = 0x%s)", 
                Bytes.toHex(thingPtr.UID), Bytes.toHex(thing.parent.UID), Bytes.toHex(thing.group.UID), Bytes.toHex(thing.planGUID), Bytes.toHex(partsRevision)));
        printParts(flags);
        
        for (long i = 0; i < PARTS.length; ++i)
            if (checkBit(flags, i)) 
                if (!deserializePart(PARTS[(int) i], thing)) {
                    stopParsing = true;
                    return null;
                }
        
        return thingPtr;        
    }
    
    public Part deserializePart(String part) {
        int comp = input.int32();
        if (part == "SCRIPTINSTANCE") {
            System.out.println("Deserializing ScriptInstance");
            ScriptInstance instance = new ScriptInstance();
            instance.Deserialize(this);
            return instance;
        } else {
            Part p = PartList.getPart(part);
            p.Deserialize(this);
            return p;
        }
    }
    
    
    public boolean deserializePart(String part, Thing thing) {
        int comp = input.int32();
        try {
            System.out.println("P" + part + " (START: 0x" + Bytes.toHex(input.offset) + ")"); 
            thing.parts.add(part).Deserialize(this);
            System.out.println("P" + part + " (END: 0x" + Bytes.toHex(input.offset) + ")");
            return true;
        } catch (Exception ex) { System.out.println(ex); stopParsing = true; return false; }
    }
    
    
    public InventoryItem DeserializeItem() {
        InventoryItem item = new InventoryItem();
        input.offset = 0;
        
        item.isUsedForStreaming = false;
        if (input.peek() == 0 || input.peek() == 1)
            item.isUsedForStreaming = input.bool();
       
        
        gameRevision = input.int32(); 
        item.revision = gameRevision; 
        input.revision = gameRevision;
        
        int bufferSize = input.int32();
        
        int metadataOffset = input.offset + bufferSize;
        int thingCount = input.int32();
        
        System.out.println(String.format("Parsing Inventory Item (r%d, thingCount: %d)", gameRevision, thingCount));
        System.out.println("Inventory Item is used for streaming?: " + item.isUsedForStreaming);
        
        System.out.println("Thing Serializer is disabled, skipping.");
        
        /*
        try {
            ThingPtr[] thingData = new ThingPtr[thingCount];
            for (int i = 0; i < thingCount; ++i) {
                thingData[i] = deserializeThing();
                if (thingData[i] == null) throw new Exception("Thing is null!");
            } 
        } catch(Exception e) { System.err.println("There was an error processing the Inventory Item."); } 
        */

        input.seek(metadataOffset);
        
        try {
            if (gameRevision > 0x34a) {
                Serializer serializer = new Serializer(input, LAMS);
                item.metadata = serializer.ParseMetadata();
            } else if (gameRevision <= 0x1fa) {
                Serializer serializer = new Serializer(input, LAMS);
                item.metadata = serializer.ParseLBP1Metadata();
            } else {
                Serializer serializer = new Serializer(input, LAMS);
                item.metadata = serializer.ParseLBP1BPRMetadata(true);
            }
        } catch (Exception e) { item.metadata = null; System.err.println("There was an error processing the Inventory Metadata."); }
        
        return item;
    }
    
    
    
    
}
