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

public class PCostume implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public ResourceDescriptor mesh;
    public ResourceDescriptor material;

    @GsonRevision(min=0x19a)
    public ResourceDescriptor materialPlan;

    public int[] meshPartsHidden;
    public Primitive[] primitives;

    @GsonRevision(lbp3=true, min=0xdb)
    public byte creatureFilter;

    public CostumePiece[] costumePieces;

    @GsonRevision(branch=0x4c44, min=Revisions.LD_TEMP_COSTUME)
    @GsonRevision(min=0x2c5)
    public CostumePiece[] temporaryCostumePiece;

    public PCostume() {
        this.costumePieces = new CostumePiece[14];
        for (int i = 0; i < this.costumePieces.length; ++i)
            this.costumePieces[i] = new CostumePiece();
        this.costumePieces[CostumePieceCategory.HEAD.getIndex()].mesh = 
            new ResourceDescriptor(9876, ResourceType.MESH);
        this.costumePieces[CostumePieceCategory.TORSO.getIndex()].mesh = 
            new ResourceDescriptor(9877, ResourceType.MESH);
        this.temporaryCostumePiece = new CostumePiece[] { new CostumePiece() };
    }

    @SuppressWarnings("unchecked")
    @Override public PCostume serialize(Serializer serializer, Serializable structure) {
        PCostume costume = (structure == null) ? new PCostume() : (PCostume) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        costume.mesh = serializer.resource(costume.mesh, ResourceType.MESH);
        costume.material = serializer.resource(costume.material, ResourceType.GFX_MATERIAL);

        if (version >= 0x19a)
            costume.materialPlan = serializer.resource(costume.materialPlan, ResourceType.PLAN, true);
    
        costume.meshPartsHidden = serializer.intvector(costume.meshPartsHidden);
        costume.primitives = serializer.array(costume.primitives, Primitive.class);
        
        if (subVersion >= 0xdb)
            costume.creatureFilter = serializer.i8(costume.creatureFilter);

        costume.costumePieces = serializer.array(costume.costumePieces, CostumePiece.class);

        if (version >= 0x2c5 || revision.has(Branch.LEERDAMMER, Revisions.LD_TEMP_COSTUME))
            costume.temporaryCostumePiece = serializer.array(costume.temporaryCostumePiece, CostumePiece.class);

        return costume;
    }

    @Override public int getAllocatedSize() { 
        int size = PCostume.BASE_ALLOCATION_SIZE;
        if (this.costumePieces != null)
            for (CostumePiece piece : this.costumePieces)
                size += piece.getAllocatedSize();
        if (this.primitives != null) size += (this.primitives.length * Primitive.BASE_ALLOCATION_SIZE);
        if (this.meshPartsHidden != null) size += (this.meshPartsHidden.length * 4);
        return size;
    }
}
