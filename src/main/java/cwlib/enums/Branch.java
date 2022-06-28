package cwlib.enums;

public enum Branch {
    /**
     * Branched revision for Leerdammer update in LittleBigPlanet 1.
     * Tag: LD
     */
    LEERDAMMER(Revisions.LD_HEAD, 0x4c44, Revisions.LD_MAX),

    /**
     * LittleBigPlanet Vita branched revision
     * Tag: D1
     */
    DOUBLE11(Revisions.D1_HEAD, 0x4431, Revisions.D1_MAX),

    /**
     * Custom branched revision for Toolkit custom resources.
     * Tag: MZ
     */
    MIZUKI(Revisions.MZ_HEAD, 0x4d5a, Revisions.MZ_MAX);

    private final int head;
    private final short id;
    private final short revision;

    private Branch(int head, int id, int revision) {
        this.head = head;
        this.id = (short) id;
        this.revision = (short) revision;
    }

    public int getHead() { return this.head; }
    public short getID() { return this.id; }
    public short getRevision() { return this.revision; }

    public static Branch fromID(short ID) {
        for (Branch branch : Branch.values())
            if (branch.getID() == ID) return branch;
        return null;
    }
}
