package ennuo.craftworld.types.savedata;

import com.google.gson.Gson;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.utilities.Bytes;

public class LocalProfile implements Serializable {
    public CachedInventoryItem[] inventory;
    public StringTable stringTable;
    public boolean fromProductionBuild;
    public short[] lbp1TutorialStates;
    public short[] dlcPackViewed;
    public short[] dlcPackShown;
    public int lbp1MainMenuButtonUnlocks;
    public LevelData[] playedLevelData;
    public LevelData[] viewedLevelData;

    public Serializable serialize(Serializer serializer, Serializable structure) {
        LocalProfile profile = (structure == null) ? new LocalProfile() : (LocalProfile) structure;
        
        profile.inventory = serializer.array(profile.inventory, CachedInventoryItem.class);
        profile.stringTable = serializer.struct(profile.stringTable, StringTable.class);
        if (serializer.revision.head > 0x3b5)
            profile.fromProductionBuild = serializer.bool(profile.fromProductionBuild);
        
        profile.lbp1TutorialStates = serializer.i16a(profile.lbp1TutorialStates);
        profile.dlcPackViewed = serializer.i16a(profile.dlcPackViewed);
        profile.dlcPackShown = serializer.i16a(profile.dlcPackShown);
        
        if (serializer.revision.head > 0x1e3)
            profile.lbp1MainMenuButtonUnlocks = serializer.i32(profile.lbp1MainMenuButtonUnlocks);
        
        
        /*
        System.out.println("We are at offset " + Bytes.toHex(serializer.input.offset));
        FileIO.write(serializer.input.data, "C:/Users/Aidan/Desktop/myipr");
        Gson gson = new Gson();
        FileIO.write(gson.toJson(profile).getBytes(), "C:/Users/Aidan/Desktop/myipr.json");
        */
        
        return this;
    }
}
