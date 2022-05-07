package ennuo.craftworld.types.data;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.utilities.StringUtils;

public class ResourceDescriptor {
    public ResourceType type = ResourceType.FILE_OF_BYTES;
    public long GUID = -1;
    public SHA1 hash = null;
    public int flags;
    
    public ResourceDescriptor() {}
    public ResourceDescriptor(ResourceType type, String str) {
        if (StringUtils.isGUID(str))
            this.GUID = StringUtils.getLong(str);
        else if (StringUtils.isSHA1(str))
            this.hash = StringUtils.getSHA1(str);
        else throw new IllegalArgumentException("Invalid resource reference passed into resource descriptor!");

        this.type = type;
    }
    
    public ResourceDescriptor(long GUID, ResourceType type) {
        this.GUID = GUID;
        this.type = type;
    }
    
    public ResourceDescriptor(SHA1 hash, ResourceType type) {
        this.hash = hash;
        this.type = type;
    }
    
    @Override public int hashCode() { return this.toString().hashCode(); }

    @Override public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ResourceDescriptor)) return false;
        ResourceDescriptor descriptor = (ResourceDescriptor)o;
        return descriptor.toString().equals(this.toString());
    }

    @Override public String toString() {
        if (this.hash != null)
            return "h" + this.hash.toString();
        else if (this.GUID != -1)
            return "g" + this.GUID;
        return "null";
    }
}