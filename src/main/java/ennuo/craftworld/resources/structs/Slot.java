package ennuo.craftworld.resources.structs;

import ennuo.craftworld.utilities.Images;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.enums.GameMode;
import ennuo.craftworld.resources.enums.LevelType;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import org.joml.Vector4f;

public class Slot implements Serializable {
    public static int MAX_SIZE = 0x1200;
    
    public SlotID id = new SlotID();
    
    public ResourceDescriptor root;
    public ResourceDescriptor adventure;
    public ResourceDescriptor icon;
    
    public ImageIcon renderedIcon;
    
    public Vector4f location = new Vector4f(0.75f, 0.67f, 0.06f, 0);
    public int revision = 1;
    
    public String authorID = "";
    public String authorName = "";
    public String translationKey = "";
    
    public String title;
    public String description;
    
    public SlotID primaryLinkLevel = new SlotID();
    public SlotID primaryLinkGroup = new SlotID();
    
    public boolean isLocked = false;
    public boolean copyable = false;
    
    public long backgroundGUID = 0;
    public ResourceDescriptor planetDecorations = null;
    
    public LevelType developerLevelType = LevelType.COOPERATIVE;
    
    public Label[] authorLabels = new Label[0];
    
    public Collectable[] requiredCollectables;
    public Collectable[] containedCollectables;
    
    public boolean isSubLevel;
    
    public byte minPlayers = 1;
    public byte maxPlayers = 4;
    
    public boolean enforceMinMaxPlayers;
    
    public boolean moveRecommended;
    public boolean crossCompatible;
    
    public boolean showOnPlanet = true;
    
    public byte livesOverride;
    public GameMode gameMode = GameMode.NONE;
    
    public boolean isGameKit = false;
    
    public String entranceName = "";
    
    public SlotID originalSlotID = new SlotID();
    
    public byte customBadgeSize = 1;
    
    public int gameProgressionState = 3;
    
    public boolean acingEnabled = true;
    
    public long[] customRewardEnabled;
    public String[] rewardConditionDescription;
    public long[] customRewardCondition;
    public long[] amountNeededCustomReward;
    public String[] customRewardDescription;
    
    public boolean containsCollectabubbles;
    public boolean sameScreenGame  = true;
    public long sizeOfResources;
    public long sizeOfSubLevels;
    
    public String trailerLocal;
    public String trailerWeb;
    
    public SlotID[] subLevels;
    public ResourceDescriptor slotList;
    public int vitaRevision = 0;
    
