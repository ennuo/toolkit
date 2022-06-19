package cwlib.io;

import cwlib.io.serializer.Serializer;

/**
 * Interface that specifies that an object
 * can be serialized as a pure data binary
 * structure.
 */
public interface Serializable {
    /**
     * (De)serializes a structure that implements Serializable.
     * @param <T> Type of structure to (de)serialize
     * @param serializer Serializer instance
     * @param structure Instance of structure to (de)serialize
     * @param clazz Class of type of structure to (de)serialize
     * @return Instance of structure that was (de)serialized
     */
    public static <T extends Serializable> T serialize(Serializer serializer, T structure, Class<T> clazz) {
        try {
            if (structure == null) structure = clazz.getDeclaredConstructor().newInstance();
            return structure.serialize(serializer, structure);
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }
    
    /**
     * (De)serializes a structure that implements Serializable.
     * @param <T> Type of structure to (de)serialize
     * @param serializer Serializer instance
     * @param structure Instance of structure to (de)serialize
     * @return Instance of structure that was (de)serialized.
     */
    <T extends Serializable> T serialize(Serializer serializer, T structure);

    /**
     * Calculates the size necessary to store this structure
     * @return Size of this structure
     */
    int getAllocatedSize();
}
