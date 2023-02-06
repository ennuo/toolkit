package cwlib.util;

import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;
import java.util.regex.Pattern;

public final class Strings {
    private static final Pattern SHA1_REGEX = Pattern.compile("(h?)[a-fA-F0-9]{40}$");
    private static final Pattern GUID_REGEX = Pattern.compile("(g?)\\d+");
    private static final Pattern HEX_GUID_REGEX = Pattern.compile("(g?)(0x|0X)[a-fA-F0-9]+$");
    
    /**
     * Left pads the input with zeros.
     * @param text String to pad
     * @param size Number of characters to pad
     * @return Padded string
     */
    public static final String leftPad(String text, int size) {
        StringBuilder builder = new StringBuilder(text);
        while (builder.length() < size)
            builder.insert(0, '0');
        return builder.toString();
    }
    
    /**
     * Parses the string as a long.
     * @param number String containing numerical data
     * @return Long value parsed from string
     */
    public static final long getLong(String number) {
        if (number == null) return -1;
        long integer;
        try {
            number = number.replaceAll("\\s", "");
            if (number.toLowerCase().startsWith("0x"))
                integer = Long.parseLong(number.substring(2), 16);
            else if (number.startsWith("g")) {
                number = number.substring(1);
                if (number.toLowerCase().startsWith("0x"))
                    integer = Long.parseLong(number.substring(2), 16);
                else
                    integer = Long.parseLong(number);
            }
            else
                integer = Long.parseLong(number);
            return integer;
        } catch (NumberFormatException e) { return -1; }
    }
    
    /**
     * Gets a SHA1 from a string
     * @param hash SHA1 string
     * @return SHA1 hash from string.
     */
    public static final SHA1 getSHA1(String hash) {
        if (hash == null || hash.isEmpty()) return null;
        hash = hash.replaceAll("\\s", "");
        if (hash.startsWith("h"))
            hash = hash.substring(1);
        if (!Strings.isSHA1(hash)) return null;
        return new SHA1(hash);
    }

    /**
     * Gets a GUID from a string
     * @param number String containing GUID
     * @return GUID from string.
     */
    public static final GUID getGUID(String number) {
        long value = Strings.getLong(number);
        if (value <= 0) return null;
        return new GUID(value);
    }
    
    /**
     * Tests if a given string is a SHA1 hash.
     * @param hash String to test
     * @return Whether or not the string is a SHA1.
     */
    public static final boolean isSHA1(String hash) {
        if (hash == null || hash.isEmpty()) return false;
        return SHA1_REGEX.matcher(hash).matches();
    }
    
    /**
     * Tests if a given string is a valid GUID.
     * @param guid String to test
     * @return Whether or not the string is a valid GUID.
     */
    public static final boolean isGUID(String guid) {
        if (guid == null || guid.isEmpty()) return false;
        return GUID_REGEX.matcher(guid).matches() || HEX_GUID_REGEX.matcher(guid).matches();
    }

    /**
     * Cleans up a filepath string for consistency.
     * @param path Path string
     * @return Sanitized path
     */
    public static final String cleanupPath(String path) {
        if (path == null) return null;
        path = path.trim();
        path = path.replaceAll("\\\\", "/");
        path = path.replaceAll("[//]+", "/");
        if (path.startsWith("/"))
            path = path.substring(1);
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return path;
    }
    
    public static final String setExtension(String path, String extension) {
        if (extension.startsWith("."))
            extension = extension.substring(1);
        int index = path.lastIndexOf(".");
        if (index != -1)
            path = path.substring(0, index);
        return path + "." + extension;
    }
}