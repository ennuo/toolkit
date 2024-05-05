package cwlib.util;

import cwlib.singleton.ResourceSystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class FileIO
{
    public static String getResourceFileAsString(String fileName)
    {
        try (InputStream is = FileIO.class.getResourceAsStream(fileName))
        {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader =
                new BufferedReader(isr))
            {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
        catch (Exception ex) { return null; }
    }

    public static byte[] getResourceFile(String filename)
    {
        try (InputStream stream = FileIO.class.getResourceAsStream(filename))
        {
            if (stream == null) return null;
            return stream.readAllBytes();
        }
        catch (Exception ex) { return null; }
    }

    public static boolean write(byte[] data, String path)
    {
        File file = new File(path);
        if (file.getParentFile() != null)
            file.getParentFile().mkdirs();
        try
        {
            ResourceSystem.println("FileIO", "Writing file to " + path);
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(data);
            stream.close();
        }
        catch (IOException ex)
        {
            ResourceSystem.println("FileIO", "Failed to write file to " + path);
            return false;
        }
        return true;
    }

    public static BufferedImage readBufferedImage(String path)
    {
        BufferedImage image = null;
        File file = new File(path);
        if (!file.exists()) return null;
        try
        {
            image = ImageIO.read(file);
        }
        catch (IOException ex)
        {
            ResourceSystem.println("FileIO", "Failed to read image");
        }
        return image;
    }

    public static String readString(Path path)
    {
        try
        {
            byte[] data = Files.readAllBytes(path);
            return new String(data, StandardCharsets.UTF_8);
        }
        catch (IOException ex)
        {
            ResourceSystem.println("FileIO", "An error occurred reading file.");
            return null;
        }
    }

    public static byte[] read(String path)
    {
        try
        {
            ResourceSystem.println("FileIO", "Reading file at " + path);

            File file = new File(path);

            FileInputStream stream = new FileInputStream(file);

            long size = file.length();
            byte[] buffer = new byte[(int) size];
            stream.read(buffer);

            stream.close();
            return buffer;
        }
        catch (IOException ex)
        {
            ResourceSystem.println("FileIO", "Failed to read file at path (" + path + "), " +
                                             "does it" +
                                             " exist?");
            return null;
        }
    }
}