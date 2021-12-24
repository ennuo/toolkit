package ennuo.toolkit.configurations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ennuo.craftworld.resources.io.FileIO;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public static File jarDirectory;
    static {
        try {
            Config.jarDirectory = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        } catch (Exception e) {}
    }
    public static Path path = Paths.get(Config.jarDirectory.getAbsolutePath(), "config.json");
    public static Config instance;
    
    public Profile[] profiles = new Profile[] {};
    public String currentProfile = "";
    public boolean useLegacyFileDialogue = false;
    
    public Profile getCurrentProfile() {
        if (currentProfile.isEmpty() || profiles == null) 
            return null;
        for (Profile profile : this.profiles)
            if (this.currentProfile.equals(profile.name))
                return profile;
        return null;
    }
    
    public static void generate() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Config.instance = new Config();
        FileIO.write(gson.toJson(Config.instance).getBytes(), Config.path.toString());
    }
    
    public static void initialize() {
        if (Files.notExists(Config.path)) Config.generate();
        else {
            try {
                Config.instance = new Gson().fromJson(FileIO.readString(Config.path), Config.class);
            } catch (Exception e) {
                System.err.println("An error occurred reading config, attempting to generate new one.");
                Config.generate();
            }
        }
    }
}
