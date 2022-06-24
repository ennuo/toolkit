package cwlib.enums;

public enum DatabaseType {
    NONE(null, null, false, false),
    FILE_DATABASE("FileDB", "map", true, false),
    BIGFART("Big Profile", "", false, true),
    SAVE("Profile Data", null, false, true),
    MOD("Mod", "mod", true, true);

    /**
     * Whether or not this database type has entries
     * with GUIDs
     */
    private final boolean hasGUIDs;
    private final boolean containsData;
    private final String name;
    private final String extension;

    private DatabaseType(String name, String extension, boolean hasKeys, boolean hasData) {
        this.name = name;
        this.extension = extension;
        this.hasGUIDs = hasKeys; 
        this.containsData = hasData;
    }

    public String getName() { return this.name; }
    public String getExtension() { return this.extension; }
    public boolean hasGUIDs() { return this.hasGUIDs; }
    public boolean containsData() { return this.containsData; }
}
