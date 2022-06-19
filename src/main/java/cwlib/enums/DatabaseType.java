package cwlib.enums;

public enum DatabaseType {
    FILE_DATABASE(true),
    BIGFART(false),
    SAVE(false),
    MOD(true);

    /**
     * Whether or not this database type has entries
     * with GUIDs
     */
    private final boolean hasGUIDs;

    private DatabaseType(boolean hasKeys) { this.hasGUIDs = hasKeys; }

    public boolean hasGUIDs() { return this.hasGUIDs; }
}
