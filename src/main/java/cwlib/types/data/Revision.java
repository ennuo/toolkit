package cwlib.types.data;

/**
 * Utilities for comparing game revisions.
 */
public final class Revision {
    public static final int LBP1_FINAL_REVISION = 0x272;
    public static final int LBPV_REVISION = 0x3e2;

    public static final int LEERDAMMER_BRANCH_ID = 0x4c44;
    public static final int DOUBLE11_BRANCH_ID = 0x4431;

    public static final int DOUBLE11_BRANCH_REVISION = 0x87;
    public static final int LEERDAMMER_BRANCH_REVISION = 0x17;

    private final int head;
    private final short branchID;
    private final short branchRevision;

    /**
     * Forms revision with branch data.
     * @param revision Head revision
     * @param branchDescription Revision branch descriptor
     */
    public Revision(int revision, int branchDescription) {
        this.head = revision;
        this.branchID = (short) (branchDescription >> 0x10);
        this.branchRevision = (short) (branchDescription & 0xFFFF);
    }

    /**
     * Forms revision with no branch data.
     * @param revision Head revision
     */
    public Revision(int revision) {
        this.head = revision;
        this.branchID = 0;
        this.branchRevision = 0;
    }

    /**
     * Forms revision with branch ID and revision.
     * @param revision Head revision
     * @param branchID Branch ID of revision
     * @param branchRevision Revision of branch
     */
    public Revision(int revision, int branchID, int branchRevision) {
        this.head = revision;
        this.branchID = (short) branchID;
        this.branchRevision = (short) branchRevision;
    }

    /**
     * Checks if the current revision is from LBP1.
     * @return Whether or not game's revision is LBP1
     */
    public boolean isLBP1() { return this.head <= Revision.LBP1_FINAL_REVISION; }

    /**
     * Checks if the current revision is from LBP1 with Branch ID of 0x4c44.
     * @return Whether or not the game's revision is branched LBP1
     */
    public boolean isLeerdammer() { 
        return this.branchID == Revision.LEERDAMMER_BRANCH_ID && this.isLBP1(); 
    }

    /**
     * Checks if the current revision is from LBP2.
     * @return Whether or not the game's revision is LBP2
     */
    public boolean isLBP2() { 
        if (this.isLBP1() || this.isLBP3() || this.isVita()) return false;
        return true;
    }

    /**
     * Checks if the current revision is from LBP Vita.
     * @return Whether or not the game's revision is LBP Vita.
     */
    public boolean isVita() { 
        return this.branchID == Revision.DOUBLE11_BRANCH_ID && this.head == Revision.LBPV_REVISION; 
    }

    /**
     * Checks if the current revision is from LBP3.
     * @return Whether or not the game's revision is LBP3.
     */
    public boolean isLBP3() { return this.head >> 0x10 != 0; }
    
    /**
     * Checks if the revision is after current.
     * @param revision Head revision to compare
     * @return Whether or not specified revision is after current
     */
    public boolean isAfterLBP3Revision(int revision) {
        if (!this.isLBP3()) return false;
        if ((this.head >> 0x10) > revision) return true;
        return false;
    }
    
    /**
     * Checks if the vita revision is after current.
     * @param revision Vita branch revision to compare
     * @return Whether or not specified vita revision is after current
     */
    public boolean isAfterVitaRevision(int revision) {
        if (!this.isVita()) return false;
        if (this.branchRevision > revision) return true;
        return false;
    }
    
    /**
     * Checks if the branch revision is after current.
     * @param branchRevision Branch revision to compare
     * @return Whether or not specified branch revision is after current
     */
    public boolean isAfterLeerdamerRevision(int branchRevision) {
        if (!this.isLeerdammer()) return false;
        if (this.branchRevision > branchRevision) return true;
        return false;
    }

    /**
     * Gets the LBP3 specific revision of the head revision.
     * @return LBP3 head revision
     */
    public int getSubVersion() { return (this.head >>> 16) & 0xFFFF; }

    /**
     * Gets the LBP1/LBP2/V revision of the head revision.
     * @return LBP1/2/V head revision
     */
    public int getVersion() { return this.head & 0xFFFF; }

    public int getHead() { return this.head; }
    public short getBranchID() { return this.branchID; }
    public short getBranchRevision() { return this.branchRevision; }


    @Override public String toString() {
        if (this.branchID != 0) {
            return String.format("Revision: (r%d, b%04X:%04X)", 
                this.head, this.branchID, this.branchRevision);
        }
        return String.format("Revision: (r%d)", this.head);
    }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Revision)) return false;
        final Revision otherRevision = (Revision) other;
        return otherRevision.head == this.head
            && otherRevision.branchID == this.branchID
            && otherRevision.branchRevision == this.branchRevision;
    }
}
