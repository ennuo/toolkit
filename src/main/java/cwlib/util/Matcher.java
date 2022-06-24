package cwlib.util;

import java.util.ArrayList;

/**
 * Knuth-Morris-Pratt Algorithm for Pattern Matching
 * Sourced: http://stackoverflow.com/questions/1507780/searching-for-a-sequence-of-bytes-in-a-binary-file-with-java
 */
public final class Matcher {
    /**
     * Finds the first occurence of a pattern in a byte array starting from the origin.
     * @param data Data to search
     * @param pattern Pattern to search for
     * @return Index in byte array where pattern occurs, -1 if not found
     */
    public static int indexOf(byte[] data, byte[] pattern) {
        return Matcher.indexOf(data, pattern, 0);
    }

    /**
     * Finds the first occurence of a pattern in a byte array starting from an offset.
     * @param data Data to search
     * @param pattern Pattern to search for
     * @param offset Offset to start from
     * @return Index in byte array where pattern occurs, -1 if not found
     */
    public static int indexOf(byte[] data, byte[] pattern, int offset) {
        int[] failure = computeFailure(pattern);
        int j = 0;
        if (data.length == 0)
            return -1;
        for (int i = offset; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i])
                j = failure[j - 1];
            if (pattern[j] == data[i])
                j++;
            if (j == pattern.length)
                return i - pattern.length + 1;
        }
        return -1;
    }

    /**
     * Finds tall occurences of a pattern in a byte array starting from the origin.
     * @param data Data to search
     * @param pattern Pattern to search for
     * @return Indices in byte array where pattern occurs
     */
    public static int[] indicesOf(byte[] data, byte[] pattern) {
        ArrayList<Integer> indices = new ArrayList<>();
        int offset = Matcher.indexOf(data, pattern, 0);
        while (offset != -1) {
            indices.add(offset);
            offset = Matcher.indexOf(data, pattern, offset + pattern.length);
        }
        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     * @param pattern Pattern to search for
     * @return Partial match table
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];
        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i])
                j = failure[j - 1];
            if (pattern[j] == pattern[i])
                j++;
            failure[i] = j;
        }
        return failure;
    }
}