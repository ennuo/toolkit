package cwlib.enums;

import cwlib.io.ValueEnum;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

/**
 * Represents a type of DLC content slot.
 */
public enum ContentsType implements ValueEnum<Integer> {
    GROUP(0, CommonMeshes.POLAROID_GUID),
    PLANS(1, CommonMeshes.BUBBLE_GUID),
    LEVEL(2, CommonMeshes.LEVEL_BADGE_GUID),
    COSTUME(3, CommonMeshes.BUBBLE_GUID),
    ADVENTURE(5, CommonMeshes.ADVENTURE_BADGE_GUID);
    
    private final int value;

    /**
     * The default associated mesh associated
     * with this content type.
     */
    private final GUID badgeMeshGUID;
    
    private ContentsType(int value, GUID mesh) { 
        this.value = value; 
        this.badgeMeshGUID = mesh;
    }

    public Integer getValue() { return this.value; }
    public ResourceDescriptor getBadgeMesh() {
        return new ResourceDescriptor(this.badgeMeshGUID, ResourceType.MESH);
    }
    
    public static ContentsType fromValue(int value) {
        for (ContentsType type : ContentsType.values()) {
            if (type.value == value) 
                return type;
        }
        return ContentsType.GROUP;
    }
}