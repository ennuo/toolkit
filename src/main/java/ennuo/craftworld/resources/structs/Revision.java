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
    
    public boolean isLBP1() { return this.head <= 0x272; }
    public boolean isLBP1Branched() { return this.branchID == 0x4c44 && this.isLBP1(); }
    public boolean isLBP2() { 
        if (this.isLBP1() || this.isLBP3() || this.isVita()) return false;
        return true;
    }
    public boolean isVita() { return this.branchID == 0x4431 && this.head == 0x3e2; }
    public boolean isLBP3() { return this.head >> 0x10 != 0; }
    
    public boolean isAfterLBP3Revision(int revision) {
        if (!this.isLBP3()) return false;
        if ((this.head >> 0x10) > revision) return true;
        return false;
    }
    
    public boolean isAfterVitaBranchedRevision(int branchRevision) {
        if (!this.isVita()) return false;
        if (this.branchRevision > branchRevision) return true;
        return false;
    }
    
    public boolean isAfterLBP1BranchedRevision(int branchRevision) {
        if (this.branchID != 0x4c44) return false;
        if (this.branchRevision > branchRevision) return true;
        return false;
    }
}
