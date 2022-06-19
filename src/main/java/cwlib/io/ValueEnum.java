package cwlib.io;

/**
 * Used in conjunction with enums for easily (de)serializing values.
 */
public interface ValueEnum<E> {
    E getValue();
}