package cwlib;

public class ConfigShared 
{
    public static ConfigShared instance = new ConfigShared();

    public static ExportSettings export() { return instance.export; }
    
    public ExportSettings export = new ExportSettings();
}
