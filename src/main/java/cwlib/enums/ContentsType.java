package cwlib.enums;

import cwlib.types.data.ResourceReference;

public enum ContentsType {
    THEME(0, 27162),
    PACK(1, 68653),
    LEVEL(2, 16006),
    COSTUME(3, 68653),
    ADVENTURE(5, 642431);
    
    public final int value;
    private final long badgeMeshGUID;
    
    private ContentsType(int value, int mesh) { 
        this.value = value; 
        this.badgeMeshGUID = mesh;
    }
    
    public ResourceReference getBadgeMesh() {
        return new ResourceReference(this.badgeMeshGUID, ResourceType.MESH);
    }
    
    public static ContentsType getValue(int value) {
        for (ContentsType type : ContentsType.values()) {
            if (type.value == value) 
                return type;
        }
        return ContentsType.THEME;
    }  
}
