package ennuo.toolkit.utilities;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import jnafilechooser.api.JnaFileChooser;
import jnafilechooser.api.JnaFileChooser.Mode;

public class FileChooser {
    //public JFileChooser fileDialogue;

    public JnaFileChooser fileDialogue;


    private FileFilter filter;

    private Window frame;

    public FileChooser(Window frame) {
        this.fileDialogue = new JnaFileChooser();
        this.fileDialogue.setCurrentDirectory(Paths.get(System.getProperty("user.home") + "/Documents").toAbsolutePath().toString());
        this.frame = frame;
    }

    public File openFile(String name, String ext, String desc, boolean saveFile) {
        this.fileDialogue = new JnaFileChooser();
        System.out.println("Waiting for user to select file...");
        if (setupFilter(name, ext, desc, false, false) && (
                saveFile ? this.fileDialogue.showSaveDialog(this.frame) : this.fileDialogue.showOpenDialog(this.frame)))
            return this.fileDialogue.getSelectedFile();
        System.out.println("Cancelling operation, user did not select a file.");
        return null;
    }

    public File[] openFiles(String ext, String desc) {
        this.fileDialogue = new JnaFileChooser();
        System.out.println("Waiting for user to select files...");
        if (setupFilter("", ext, desc, true, false) &&
            this.fileDialogue.showOpenDialog(this.frame))
            return this.fileDialogue.getSelectedFiles();
        System.out.println("Cancelling operation, user did not select any files.");
        return null;
    }

    public String openDirectory() {
        this.fileDialogue = new JnaFileChooser();
        System.out.println("Waiting for user to select directory...");
        if (setupFilter("", "", "", false, true) &&
            this.fileDialogue.showOpenDialog(this.frame))
            return this.fileDialogue.getSelectedFile().getAbsolutePath() + "\\";
        System.out.println("Cancelling operation, user did not select a directory.");
        return null;
    }

    private boolean setupFilter(String name, final String ext, final String desc, boolean mult, boolean dirs) {
        if (dirs)
            this.fileDialogue.setMode(Mode.Directories);
        if (!name.equals("") && name != null)
            this.fileDialogue.setDefaultFileName(name);
        if (ext.equals("") || desc.equals("")) {
            this.fileDialogue.addFilter("All Files", "*");
            return true;
        }
        this.fileDialogue.addFilter(desc, ext);
        this.fileDialogue.addFilter("All Files", "*");
        return true;
    }
}