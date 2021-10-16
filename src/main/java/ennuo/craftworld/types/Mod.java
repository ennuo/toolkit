package ennuo.craftworld.types;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Images;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.patches.ModPatch;
import ennuo.craftworld.resources.enums.ModCompatibility;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.things.InventoryMetadata;
import ennuo.craftworld.things.Serializer;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Mod extends FileData {
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
    
    public ArrayList<InventoryMetadata> items = new ArrayList<InventoryMetadata>();
    public ArrayList<FileEntry> entries = new ArrayList<FileEntry>();
    public ArrayList<Slot> slots = new ArrayList<Slot>();
    public ArrayList<ModPatch> patches = new ArrayList<ModPatch>();
    
    public boolean isParsed = false;
    public boolean isProtected = false;
    
    
    public int size = 0xFFFF;
    
    public Mod() { model = new FileModel(new FileNode("MOD", null, null)); root = (FileNode)model.getRoot(); }
    public Mod(File file, Data data, String key) {
        this();
        type = "Mod";
        name = file.getName();
        path = file.getAbsolutePath();
        process(data, key);
    }
    
    public void add(FileEntry entry) {
        entries.add(entry);
        size += (8 + entry.path.length()); 
        if (entry.data != null)
            size += entry.data.length;
    }
    
    public void add(String path, byte[] data) { add(path, data, getNextGUID()); }
    public void add(String path, byte[] data, long GUID) {
        byte[] hash = Bytes.SHA1(data);
        
        FileEntry old = find(GUID);
        if (old != null) {
            old.timestamp = System.currentTimeMillis() / 1000L;
            old.hash = hash;
            old.size = data.length;
            old.data = data;
            old.resetResources();
            size += path.length();
            size += data.length;
            return;
        }
        
        FileEntry hashDupe = find(hash);
        if (hashDupe != null) {
            if (hashDupe.path.equals(path))
                return;
        }
        
        FileEntry entry = new FileEntry(data, Bytes.SHA1(data));
        entry.path = path;
        entry.GUID = GUID;
        entries.add(entry);
        if (data != null)
            size += data.length;
        size += path.length();
        size += 8;
        
        addNode(entry);
    }
    
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
        
        if (revision > 5) data.revision = 0x01ae03fa;
        else data.revision = 0xFFFF;
        
        if (revision > Mod.MAX_REVISION) {
            System.err.println(String.format("This mod file (v%s) isn't supported with your version of Craftworld Toolkit (v%s), are you out of date?", String.valueOf(revision), String.valueOf(Mod.MAX_REVISION)));
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
                size += entry.size + (0xFFFF);
                entries.add(entry);
                addNode(entry);
            }
        }
        
        int itemCount;
        if (revision > 5) itemCount = data.i32();
        else itemCount = data.i16();
        System.out.println("Mod has " + itemCount + " inventory patches");
        if (itemCount != 0) {
            items = new ArrayList<InventoryMetadata>(itemCount);
            for (int i = 0; i < itemCount; ++i) {
                InventoryMetadata item = new Serializer(data).ParseMetadata();
                item.resource = data.resource(RType.PLAN, true);
                item.translatedLocation = data.str16();
                item.translatedCategory = data.str16();
                if (revision > 4) {
                    item.minRevision = data.i32();
                    item.maxRevision = data.i32();
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
                    slots.add(new Slot(data, true, false));
            }
        }
        
        int patchCount = data.i32();
        System.out.println("Mod has " + patchCount + " file patches");
        for (int i = 0; i < patchCount; ++i)
            patches.add(ModPatch.deserialize(data));
        
        for (int i = 0; i < entryCount; ++i) {
            entries.get(i).data = data.bytes(entries.get(i).size);
            entries.get(i).hash = Bytes.SHA1(entries.get(i).data);
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
                Logger.getLogger(Mod.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    public FileEntry find(byte[] hash) {
        if (hash == null) return null;
        for (int i = 0; i < entries.size(); i++)
            if (Arrays.equals(hash, entries.get(i).hash))
                return entries.get(i);
        return null;
    }
    
    public FileEntry find(long GUID) {
        if (GUID == -1) return null;
        for (int i = 0; i < entries.size(); ++i)
            if (entries.get(i).GUID == GUID)
                return entries.get(i);
        return null;
    }
    
    public void replace(FileEntry entry, byte[] buffer) {
        entry.data = buffer;
        entry.size = buffer.length;
        byte[] oldHash = entry.hash;
        byte[] newHash = Bytes.SHA1(buffer);
        entry.hash = newHash;
        entry.timestamp = System.currentTimeMillis() / 1000L;
        size += buffer.length;
        shouldSave = true;
        for (Slot slot : slots)
            if (slot.root != null && Arrays.equals(oldHash, slot.root.hash))
                slot.root.hash = newHash;
        for (InventoryMetadata item : items)
            if (item.resource != null && Arrays.equals(oldHash, item.resource.hash))
                item.resource.hash = newHash;
    }
    
    public void remove(FileEntry entry) { entries.remove(entry); }
    
    public byte[] extract(byte[] sha1) {
        FileEntry entry = find(sha1);
        if (entry == null) return null;
        return entry.data;
    }
    
    public boolean save(String path) { return save(path, true); }
    public boolean save(String path, boolean encrypt) {
        revision = Mod.MAX_REVISION;
        Output output = new Output(size + (0xFF * entries.size()), 0xFFFF);
        if (revision > 5) output.revision = 0x01ae03fa;
        
        output.str("MODb");
        output.i8(revision); output.u8(compatibility.value);
        output.u8(major); output.u8(minor);
        output.str8(modID);
        if (revision > 5) output.str16(author);
        else output.str8(author);
        output.str16(title);
        output.str16(description);
        
        output.i32(entries.size());
        for (int i = 0; i < entries.size(); ++i) {
            FileEntry entry = entries.get(i);
            output.str8(entry.path);
            output.i32(entry.data.length);
            output.u32(entry.GUID);
            if (revision > 3)
                output.u32(entry.timestamp);
        }
        
        if (revision > 5) output.i32(items.size());
        else output.i16((short) items.size());
        for (int i = 0; i < items.size(); ++i) {
            InventoryMetadata metadata = items.get(i);
            new Serializer(output).serializeMetadata(metadata, true);
            output.resource(metadata.resource, true);
            output.str16(metadata.translatedLocation);
            output.str16(metadata.translatedCategory);
            if (revision > 4) {
                output.i32(metadata.minRevision);
                output.i32(metadata.maxRevision);
            }
        }
        
        if (revision > 3) {
            output.i32(slots.size());
            for (Slot slot : slots)
                slot.serialize(output, true, false);
        }
        
        // patches aren't supported yet by this tool, so remove any that exist. //
        output.i32(0);
        
        for (int i = 0; i < entries.size(); ++i)
            output.bytes(entries.get(i).data);
        
        //icon
        if (icon == null) output.i32(0);
        else {
           BufferedImage out = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
           Graphics g = out.createGraphics();
           icon.paintIcon(null, g, 0, 0);
           g.dispose();
           
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(out, "png", baos);
                baos.flush();
                byte[] img = baos.toByteArray();
                output.i32(img.length);
                output.bytes(img);
            } catch (IOException ex) {
                System.err.println("Failed to write icon.");
                output.i32(0);
            }
        }
       
        
        output.shrink();
        
        
        if (!encrypt || !isProtected) {
            FileIO.write(output.buffer, path);
            if (path.equals(this.path)) shouldSave = false;
            return true;
        }
        
        if (password == null || password.equals(""))
            password = Mod.DEFAULT_PASSWORD;
        
        byte[] encrypted = Bytes.Encrypt(password, output.buffer);
        
        
        Output o = new Output(encrypted.length + 5);
        o.str("MODe");
        o.bool(!password.equals(DEFAULT_PASSWORD));
        o.bytes(encrypted);
        
        FileIO.write(o.buffer, path);
        
        if (path.equals(this.path)) shouldSave = false;
        
        return true;
    }
    
    
    
    
}