    public Slot serialize(Serializer serializer, Serializable structure) {
        Slot slot = (structure == null) ? new Slot() : (Slot) structure;
        
        slot.id = serializer.struct(slot.id, SlotID.class);
        
        slot.root = serializer.resource(slot.root, ResourceType.LEVEL, true);
        if (serializer.revision > 0x010503ef)
            slot.adventure = serializer.resource(slot.adventure, ResourceType.ADVENTURE_CREATE_PROFILE, true);
        slot.icon = serializer.resource(slot.icon, ResourceType.TEXTURE, true);
        
        slot.location = serializer.v4(slot.location);
        
        slot.authorID = serializer.str(slot.authorID, 0x14);
        slot.authorName = serializer.str16(slot.authorName);
        
        slot.translationKey = serializer.str8(slot.translationKey);
        
        slot.title = serializer.str16(slot.title);
        slot.description = serializer.str16(slot.description);
        
        slot.primaryLinkLevel = serializer.struct(slot.primaryLinkLevel, SlotID.class);
        slot.primaryLinkGroup = serializer.struct(slot.primaryLinkGroup, SlotID.class);
        
        slot.isLocked = serializer.bool(slot.isLocked);
        slot.copyable = serializer.bool(slot.copyable);
        
        slot.backgroundGUID = serializer.u32(slot.backgroundGUID);
        
        if (serializer.revision > 0x2c3)
            slot.planetDecorations = serializer.resource(slot.planetDecorations, ResourceType.PLAN, true);
        
        slot.developerLevelType = LevelType.getValue(serializer.i32(slot.developerLevelType.value));
        
        if (serializer.revision <= 0x33a)
            slot.gameProgressionState = serializer.i32(slot.gameProgressionState);
        
        if (serializer.revision <= 0x2c3) return slot;
        
        if (serializer.revision > 0x33a)
            slot.authorLabels = serializer.array(slot.authorLabels, Label.class);
        
        slot.requiredCollectables = serializer.array(slot.requiredCollectables, Collectable.class);
        slot.containedCollectables = serializer.array(slot.containedCollectables, Collectable.class);
        
        if (serializer.revision <= 0x33a) return slot;
        
        slot.isSubLevel = serializer.bool(slot.isSubLevel);
        
        if (serializer.revision <= 0x3af) return slot;
        
        slot.minPlayers = serializer.i8(slot.minPlayers);
        slot.maxPlayers = serializer.i8(slot.maxPlayers);
        
        if (serializer.revision >= 0x021803F9)
            slot.enforceMinMaxPlayers = serializer.bool(slot.enforceMinMaxPlayers);
        
        if (serializer.revision >= 0x3b7)
            slot.moveRecommended = serializer.bool(slot.moveRecommended);
        
        if (serializer.revision >= 0x3e6)
            slot.crossCompatible = serializer.bool(slot.crossCompatible);
        
        slot.showOnPlanet = serializer.bool(slot.showOnPlanet);
        
        slot.livesOverride = serializer.i8(slot.livesOverride);
        
        if (serializer.revision == 0x3e2 && serializer.branchDescription != 0) {
            slot.acingEnabled = serializer.bool(slot.acingEnabled);
            slot.customRewardEnabled = serializer.u32a(slot.customRewardEnabled);
            
            if (!serializer.isWriting) slot.rewardConditionDescription = new String[serializer.input.i32()];
            else serializer.output.i32(slot.rewardConditionDescription.length);
            for (int i = 0; i < slot.rewardConditionDescription.length; ++i)
                slot.rewardConditionDescription[i] = serializer.str16(slot.rewardConditionDescription[i]);
            
            slot.customRewardEnabled = serializer.u32a(slot.customRewardCondition);
            
            if (!serializer.isWriting) slot.amountNeededCustomReward = new long[serializer.input.i32()];
            else serializer.output.i32(slot.amountNeededCustomReward.length);
            for (int i = 0; i < slot.amountNeededCustomReward.length; ++i)
                slot.amountNeededCustomReward[i] = serializer.u32f(slot.amountNeededCustomReward[i]);
            
            if (!serializer.isWriting) slot.customRewardDescription = new String[serializer.input.i32()];
            else serializer.output.i32(slot.customRewardDescription.length);
            for (int i = 0; i < slot.customRewardDescription.length; ++i)
                slot.customRewardDescription[i] = serializer.str16(slot.customRewardDescription[i]);
            
            slot.containsCollectabubbles = serializer.bool(slot.containsCollectabubbles);
            slot.enforceMinMaxPlayers = serializer.bool(slot.enforceMinMaxPlayers);
            slot.sameScreenGame = serializer.bool(slot.sameScreenGame);
            
            slot.sizeOfResources = serializer.u32(slot.sizeOfResources);
            slot.sizeOfSubLevels = serializer.u32(slot.sizeOfSubLevels);
            
            slot.subLevels = serializer.array(slot.subLevels, SlotID.class);
            slot.slotList = serializer.resource(slot.slotList, ResourceType.SLOT_LIST);
            
            slot.vitaRevision = serializer.i32(slot.vitaRevision);
        }
        
        if (serializer.revision <= 0x3f8) return slot;
        
        slot.gameMode = GameMode.getValue(serializer.i32(slot.gameMode.value));
        slot.isGameKit = serializer.bool(slot.isGameKit);
        
        if (serializer.revision <= 0x010503EF) return slot;
        
        slot.entranceName = serializer.str16(slot.entranceName);
        slot.originalSlotID = serializer.struct(slot.originalSlotID, SlotID.class);
        
        if (serializer.revision <= 0x014703ef) return slot;
        
        slot.customBadgeSize = serializer.i8(slot.customBadgeSize);
        
        slot.trailerLocal = serializer.str8(slot.trailerLocal);
        if (serializer.revision > 0x01ae03f9)
            slot.trailerWeb = serializer.str8(slot.trailerWeb);
        
        return slot;
    }
    
    public void renderIcon(FileEntry entry) {
        byte[] data = Globals.extractFile(icon);
        BufferedImage image = null;
        if (data != null) {
            Texture texture = new Texture(data);
            if (texture != null) image = texture.getImage();
        }
            
        int revision = entry.revision;
        if (root != null) {
            byte[] root = Globals.extractFile(this.root);
            if (root != null)
                revision = new Resource(root).revision;
        }    
            
        if (id.type.equals(SlotType.DEVELOPER_GROUP) || id.type.equals(SlotType.DLC_PACK))
            renderedIcon = Images.getGroupIcon(image);
        else renderedIcon = Images.getSlotIcon(image, revision);
    }
}
