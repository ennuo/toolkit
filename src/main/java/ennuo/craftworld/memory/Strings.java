package ennuo.craftworld.memory;

public class Strings {
    public static String leftPad(String text, int size) {
        StringBuilder builder = new StringBuilder(text);
        for (; builder.length() < size; builder.insert(0, '0'));
        return builder.toString();
    }
}