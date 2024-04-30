package cwlib.structs.things.parts.depreciated;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class PParticleEmitter2 implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public static class ParticleLayer implements Serializable
    {
        public static final int BASE_ALLOCATION_SIZE = 0x30;

        public Matrix4f previousTransform, boneTransform;
        public float lastEmitTime, lastUpdateTime;
        public ResourceDescriptor settings0, settings1;
        public Vector4f volume;
        public int boneIndex;
        public float density;
        public int bucketConnectionIndex;

        @Override
        public void serialize(Serializer serializer)
        {

        }

        @Override
        public int getAllocatedSize()
        {
            int size = ParticleLayer.BASE_ALLOCATION_SIZE;

            return size;
        }
    }

    @Override
    public void serialize(Serializer serializer)
    {

    }

    @Override
    public int getAllocatedSize()
    {
        int size = PParticleEmitter2.BASE_ALLOCATION_SIZE;

        return size;
    }
}
