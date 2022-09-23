package toolkit.configurations;

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
    
    // Use Profile.useLegacyFileDialogue instead.
    @Deprecated public boolean useLegacyFileDialogue = false;
    
    public static void removeProfile(int index) {
        Config.instance.profiles.remove(index);
    }
    
    public static Profile newProfile() {
        Profile profile = new Profile();
        Config.instance.profiles.add(profile);
        return profile;
    }
    
    public static boolean save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return FileIO.write(gson.toJson(Config.instance).getBytes(), Config.path.toString());
    }
    
    public Profile getCurrentProfile() {
        return this.profiles.get(this.currentProfile);
    }
    
    public static void generate() {
        Config config = new Config();
        Config.instance = config;
        
        config.currentProfile = 0;
        Profile profile = new Profile();
        profile.name = "Default";
        config.profiles.add(profile);
        
        Config.save();
    }
    
    public static void initialize() {
        if (Files.notExists(Config.path)) Config.generate();
        else {
            try {
                Config.instance = new Gson().fromJson(FileIO.readString(Config.path), Config.class);
                
                // We need at least the default profile.
                if (Config.instance.profiles == null || Config.instance.profiles.size() == 0)
                    Config.generate();
            } catch (Exception e) {
                System.err.println("An error occurred reading config, attempting to generate new one.");
                Config.generate();
            }
        }
    }
}
