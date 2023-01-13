package configurations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cwlib.util.FileIO;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public static File jarDirectory;
    static {
        try {
            Config.jarDirectory = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        } catch (Exception e) {}
    }
    
    public static Path path = Paths.get(Config.jarDirectory.getAbsolutePath(), "config.json");
    public static Config instance;
    
    public List<Profile> profiles = new ArrayList<>();
    public int currentProfile = 0;
    
    public boolean isDebug = false;
    public boolean useLegacyFileDialogue = !ApplicationFlags.IS_WINDOWS;
    public boolean displayWarningOnDeletingEntry = true;
    public boolean displayWarningOnZeroEntry = true;
    public boolean addToArchiveOnCopy = true;
    public boolean enable3D = false;
    
    public static Profile newProfile() {
        Profile profile = new Profile();
        Config.instance.profiles.add(profile);
        return profile;
    }
    public static void removeProfile(int index) { Config.instance.profiles.remove(index); }
    public Profile getCurrentProfile() { return this.profiles.get(this.currentProfile); }
    
    public static void generate() {
        Config config = new Config();
        
        Config.instance = config;
        config.currentProfile = 0;
        config.profiles.add(new Profile("Default"));
        
        Config.save();
    }
    
    public static boolean save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return FileIO.write(gson.toJson(Config.instance).getBytes(), Config.path.toString());
    }
    
    public static void initialize() {
        if (Files.exists(Config.path)) {
            try { 
                Config.instance = new Gson().fromJson(FileIO.readString(Config.path), Config.class);
                if (!ApplicationFlags.IS_WINDOWS)
                    Config.instance.enable3D = false;
                // Generate default profiles if they don't exist.
                if (Config.instance.profiles == null || Config.instance.profiles.size() == 0)
                    Config.generate();
            }
            catch (Exception ex) {
                System.err.println("Config is invalid, generating a new one.");
                Config.generate();
            }
        } else Config.generate();
    }
}
