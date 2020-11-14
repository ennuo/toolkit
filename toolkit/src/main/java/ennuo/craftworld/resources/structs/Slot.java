package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.structs.Collectable;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Images;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.memory.Vector4f;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.enums.ContentsType;
import ennuo.craftworld.resources.enums.GameMode;
import ennuo.craftworld.resources.enums.LevelType;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.structs.Label;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.windows.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Slot {
    
    public static int MAX_SIZE = 0x1200;
    
    public SlotID slot = new SlotID();
    public SlotID group = new SlotID();
    
    public ResourcePtr root;
    public ResourcePtr adventure;
    public ResourcePtr icon = new ResourcePtr(10682, RType.TEXTURE);
    
    public ImageIcon renderedIcon;
    
    public Vector4f location = new Vector4f(0.75f, 0.67f, 0.06f, 0);
    public int revision = 1;
    
    public String authorID = "";
    public String authorName = "";
    public String translationKey = "";
    
    public String title = "Unnamed Level";
    public String description = "No description provided";
    
    public SlotID primaryLinkLevel = new SlotID();
    public SlotID primaryLinkGroup = new SlotID();
    
    public boolean isLocked = false;
    public boolean copyable = false;
    
    public long backgroundGUID = 405678;
    public ResourcePtr planetDecorations = new ResourcePtr(250423, RType.LEVEL);
    
    public LevelType developerLevelType = LevelType.COOPERATIVE;
    
    public Label[] authorLabels = new Label[0];
    
    public Collectable[] requiredCollectables = new Collectable[] { new Collectable(), new Collectable(), new Collectable() };
    public Collectable[] containedCollectables = new Collectable[0];
    
    public boolean isSubLevel = false;
    
    public int minPlayers = 1;
    public int maxPlayers = 4;
    
    public boolean enforceMinMaxPlayers = false;
    
    public boolean moveRecommended = false;
    public boolean crossCompatible = false;
    
    public boolean showOnPlanet = true;
    
    public int livesOverride = 0;
    public GameMode gameMode = GameMode.NONE;
    
    public boolean isGameKit = false;
    
    public String entranceName = "";
    
    public SlotID originalSlotID = new SlotID();
    
    public int customBadgeSize = 1;
    
    public int gameProgressionState = 3;
    
    
    public Slot() {};
    public Slot(Data data) {
        process(data);
    }
    public Slot(Data data, boolean parseSlot, boolean parseGroup) {
        process(data, parseSlot, parseGroup);
    }
    
    public void renderIcon(FileEntry entry, Toolkit toolkit) {
        byte[] data = toolkit.extractFile(icon);
        BufferedImage image = null;
        if (data != null) {
            Texture texture = new Texture(data);
            if (texture != null) image = texture.getImage();
        }
            
        int revision = entry.revision;
        if (root != null) {
            byte[] root = toolkit.extractFile(this.root);
            if (root != null)
                revision = new Resource(root).revision;
        }    
            
        if (slot.type.equals(SlotType.DEVELOPER_GROUP) || slot.type.equals(SlotType.DLC_PACK))
            renderedIcon = Images.getGroupIcon(image);
        else renderedIcon = Images.getSlotIcon(image, revision);
    }
    
    
    private void process(Data data) { process(data, false, false); }
    private void process(Data data, boolean parseSlot, boolean parseGroup) {
        if (parseSlot)
            slot = new SlotID(data);
        if (parseGroup)
            group = new SlotID(data);
        
        root = data.resource(RType.LEVEL, true);
        
        if (root != null)
            System.out.println("slot has root = " + root.toString());
        
        if (data.revision > 0x010503EF) {
            adventure = data.resource(RType.ADVENTURE_CREATE_PROFILE, true);
            if (adventure != null)
                System.out.println("slot has adventure = " + adventure.toString());
        }
        
        icon = data.resource(RType.TEXTURE, true);
        
        if (icon != null)
            System.out.println("slot has icon = " + icon.toString());
        
        location = data.v4();
        
        authorID = data.str(0x14);
        
        authorName = data.str16();
        
        if (authorName != null)
            System.out.println("slot has author = " + authorName);
        
        translationKey = data.str8();
        
        if (translationKey != null && !translationKey.equals(""))
            System.out.println(String.format("Slot has translationKey = %s", translationKey));
        
        title = data.str16();
        description = data.str16();
        
        if (title == null || title.equals("")) title = "Unnamed Level";
        if (description == null || description.equals("")) description = "No description provided.";
        
        System.out.println(String.format("Slot has title = %s, description = %s", title, description));
        
        primaryLinkLevel = new SlotID(data);
        primaryLinkGroup = new SlotID(data);
        
        isLocked = data.bool();
        copyable = data.bool();
        
        System.out.println("Slot is locked? " + isLocked);
        System.out.println("Slot is copyable? " + copyable);
        
        backgroundGUID = data.uint32();
        
        if (data.revision > 0x272)
            planetDecorations = data.resource(RType.PLAN, true);
        
        developerLevelType = LevelType.getValue(data.int32());
        
        if (data.revision <= 0x272)
            gameProgressionState = data.int32();
        
        if (data.revision <= 0x272) return;
        
        System.out.println(String.format("Slot has levelType = %s", developerLevelType.name()));
        
        int labelCount = data.int8();
        authorLabels = new Label[labelCount];
        for (int i = 0; i < labelCount; ++i)
            authorLabels[i] = new Label(data);
        
        int collectableRequiredCount = data.int32();
        if (collectableRequiredCount != 0) {
            requiredCollectables = new Collectable[collectableRequiredCount];
            for (int i = 0; i < collectableRequiredCount; ++i)
                requiredCollectables[i] = new Collectable(data);
        }
        
        int collectableContainedCount = data.int32();
        if (collectableContainedCount != 0) {
            containedCollectables = new Collectable[collectableContainedCount];
            for (int i = 0; i < collectableContainedCount; ++i)
                containedCollectables[i] = new Collectable(data);
        }
        
        isSubLevel = data.bool();
        
        if (data.revision <= 0x3af) return;
        
        System.out.println("Slot is sublevel? " + isSubLevel);
        
        minPlayers = data.int8();
        maxPlayers = data.int8();
        
        if (data.revision >= 0x021803F9)
            enforceMinMaxPlayers = data.bool();
        
        System.out.println(String.format("Slot has minPlayers = %d, maxPlayers = %d", minPlayers, maxPlayers));
        
        if (data.revision >= 0x3b7)
            moveRecommended = data.bool();
        
        System.out.println("Slot has move support? " + moveRecommended);
              
        if (data.revision >= 0x3e6) 
            crossCompatible = data.bool();
        
        System.out.println("Slot is cross-compatible? " + crossCompatible);
        
        showOnPlanet = data.bool();
        
        livesOverride = data.int8();
        
        if (data.revision <= 0x3f8) return;
        
        gameMode = GameMode.getValue(data.int8());
        
        System.out.println(String.format("Slot has gameMode = %s", gameMode.name()));
        
        isGameKit = data.bool();
        
        if (data.revision <= 0x010503EF) return;
        
        entranceName = data.str16();
        
        originalSlotID = new SlotID(data);
        
        if (data.revision <= 0x014703ef) return;
        
        customBadgeSize = data.int8();
        
        System.out.println("Slot has customBadgeSize = " + customBadgeSize);
        
        data.str8(); 
        if (data.revision > 0x01ae03f9)
            data.str8();
    }
    
    public void serialize(Output output) { serialize(output, false, false); }
    public void serialize(Output output, boolean serializeSlot, boolean serializeGroup) {
        if (serializeSlot)
            slot.serialize(output);
        if (serializeGroup)
            group.serialize(output);
        output.resource(root, true);
        
        if (output.revision > 0x010503EF)
            output.resource(adventure, true);
        
        output.resource(icon, true);
        
        output.v4(location);
        output.string(authorID, 0x14);
        
        output.str16(authorName);
        output.str8(translationKey);
        
        
        if (title.equals("Unnamed Level")) title = "";
        if (description.equals("No description provided.")) description = "";
        
        
        output.str16(title);
        output.str16(description);
        
        primaryLinkLevel.serialize(output);
        primaryLinkGroup.serialize(output);
        
        output.bool(isLocked);
        output.bool(copyable);
        
        output.uint32(backgroundGUID);
        
        if (output.revision > 0x272)
            output.resource(planetDecorations, true);
        
        output.int32(developerLevelType.value);
        
        if (output.revision <= 0x272)
            output.int32(gameProgressionState);
        
        if (output.revision <= 0x272) return;
        
        output.int32(authorLabels.length);
        for (Label label : authorLabels)
            label.serialize(output);
        
        output.int32(requiredCollectables.length);
        for (Collectable c : requiredCollectables)
            c.serialize(output);
        
        output.int32(containedCollectables.length);
        for (Collectable c : containedCollectables)
            c.serialize(output);
        
        output.bool(isSubLevel);
        
        if (output.revision <= 0x3af) return;
        
        output.int8(minPlayers);
        output.int8(maxPlayers);
        
        if (output.revision >= 0x021803F9)
            output.bool(enforceMinMaxPlayers);
        
        if (output.revision >= 0x3b7)
            output.bool(moveRecommended);
        
        if (output.revision >= 0x3e6) 
            output.bool(crossCompatible);
        
        output.bool(showOnPlanet);
        
        output.int8(livesOverride);
        
        if (output.revision <= 0x3f8) return;
        
        output.int8(gameMode.value);
        
        output.bool(isGameKit);
        
        if (output.revision <= 0x010503EF) return;
        
        output.str16(entranceName);
        
        originalSlotID.serialize(output);
        
        if (output.revision <= 0x014703ef) return;
        
        output.int8(customBadgeSize);
        
        output.str8("");
        if (output.revision > 0x01ae03f9)
            output.str8("");
    }
}
