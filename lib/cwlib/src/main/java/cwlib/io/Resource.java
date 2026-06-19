package cwlib.io;

import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

/**
 * Interface used to specify that an object
 * can be compressed to a resource.
 */
public interface Resource extends Serializable
{
    /**
     * Builds this resource using specified revision and compression flags
     * and returns the data necessary to properly compress it.
     *
     * @param revision         Revision of the resource
     * @param compressionFlags Compression flags used during resource serialization
     * @return Serialization data
     */
    SerializationData build(Revision revision, byte compressionFlags);

    /**
     * Serializes custom data for use in Alear patched versions of LittleBigPlanet.
     * @param serializer Serializer instance
     */
    default void serializeExtraData(Serializer serializer)
    {
        // I know it's bad practice, but I'm definitely not adding this to everything.
    }

    default int getExtraDataAllocatedSize()
    {
        return 0;
    }

    /**
     * Performs necessary fixes to a resource after serializing is finished.
     *
     * @param revision The revision of the loaded resource
     */
    default void onLoadFinished(Revision revision)
    {
        // Using a default implementation since I'm not adding this to every resource right now.
    }

    /**
     * Performs necessary fixes to a resource for serialization for a specified revision.
     *
     * @param revision The target revision
     */
    default void onStartSave(Revision revision)
    {
        // Using a default implementation since I'm not adding this to every resource right now.
    }

}
