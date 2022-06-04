package ennuo.craftworld.types.mods.legacy;

import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.utilities.Images;
import ennuo.craftworld.types.mods.legacy.patches.ModPatch;
import ennuo.craftworld.resources.enums.ModCompatibility;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.FileEntry;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * This should only be used for upgrading old mods to the new format.
 * @deprecated
 */
@Deprecated
public class LegacyMod {
    public static final String DEFAULT_PASSWORD = "purchasecollege";
    public static final byte MAX_REVISION = 6;
    
    public String modID = "my_mod";
    public byte revision = 6;
    public ModCompatibility compatibility = ModCompatibility.ALL;
    
    public int major = 1;
    public int minor = 0;
    
    public String title = "Mod";
    public String author = "Sackthing";
    public String description = "Standard mod template.";
   
    public ImageIcon icon;
    
    public String password;
    
    public ArrayList<InventoryDetails> items = new ArrayList<InventoryDetails>();
    public ArrayList<FileEntry> entries = new ArrayList<FileEntry>();
    public ArrayList<Slot> slots = new ArrayList<Slot>();
    public ArrayList<ModPatch> patches = new ArrayList<ModPatch>();
    
    public boolean isParsed = false;
    public boolean isProtected = false;
    
    public LegacyMod() {}
    public LegacyMod(File file, Data data, String key) { process(data, key); }
    
    private void process(Data data, String key) {
        String magic = data.str(4);
        if (magic.equals("MODe")) {
            isProtected = true;
            if (!data.bool())
                key = DEFAULT_PASSWORD;
            System.out.println("Mod is encrypted, attempting to decrypt with key provided.");
            byte[] decrypted = Bytes.Decrypt(key, data.bytes(data.length - 5));
            data.setData(decrypted);
            if (data.str(0x4).equals("MODb"))
                System.out.println("Successfully decrypted mod!");
            else return;
            
        } else if (!magic.equals("MODb")) return;
        else System.out.println("Mod is not encrypted");

        revision = data.i8();
        
        if (revision > 5) data.revision.head = 0x01ae03fa;
        else data.revision.head = 0xFFFF;
        
        if (revision > LegacyMod.MAX_REVISION) {
            System.err.println(String.format("This mod file (v%s) isn't supported with your version of Craftworld Toolkit (v%s), are you out of date?", String.valueOf(revision), String.valueOf(LegacyMod.MAX_REVISION)));
            isParsed = false;
            return;
        }
        
        
        compatibility = ModCompatibility.getValue(data.i8());
        
        System.out.println(String.format("Mod has revison = %d, compatibility = %d (%s)", revision, compatibility.value, compatibility.name()));
        
        major = data.i8();
        minor = data.i8();
        
        modID = data.str8();
        
        System.out.println(String.format("Mod has version = %02d.%02d", major, minor));
        
        if (revision > 5) author = data.str16();
        else author = data.str8();

        title = data.str16();
        description = data.str16();
        
        System.out.println(String.format("Mod has author = %s, title = %s, description = %s", author, title, description));
        
        int entryCount = data.i32();
        System.out.println("Mod has " + entryCount + " entries");
        if (entryCount != 0) {
            entries = new ArrayList<FileEntry>(entryCount);
            for (int i = 0; i < entryCount; ++i) {
                FileEntry entry = new FileEntry(data.str8(), data.i32(), data.u32());
                if (revision > 3) entry.timestamp = data.u32();
                else entry.timestamp = 0;
                entries.add(entry);
            }
        }
        
        int itemCount;
        if (revision > 5) itemCount = data.i32();
        else itemCount = data.i16();
        System.out.println("Mod has " + itemCount + " inventory patches");
        if (itemCount != 0) {
            items = new ArrayList<InventoryDetails>(itemCount);
            for (int i = 0; i < itemCount; ++i) {
                InventoryDetails item = new Serializer(data).struct(null, InventoryDetails.class);
                item.location = data.u32();
                item.category = data.u32();
                item.translatedLocation = data.str16();
                item.translatedCategory = data.str16();
                if (revision > 4) {
                    // NOTE(Aidan): min/max revision has been depreciated.
                    data.i32();
                    data.i32();
                }
                items.add(item);
            }
        }
        
        if (revision > 3) {
            int slotCount = data.i32();
            System.out.println("Mod has " + slotCount + " slots");
            if (slotCount != 0) {
                slots = new ArrayList<Slot>(slotCount);
                for (int i = 0; i < slotCount; ++i)
                    slots.add(new Serializer(data).struct(null, Slot.class));
            }
        }
        
        int patchCount = data.i32();
        System.out.println("Mod has " + patchCount + " file patches");
        for (int i = 0; i < patchCount; ++i)
            patches.add(ModPatch.deserialize(data));
        
        for (int i = 0; i < entryCount; ++i) {
            entries.get(i).data = data.bytes(entries.get(i).size);
            entries.get(i).hash = SHA1.fromBuffer(entries.get(i).data);
        }
        
        int imageSize = data.i32();
        if (imageSize == 0) {
            System.out.println("Mod has no image file");
            isParsed = true;
            return;
        } else {
            try {
                byte[] image = data.bytes(imageSize);
                InputStream in = new ByteArrayInputStream(image);
                BufferedImage img = ImageIO.read(in);
                if (img != null)
                    icon = Images.getImageIcon(img);
                isParsed = true;
            } catch (IOException ex) {
                Logger.getLogger(LegacyMod.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
}
