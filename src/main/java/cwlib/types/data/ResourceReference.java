package cwlib.types.data;

import cwlib.enums.ResourceType;
import cwlib.util.Strings;

public class ResourceReference {
    public ResourceType type = ResourceType.FILE_OF_BYTES;
    public long GUID = -1;
    public SHA1 hash = null;
    public int flags;
    
    public ResourceReference() {}
    public ResourceReference(ResourceType type, String str) {
        if (Strings.isGUID(str))
            this.GUID = Strings.getLong(str);
        else if (Strings.isSHA1(str))
            this.hash = Strings.getSHA1(str);
        else throw new IllegalArgumentException("Invalid resource reference passed into resource descriptor!");

        this.type = type;
    }
    
    public ResourceReference(long GUID, ResourceType type) {
        this.GUID = GUID;
        this.type = type;
    }
    
    public ResourceReference(SHA1 hash, ResourceType type) {
        this.hash = hash;
        this.type = type;
    }
    
    @Override public int hashCode() { return this.toString().hashCode(); }

    @Override public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ResourceReference)) return false;
        ResourceReference descriptor = (ResourceReference)o;
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