package configurations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cwlib.ConfigShared;
import cwlib.CwlibConfiguration;
import cwlib.ExportSettings;
import cwlib.SearchSettings;
import cwlib.resources.RTranslationTable;
import cwlib.util.FileIO;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config extends ConfigShared
{
    public static Path path = Paths.get(CwlibConfiguration.JAR_DIRECTORY.getAbsolutePath(),
        "config.json");


    private static HashMap<Long, String> _translations = new HashMap<>();
    
    public static Config instance;

    public static PusherSettings pusher() { return instance.pusher; }
    public static SyncSettings sync() { return instance.sync; }
    public static ExportSettings export() { return instance.export; }
    
    public PusherSettings pusher = new PusherSettings();
    public SyncSettings sync = new SyncSettings();
    
    public List<Profile> profiles = new ArrayList<>();
    public int currentProfile = 0;
    public boolean showAlearData = true;
    public boolean isDebug = false;
    public boolean useLegacyFileDialogue = !CwlibConfiguration.IS_WINDOWS;
    public boolean displayWarningOnDeletingEntry = true;
    public boolean displayWarningOnZeroEntry = true;
    public boolean addToArchiveOnCopy = true;
    public boolean enable3D = false;

    public static void loadTranslations(File file)
    {
        var lines = FileIO.readString(Path.of(file.getAbsolutePath())).split("\n");
        for (var line : lines)
        {
            line = line.trim();
            _translations.put(RTranslationTable.makeLamsKeyID(line), line);
        }
    }

    public static String getTranslation(long key)
    {
        return _translations.getOrDefault(key, Long.toUnsignedString(key));
    }

    public static Profile newProfile()
    {
        Profile profile = new Profile();
        Config.instance.profiles.add(profile);
        return profile;
    }

    public static void removeProfile(int index)
    {
        Config.instance.profiles.remove(index);
    }

    public Profile getCurrentProfile()
    {
        return this.profiles.get(this.currentProfile);
    }

    public static void generate()
    {
        Config config = new Config();

        Config.instance = config;
        ConfigShared.instance = config;
        config.currentProfile = 0;
        config.profiles.add(new Profile("Default"));

        Config.save();
    }

    public static boolean save()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return FileIO.write(gson.toJson(Config.instance).getBytes(), Config.path.toString());
    }

    public static void initialize()
    {
        var keys = new File(CwlibConfiguration.JAR_DIRECTORY, "keys.txt");
        if (keys.exists())
            loadTranslations(keys);

        if (Files.exists(Config.path))
        {
            try
            {
                Config.instance = new Gson().fromJson(FileIO.readString(Config.path),
                    Config.class);
                ConfigShared.instance = Config.instance;
                if (!CwlibConfiguration.IS_WINDOWS)
                    Config.instance.enable3D = false;
                // Generate default profiles if they don't exist.
                if (Config.instance.profiles == null || Config.instance.profiles.size() == 0)
                    Config.generate();
            }
            catch (Exception ex)
            {
                System.err.println("Config is invalid, generating a new one.");
                Config.generate();
            }
        }
        else Config.generate();
    }
}
