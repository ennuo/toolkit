package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.joint.FCurve;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

/**
 * Resource that stores behaviour of joints,
 * like bolts and strings.
 */
public class RJoint implements Resource
{
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

    @Override
    public void serialize(Serializer serializer)
    {
        allowExpand = serializer.bool(allowExpand);
        allowContract = serializer.bool(allowContract);
        contractFreely = serializer.bool(contractFreely);

        lengthElasticity = serializer.f32(lengthElasticity);
        lengthPlasticity = serializer.f32(lengthPlasticity);

        lengthFunc = serializer.struct(lengthFunc, FCurve.class);

        angleDeviation = serializer.f32(angleDeviation);
        angleElasticity = serializer.f32(angleElasticity);
        anglePlasticity = serializer.f32(anglePlasticity);
        angleVelocity = serializer.f32(angleVelocity);

        angleFunc = serializer.struct(angleFunc, FCurve.class);

        normalizedForces = serializer.bool(normalizedForces);
        dontRotateA = serializer.bool(dontRotateA);
        dontRotateB = serializer.bool(dontRotateB);

        breakResistance = serializer.f32(breakResistance);

        gfxMaterial = serializer.resource(gfxMaterial, ResourceType.GFX_MATERIAL);

        gfxWidth = serializer.f32(gfxWidth);

        eventNameAngle = serializer.str(eventNameAngle);
        eventNameLength = serializer.str(eventNameLength);

        mesh = serializer.resource(mesh, ResourceType.MESH);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE + eventNameAngle.length() + eventNameLength.length();
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
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
