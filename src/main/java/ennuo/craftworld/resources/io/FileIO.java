package ennuo.craftworld.resources.io;

import ennuo.toolkit.windows.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

public class FileIO {
    static String getResourceFileAsString(String fileName) throws IOException {
        System.out.println("Reading " + fileName + " from class path...");
        try (InputStream is = Toolkit.class.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    public static boolean write(byte[] data, String path) {
        try {
            System.out.println("Writing file to " + path);
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(data);
            stream.close();
        } catch (IOException ex) {
            System.err.println("Failed to write file to " + path);
            return false;
        }
        return true;
    }

    public static BufferedImage readBufferedImage(String path) {
        BufferedImage image = null;
        File file = new File(path);
        if (!file.exists()) return null;
        try {
            image = ImageIO.read(file);
        } catch (IOException ex) {
            System.err.println("Failed to read image");
        }
        return image;
    }
    
    public static String readString(Path path) {
        try {
            byte[] data = Files.readAllBytes(path);
            return new String(data);
        } catch (IOException ex) {
            System.err.println("An error occurred reading file.");
            return null;
        }
    }

    public static byte[] read(String path) {
        try {
            System.out.println("Reading file at " + path);

            File file = new File(path);

            FileInputStream stream = new FileInputStream(file);

            long size = file.length();
            byte[] buffer = new byte[(int) size];
            stream.read(buffer);

            stream.close();
            return buffer;
        } catch (IOException ex) {
            System.err.println("Failed to read file at path (" + path + "), does it exist?");
            return null;
        }
    }
}