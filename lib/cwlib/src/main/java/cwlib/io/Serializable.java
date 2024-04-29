package cwlib.io;

import cwlib.io.serializer.Serializer;

/**
 * Interface that specifies that an object
 * can be serialized as a pure data binary
 * structure.
 */
public interface Serializable
{
    /**
     * (De)serializes a structure that implements Serializable.
     *
     * @param serializer Serializer instance
     */
    void serialize(Serializer serializer);

    /**
     * Calculates the size necessary to store this structure
     *
     * @return Size of this structure
     */
    int getAllocatedSize();
}
