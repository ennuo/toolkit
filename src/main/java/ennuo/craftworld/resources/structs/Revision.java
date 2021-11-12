package ennuo.craftworld.resources.structs;

public class Revision {
    public int head;
    public short branchID;
    public short branchRevision;
    
    public Revision(int revision, int branchDescription) {
        this.head = revision;
        this.branchID = (short) (branchDescription >> 0x10);
        this.branchRevision =  (short) (branchDescription & 0xFFFF);
    }
    
    public Revision(int revision) { this.head = revision; }
    
    public Revision(int revision, int branchID, int branchRevision) {
        this.head = revision;
        this.branchID = (short) branchID;
        this.branchRevision = (short) branchRevision;
    }
    
    public boolean isVita() { return this.branchID == 0x4431; }
}
