package cwlib.enums;

public class InventoryItemFlags {
    public static final int NONE = 0x0;
    public static final int HEARTED = 0x1;
    public static final int UPLOADED = 0x2;
    public static final int CHEAT = 0x4;
    public static final int UNSAVED = 0x8;
    public static final int ERRORED = 0x10;
    public static final int HIDDEN_PLAN = 0x20;
    public static final int AUTOSAVED = 0x40;
    
    /**
     * This item can be emitted.
     */
    public static final int ALLOW_EMIT = 0x1;
    
    /**
     * The item was shared from a community level
     * or not created by the player.
     */
    public static final int COPYRIGHT = 0x2;
    
    /**
     * Indicates that the item has been used at least once.
     */
    public static final int USED = 0x4;
    
    public static final int HIDDEN_ITEM = 0x8;
    
    /**
     * This flag is used for items other than backgrounds,
     * but the most notable use is for limiting a background
     * to be used in levels only.
     */
    public static final int RESTRICTED_LEVEL = 0x10;
    
    /**
     * This flag is used for items other than backgrounds,
     * but the most notable use is for limitign a background
     * to be used as a pod background only.
     */
    public static final int RESTRICTED_POD = 0x20;
    
    /**
     * Disables highlight sound from repeating.
     */
    public static final int DISABLE_LOOP_PREVIEW = 0x40;
}
