package toolkit.functions;

import cwlib.util.Bytes;
import cwlib.types.mods.Mod;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ModCallbacks {
    public static Mod loadMod(File file) {
        final int ZIP_HEADER = 1347093252;
        final int ENCRYPTED_MOD_HEADER = 1297040485;
        final int MOD_HEADER = 1297040482;
        
        int header = 0;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] magic = new byte[4];
            raf.read(magic);
            header = Bytes.toIntegerBE(magic);
        } catch (IOException e) { 
            System.err.println("An error occured accessing the file."); 
        }
        if (header == ENCRYPTED_MOD_HEADER || header == MOD_HEADER)
            return Mod.fromLegacyMod(file);
        else if (header == ZIP_HEADER) return new Mod(file);
        return null;
    }
}
