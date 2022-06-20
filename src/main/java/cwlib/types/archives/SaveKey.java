package cwlib.types.archives;

import cwlib.enums.ResourceType;
import cwlib.types.data.SHA1;


/**
 * Information about savedata stored in SaveFart.
 */
public class SaveKey {
    @SuppressWarnings("unused") private int[] deprecated1 = new int[10];
    private boolean copied;
    private ResourceType rootType = ResourceType.INVALID;
    @SuppressWarnings("unused") private int[] deprecated2 = new int[3];
    private SHA1 rootHash = new SHA1();
    @SuppressWarnings("unused") private int[] deprecated3 = new int[10];

    public boolean getCopied() { return this.copied; }
    public ResourceType getRootType() { return this.rootType; }
    public SHA1 getRootHash() { return this.rootHash; }

    public void setCopied(boolean value) { this.copied = value; }
    public void setRootType(ResourceType value) { this.rootType = value; }
    public void setRootHash(SHA1 value) { this.rootHash = value; }
}
