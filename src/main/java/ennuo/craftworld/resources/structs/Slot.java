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
    
    public ResourceDescriptor root = new ResourceDescriptor(16704, ResourceType.LEVEL);
    public ResourceDescriptor adventure;
    public ResourceDescriptor icon = new ResourceDescriptor(11651, ResourceType.TEXTURE);
    
    public ImageIcon renderedIcon;
    
    public Vector4f location = new Vector4f(0.75f, 0.67f, 0.06f, 0);
    public int revision = 1;
    
    public SceNpOnlineId authorID;
    public String authorName = "";
    public String translationTag = "";
    
    public String title = "";
    public String description = "";
    
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
    
    public String localPath;
    public String thumbPath;
    
    public SlotID[] subLevels;
    public ResourceDescriptor slotList;
    public short vitaRevision = 0;
    
    public Slot serialize(Serializer serializer, Serializable structure) {
        Slot slot = (structure == null) ? new Slot() : (Slot) structure;
        
        slot.id = serializer.struct(slot.id, SlotID.class);
        
        slot.root = serializer.resource(slot.root, ResourceType.LEVEL, true);
        if (serializer.revision.isAfterLBP3Revision(0x144))
            slot.adventure = serializer.resource(slot.adventure, ResourceType.ADVENTURE_CREATE_PROFILE, true);
        slot.icon = serializer.resource(slot.icon, ResourceType.TEXTURE, true);
        
        slot.location = serializer.v4(slot.location);
        
        slot.authorID = serializer.struct(slot.authorID, SceNpOnlineId.class);
        
        if (serializer.revision.head >= 0x13b)
            slot.authorName = serializer.str16(slot.authorName);
        
        if (serializer.revision.head >= 0x183)
            slot.translationTag = serializer.str8(slot.translationTag);
        
        if (serializer.isWriting) {
            if (slot.translationTag != null && !slot.translationTag.isEmpty()) {
                slot.title = "";
                slot.description = "";
            }
        }
        
        slot.title = serializer.str16(slot.title);
        slot.description = serializer.str16(slot.description);
        
        slot.primaryLinkLevel = serializer.struct(slot.primaryLinkLevel, SlotID.class);
        if (serializer.revision.head >= 0x134)
            slot.primaryLinkGroup = serializer.struct(slot.primaryLinkGroup, SlotID.class);
        
        slot.isLocked = serializer.bool(slot.isLocked);
        
        if (serializer.revision.head >= 0x238) {
            slot.copyable = serializer.bool(slot.copyable);
            slot.backgroundGUID = serializer.u32(slot.backgroundGUID);
        }
        
        if (serializer.revision.head >= 0x333)
            slot.planetDecorations = serializer.resource(slot.planetDecorations, ResourceType.PLAN, true);
        
        if (serializer.revision.head >= 0x1df)
            slot.developerLevelType = LevelType.getValue(serializer.i32(slot.developerLevelType.value));
        
        if (serializer.revision.head < 0x36c && 0x1b8 < serializer.revision.head)
            slot.gameProgressionState = serializer.i32(slot.gameProgressionState);
        
        if (serializer.revision.head <= 0x2c3) return slot;
        
        if (serializer.revision.head > 0x33c)
            slot.authorLabels = serializer.array(slot.authorLabels, Label.class);
        
        if (serializer.revision.head >= 0x2ea)
            slot.requiredCollectables = serializer.array(slot.requiredCollectables, Collectable.class);
        if (serializer.revision.head >= 0x2f4)
            slot.containedCollectables = serializer.array(slot.containedCollectables, Collectable.class);
        
        if (serializer.revision.head < 0x352) return slot;
        
        slot.isSubLevel = serializer.bool(slot.isSubLevel);
        
        if (serializer.revision.head < 0x3d0) return slot;
        
        slot.minPlayers = serializer.i8(slot.minPlayers);
        slot.maxPlayers = serializer.i8(slot.maxPlayers);
        
        if (serializer.revision.isAfterLBP3Revision(0x214))
            slot.enforceMinMaxPlayers = serializer.bool(slot.enforceMinMaxPlayers);
        
        if (serializer.revision.head >= 0x3d0)
            slot.moveRecommended = serializer.bool(slot.moveRecommended);
        
        if (serializer.revision.head >= 0x3e9)
            slot.crossCompatible = serializer.bool(slot.crossCompatible);
        
        slot.showOnPlanet = serializer.bool(slot.showOnPlanet);
        
        slot.livesOverride = serializer.i8(slot.livesOverride);
        
        if (serializer.revision.isVita()) {
            if (serializer.revision.isAfterVitaRevision(0x3c)) {
                slot.acingEnabled = serializer.bool(slot.acingEnabled);
                slot.customRewardEnabled = serializer.u32a(slot.customRewardEnabled);
                
                if (!serializer.isWriting) slot.rewardConditionDescription = new String[serializer.input.i32()];
                else serializer.output.i32(slot.rewardConditionDescription.length);
                for (int i = 0; i < slot.rewardConditionDescription.length; ++i)
                    slot.rewardConditionDescription[i] = serializer.str16(slot.rewardConditionDescription[i]);

                slot.customRewardCondition = serializer.u32a(slot.customRewardCondition);

                if (!serializer.isWriting) slot.amountNeededCustomReward = new long[serializer.input.i32()];
                else serializer.output.i32(slot.amountNeededCustomReward.length);
                for (int i = 0; i < slot.amountNeededCustomReward.length; ++i)
                    slot.amountNeededCustomReward[i] = serializer.u32f(slot.amountNeededCustomReward[i]);

                if (!serializer.isWriting) slot.customRewardDescription = new String[serializer.input.i32()];
                else serializer.output.i32(slot.customRewardDescription.length);
                for (int i = 0; i < slot.customRewardDescription.length; ++i)
                    slot.customRewardDescription[i] = serializer.str16(slot.customRewardDescription[i]);
            }
            
            if (serializer.revision.isAfterVitaRevision(0x5d)) 
                slot.containsCollectabubbles = serializer.bool(slot.containsCollectabubbles);
            
            if (serializer.revision.isAfterVitaRevision(0x4b))
                slot.enforceMinMaxPlayers = serializer.bool(slot.enforceMinMaxPlayers);
            
            if (serializer.revision.isAfterVitaRevision(0x4c))
                slot.sameScreenGame = serializer.bool(slot.sameScreenGame);
            
            if (serializer.revision.isAfterVitaRevision(0x5c)) {
                slot.sizeOfResources = serializer.u32(slot.sizeOfResources);
                slot.sizeOfSubLevels = serializer.u32(slot.sizeOfSubLevels);
                slot.subLevels = serializer.array(slot.subLevels, SlotID.class);
                slot.slotList = serializer.resource(slot.slotList, ResourceType.SLOT_LIST);
            }
            
            if (serializer.revision.isAfterVitaRevision(0x7f))
                slot.vitaRevision = serializer.i16(slot.vitaRevision);
        }
        
        if (!serializer.revision.isLBP3()) return slot;
        
        if (serializer.revision.isAfterLBP3Revision(0x11))
            slot.gameMode = GameMode.getValue(serializer.i32(slot.gameMode.value));
        
        if (serializer.revision.isAfterLBP3Revision(0xd1))
            slot.isGameKit = serializer.bool(slot.isGameKit);
        
        if (serializer.revision.isAfterLBP3Revision(0x11a)) {
            slot.entranceName = serializer.str16(slot.entranceName);
            slot.originalSlotID = serializer.struct(slot.originalSlotID, SlotID.class);
        }
        
        if (serializer.revision.isAfterLBP3Revision(0x152))
            slot.customBadgeSize = serializer.i8(slot.customBadgeSize);
        
        if (serializer.revision.isAfterLBP3Revision(0x191)) {
            slot.localPath = serializer.str8(slot.localPath);
            if (serializer.revision.isAfterLBP3Revision(0x205))
                slot.thumbPath = serializer.str8(slot.thumbPath);
        }
        
        return slot;
    }
    
    public void renderIcon(FileEntry entry) {
        byte[] data = Globals.extractFile(icon);
        BufferedImage image = null;
        if (data != null) {
            Texture texture = new Texture(data);
            if (texture != null) image = texture.getImage();
        }
            
        int revision = entry.revision.head;
        if (root != null) {
            byte[] root = Globals.extractFile(this.root);
            if (root != null)
                revision = new Resource(root).revision.head;
        }    
            
        if (id.type.equals(SlotType.DEVELOPER_GROUP) || id.type.equals(SlotType.DLC_PACK))
            renderedIcon = Images.getGroupIcon(image);
        else renderedIcon = Images.getSlotIcon(image, revision);
        
    }
    
    
    @Override public String toString() {
        if (this.translationTag != null && !this.translationTag.isEmpty()) {
            if (Globals.LAMS == null) return this.translationTag;
            String translated = Globals.LAMS.translate(this.translationTag + "_NAME");
            if (translated != null) return translated;
            return this.translationTag;
        }
        if (this.title == null || this.title.isEmpty())
            return "Unnamed Level";
        return this.title;
    }
}
