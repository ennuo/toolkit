package cwlib.enums;

public enum Branch {
    /**
     * Branched revision for Leerdammer update in LittleBigPlanet 1.
     */
    LEERDAMMER(0x272, 0x4c44, 0x17),

    /**
     * LittleBigPlanet Vita branched revision
     */
    DOUBLE11(0x3e2, 0x4431, 0x87),

    /**
     * Custom branched revision for Toolkit custom resources.
     */
    MIZUKI(0x021803f9, 0x4d5a, 0x5);

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
