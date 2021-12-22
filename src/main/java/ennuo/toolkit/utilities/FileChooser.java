package ennuo.toolkit.utilities;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import org.lwjgl.PointerBuffer;
import static org.lwjgl.system.MemoryUtil.*;
import org.lwjgl.util.nfd.NFDPathSet;
import static org.lwjgl.util.nfd.NativeFileDialog.*;

public class FileChooser {
    
    public static String getHomePath(String name) {
        return Paths.get(System.getProperty("user.home"), "Documents", name).toAbsolutePath().toString();
    }
    
    
    public static File openFile(String name, String ext, boolean saveFile) {
        PointerBuffer path = memAllocPointer(1);
        String home = FileChooser.getHomePath(name);
        int result;
        File file = null;
        System.out.println("Waiting for user to select file...");
        try {
            if (saveFile) result = NFD_SaveDialog(ext, home, path);
            else result = NFD_OpenDialog(ext, home, path);
            switch (result) {
                case NFD_OKAY:
                    file = new File(path.getStringUTF8());
                    nNFD_Free(path.get(0));
                    break;
                case NFD_CANCEL: System.out.println("User cancelled file operation."); break;
                default: System.err.format("Error: %s\n", NFD_GetError()); break;
            }   
        } finally { memFree(path); }
        return file;
    }
    
    public static File[] openFiles(String ext) {
        File[] files = null;
        System.out.println("Waiting for user to select files...");
        try (NFDPathSet paths = NFDPathSet.calloc()) {
           switch (NFD_OpenDialogMultiple(ext, null, paths)) {
                case NFD_OKAY:
                    int count = (int) NFD_PathSet_GetCount(paths);
                    files = new File[count];
                    for (int i = 0; i < count; ++i)
                        files[i] = new File(NFD_PathSet_GetPath(paths, i));
                    NFD_PathSet_Free(paths);
                    break;
                case NFD_CANCEL: System.out.println("User cancelled file operation."); break;
                default: System.err.format("Error: %s\n", NFD_GetError()); break;
           }
        }
        return files;
    }

    public static String openDirectory() {
        PointerBuffer path = memAllocPointer(1);
        String directory = null;
        System.out.println("Waiting for user to select directory...");
        try {
            switch (NFD_PickFolder((ByteBuffer)null, path)) {
                case NFD_OKAY:
                    directory = path.getStringUTF8();
                    nNFD_Free(path.get(0));
                    break;
                case NFD_CANCEL: System.out.println("User cancelled file operation."); break;
                default: System.err.format("Error: %s\n", NFD_GetError()); break;
            }   
        } finally { memFree(path); }
        return directory;
    }
}