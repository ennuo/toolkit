package cwlib;

public class ConfigShared 
{
    public static ConfigShared instance = new ConfigShared();

    public static ExportSettings export() { return instance.export; }
    public static SearchSettings search() { return instance.search; }

    public ExportSettings export = new ExportSettings();
    public SearchSettings search = new SearchSettings();
}
