package cwlib.structs.things.parts;

import cwlib.enums.Branch;
import cwlib.enums.CostumePieceCategory;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.things.components.CostumePiece;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

import java.util.ArrayList;

public class PCostume implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public ResourceDescriptor mesh;
    public ResourceDescriptor material;

    @GsonRevision(min = 0x19a)
    public ResourceDescriptor materialPlan;

    public ArrayList<Integer> meshPartsHidden = new ArrayList<>();
    public Primitive[] primitives;

    @GsonRevision(lbp3 = true, min = 0xdb)
    public int creatureFilter; // why did i have this as a byte? double check later

    public CostumePiece[] costumePieces;

    @GsonRevision(branch = 0x4c44, min = Revisions.LD_TEMP_COSTUME)
    @GsonRevision(min = 0x2c5)
    public CostumePiece[] temporaryCostumePiece;

    public PCostume()
    {
        this.mesh = new ResourceDescriptor(1087, ResourceType.MESH);
        this.costumePieces = new CostumePiece[14];
        for (int i = 0; i < this.costumePieces.length; ++i)
            this.costumePieces[i] = new CostumePiece();
        this.costumePieces[CostumePieceCategory.HEAD.getIndex()].mesh =
            new ResourceDescriptor(9876, ResourceType.MESH);
        this.costumePieces[CostumePieceCategory.TORSO.getIndex()].mesh =
            new ResourceDescriptor(9877, ResourceType.MESH);
        this.temporaryCostumePiece = new CostumePiece[] { new CostumePiece() };
    }

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        mesh = serializer.resource(mesh, ResourceType.MESH);
        material = serializer.resource(material, ResourceType.GFX_MATERIAL);

        if (version >= 0x19a)
            materialPlan = serializer.resource(materialPlan, ResourceType.PLAN,
                true);

        if (serializer.isWriting())
        {
            int[] vec = meshPartsHidden.stream().mapToInt(Integer::valueOf).toArray();
            serializer.intvector(vec);
        }
        else
        {
            int[] vec = serializer.intvector(null);
            if (vec != null)
            {
                for (int v : vec)
                    meshPartsHidden.add(v);
            }
        }

        primitives = serializer.array(primitives, Primitive.class);

        if (subVersion >= 0xdb)
            creatureFilter = serializer.i32(creatureFilter);

        costumePieces = serializer.array(costumePieces, CostumePiece.class);

        if (version >= 0x2c5 || revision.has(Branch.LEERDAMMER, Revisions.LD_TEMP_COSTUME))
            temporaryCostumePiece = serializer.array(temporaryCostumePiece,
                CostumePiece.class);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PCostume.BASE_ALLOCATION_SIZE;
        if (this.costumePieces != null)
            for (CostumePiece piece : this.costumePieces)
                size += piece.getAllocatedSize();
        if (this.primitives != null)
            size += (this.primitives.length * Primitive.BASE_ALLOCATION_SIZE);
        if (this.meshPartsHidden != null) size += (this.meshPartsHidden.size() * 4);
        return size;
    }
}
