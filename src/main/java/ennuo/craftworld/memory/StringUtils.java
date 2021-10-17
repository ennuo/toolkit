package ennuo.craftworld.memory;

public final class StringUtils {
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
            else if (number.startsWith("g"))
                integer = Long.parseLong(number.substring(1));
            else
                integer = Long.parseLong(number);
            return integer;
        } catch (NumberFormatException e) { return -1; }
    }
}