package cwlib.io;

import cwlib.io.serializer.SerializationData;
import cwlib.types.data.Revision;

/**
 * Interface used to specify that an object
 * can be compressed to a resource.
 */
public interface Compressable {
    /**
     * Builds this resource using specified revision and compression flags
     * and returns the data necessary to properly compress it.
     * @param revision Revision of the resource
     * @param compressionFlags Compression flags used during resource serialization
     * @return Serialization data
     */
    SerializationData build(Revision revision, byte compressionFlags);
}
