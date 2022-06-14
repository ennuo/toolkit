package toolkit.functions;

import cwlib.util.Bytes;
import toolkit.windows.Toolkit;
import cwlib.io.streams.MemoryInputStream;
import cwlib.util.FileIO;
import cwlib.types.FileEntry;
import cwlib.types.mods.legacy.LegacyMod;
import cwlib.types.mods.Mod;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.JOptionPane;

public class ModCallbacks {
    public static Mod loadMod(File file) {
        final int ZIP_HEADER = 1347093252;
        final int ENCRYPTED_MOD_HEADER = 1297040485;
        final int MOD_HEADER = 1297040482;
        
        int header = 0;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] magic = new byte[4];
            raf.read(magic);
            header = Bytes.toInteger(magic);
        } catch (IOException e) { 
            System.err.println("An error occured accessing the file."); 
        }
        if (header == ENCRYPTED_MOD_HEADER || header == MOD_HEADER) {
            LegacyMod legacyMod;
            MemoryInputStream data = new MemoryInputStream(FileIO.read(file.getAbsolutePath()), 0xFFFF);
            data.offset = 4;
            String password = null;
            if (header == ENCRYPTED_MOD_HEADER && data.bool() == true)
                password = JOptionPane.showInputDialog(Toolkit.instance, "Mod is encrypted! Please input password.", "password");
            data.seek(0);
            legacyMod = new LegacyMod(file, data, password);
            Mod mod = new Mod();
            mod.path = file.getAbsolutePath();
            mod.name = file.getName();
            for (FileEntry entry : legacyMod.entries) {
                mod.add(entry);
                mod.addNode(entry);
            }
            mod.config.title = legacyMod.title;
            mod.config.ID = legacyMod.modID;
            mod.config.author = legacyMod.author;
            mod.config.description = legacyMod.description;
            mod.config.version = legacyMod.major + "." + legacyMod.minor;
            mod.icon = legacyMod.icon;
            return mod;
        } else if (header == ZIP_HEADER) return new Mod(file);
        return null;
    }
}
