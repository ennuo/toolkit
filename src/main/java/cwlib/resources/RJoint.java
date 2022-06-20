package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.joint.FCurve;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

/**
 * Resource that stores behaviour of joints, 
 * like bolts and strings.
 */
public class RJoint implements Compressable, Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80 + (FCurve.BASE_ALLOCATION_SIZE * 0x2);

    public boolean allowExpand, allowContract, contractFreely;
    public float lengthElasticity, lengthPlasticity;
    public FCurve lengthFunc = new FCurve();
    public float angleDeviation, angleElasticity, anglePlasticity = 0.01f;
    public float angleVelocity = 0.0f;
    public FCurve angleFunc = new FCurve();
    public boolean normalizedForces = true, dontRotateA, dontRotateB;
    public float breakResistance;
    public ResourceDescriptor gfxMaterial;
    public float gfxWidth = 20.0f;
    public String eventNameAngle, eventNameLength;
    public ResourceDescriptor mesh;

    @SuppressWarnings("unchecked")
    @Override public RJoint serialize(Serializer serializer, Serializable structure) {
        RJoint joint = (structure == null) ? new RJoint() : (RJoint) structure;

        joint.allowExpand = serializer.bool(joint.allowExpand);
        joint.allowContract = serializer.bool(joint.allowContract);
        joint.contractFreely = serializer.bool(joint.contractFreely);

        joint.lengthElasticity = serializer.f32(joint.lengthElasticity);
        joint.lengthPlasticity = serializer.f32(joint.lengthPlasticity);

        joint.lengthFunc = serializer.struct(joint.lengthFunc, FCurve.class);

        joint.angleDeviation = serializer.f32(joint.angleDeviation);
        joint.angleElasticity = serializer.f32(joint.angleElasticity);
        joint.anglePlasticity = serializer.f32(joint.anglePlasticity);
        joint.angleVelocity = serializer.f32(joint.angleVelocity);

        joint.angleFunc = serializer.struct(joint.angleFunc, FCurve.class);

        joint.normalizedForces = serializer.bool(joint.normalizedForces);
        joint.dontRotateA = serializer.bool(joint.dontRotateA);
        joint.dontRotateB = serializer.bool(joint.dontRotateB);

        joint.breakResistance = serializer.f32(joint.breakResistance);

        joint.gfxMaterial = serializer.resource(joint.gfxMaterial, ResourceType.GFX_MATERIAL);

        joint.gfxWidth = serializer.f32(joint.gfxWidth);

        joint.eventNameAngle = serializer.str(joint.eventNameAngle);
        joint.eventNameLength = serializer.str(joint.eventNameLength);

        joint.mesh = serializer.resource(joint.mesh, ResourceType.MESH);
        
        return joint;
    }

    @Override public int getAllocatedSize() { 
        return BASE_ALLOCATION_SIZE + eventNameAngle.length() + eventNameLength.length();
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RJoint.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags, 
            ResourceType.JOINT,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }
}
