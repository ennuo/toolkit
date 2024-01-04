package cwlib.structs.things.parts.depreciated;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class PParticleEmitter2 implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public static class ParticleLayer implements Serializable {
        public static final int BASE_ALLOCATION_SIZE = 0x30;

        public Matrix4f previousTransform, boneTransform;
        public float lastEmitTime, lastUpdateTime;
        public ResourceDescriptor settings0, settings1;
        public Vector4f volume;
        public int boneIndex;
        public float density;
        public int bucketConnectionIndex;
    
        @SuppressWarnings("unchecked")
        @Override public ParticleLayer serialize(Serializer serializer, Serializable structure) {
            ParticleLayer layer = (structure == null) ? new ParticleLayer() : (ParticleLayer) structure;
            
    
            
            return layer;
        }
    
        @Override public int getAllocatedSize() { 
            int size = ParticleLayer.BASE_ALLOCATION_SIZE;
    
            return size;
        }
    }




    @SuppressWarnings("unchecked")
    @Override public PParticleEmitter2 serialize(Serializer serializer, Serializable structure) {
        PParticleEmitter2 emitter = (structure == null) ? new PParticleEmitter2() : (PParticleEmitter2) structure;
        

        
        return emitter;
    }

    @Override public int getAllocatedSize() { 
        int size = PParticleEmitter2.BASE_ALLOCATION_SIZE;

        return size;
    }
}
