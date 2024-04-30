package toolkit.utilities;

import configurations.Config;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import toolkit.windows.Toolkit;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Paths;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.tinyfd.TinyFileDialogs.*;

public class FileChooser
{
    public static final JFileChooser chooser = new JFileChooser();

    public static String getHomePath(String name)
    {
        return Paths.get(System.getProperty("user.home"), "Documents", name).toAbsolutePath().toString();
    }

    public static File openFile(String name, String ext, boolean saveFile)
    {
        File[] files = openFile(name, ext, saveFile, false);
        if (files == null) return null;
        return files[0];
    }

    public static File[] openFiles(String name, String ext)
    {
        return openFile(name, ext, false, true);
    }

    public static File[] openFiles(String ext)
    {
        return openFile(null, ext, false, true);
    }

    public static File[] openFileLegacy(String name, String[] extensions, boolean saveFile,
                                        boolean multiple)
    {
        int returnValue = JFileChooser.CANCEL_OPTION;
        setupFilter(name, extensions, multiple, false);

        if (!saveFile)
            returnValue = chooser.showOpenDialog(Toolkit.INSTANCE);
        else
            returnValue = chooser.showSaveDialog(Toolkit.INSTANCE);

        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            if (multiple) return chooser.getSelectedFiles();
            else return new File[] { chooser.getSelectedFile() };
        }
        else
            System.out.println("File operation was cancelled by user.");

        return null;
    }


    public static File[] openFile(String name, String ext, boolean saveFile, boolean multiple)
    {
        String[] extensions = new String[0];
        if (ext != null)
            extensions = ext.split(",");
        if (name != null)
            name = getHomePath(name);
        File[] files = null;
        if (Config.instance.useLegacyFileDialogue)
            return openFileLegacy(name, extensions, saveFile, multiple);
        try (MemoryStack stack = stackPush())
        {
            PointerBuffer patterns = null;
            if (extensions != null && extensions.length != 0)
            {
                patterns = stack.mallocPointer(extensions.length);
                for (String extension : extensions)
                    patterns.put(stack.UTF8("*." + extension));
                patterns.flip();
            }

            String[] paths = null;
            if (saveFile)
            {
                String path = tinyfd_saveFileDialog("Save", name, patterns, "");
                if (path != null) paths = path.split("\\|");
                else
                {
                    System.out.println("File operation was cancelled by user.");
                    return null;
                }
            }
            else
            {
                String path = tinyfd_openFileDialog("Open File(s)", name, patterns, null,
                    multiple);
                if (path != null) paths = path.split("\\|");
                else
                {
                    System.out.println("File operation was cancelled by user.");
                    return null;
                }
            }

            if (paths.length == 0)
            {
                System.out.println("File operation was cancelled by user.");
                return null;
            }

            files = new File[paths.length];
            for (int i = 0; i < files.length; ++i)
                files[i] = new File(paths[i]);
        }
        return files;
    }

    private static boolean setupFilter(String name, final String[] extensions, boolean mult,
                                       boolean dirs)
    {
        chooser.resetChoosableFileFilters();
        chooser.setSelectedFile(new File(""));
        chooser.setCurrentDirectory(new File(getHomePath("")));

        if (dirs)
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        else
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (name != null && !name.equals(""))
            chooser.setSelectedFile(new File(name));
        chooser.setMultiSelectionEnabled(mult);
        if (extensions != null)
        {
            for (String extension : extensions)
            {
                if (extension == null || extension.isEmpty()) continue;
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("*." + extension,
                    extension));
            }
        }
        chooser.setAcceptAllFileFilterUsed(true);
        return true;
    }

    public static String openDirectoryLegacy()
    {
        setupFilter(null, null, false, true);
        if (chooser.showOpenDialog(Toolkit.INSTANCE) == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile().getAbsolutePath();
        return null;
    }

    public static String openDirectory()
    {
        System.out.println("Waiting for user to select directory...");
        String directory = null;
        if (Config.instance.useLegacyFileDialogue)
            directory = openDirectoryLegacy();
        else directory = tinyfd_selectFolderDialog("Select folder", "");
        if (directory == null)
            System.out.println("File operation was cancelled by user.");
        return directory;
    }
}