package cwlib.structs.things;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.parts.PRenderMesh;
import org.joml.Matrix4f;

public class Decoration implements Serializable {
    PRenderMesh renderMesh;
    Matrix4f offset = new Matrix4f().identity();
    int parentBone = -1;
    int parentTriVert = -1;
    public float baryU, baryV;
    public float decorationAngle;
    public int onCostumePiece = -1;
    public int earthDecoration = -1;
    public float decorationScale = 1.0f;
    public short placedBy = -1;
    public boolean reversed;
    public int playModeFrame;
    public int planGUID;
    
    public Decoration serialize(Serializer serializer, Serializable structure) {
        Decoration decoration = (structure == null) ? new Decoration() : (Decoration) structure;
        
        decoration.renderMesh = serializer.reference(decoration.renderMesh, PRenderMesh.class);
        
        decoration.offset = serializer.matrix(decoration.offset);
        decoration.parentBone = serializer.i32(decoration.parentBone);
        decoration.parentTriVert = serializer.i32(decoration.parentTriVert);
        
        decoration.baryU = serializer.f32(decoration.baryU);
        decoration.baryV = serializer.f32(decoration.baryV);
        
        decoration.decorationAngle = serializer.f32(decoration.decorationAngle);
        decoration.onCostumePiece = serializer.i32(decoration.onCostumePiece);
        decoration.earthDecoration = serializer.i32(decoration.earthDecoration);
        decoration.decorationScale = serializer.f32(decoration.decorationScale);
        
        decoration.placedBy = serializer.i16(decoration.placedBy);
        decoration.reversed = serializer.bool(decoration.reversed);
        
        decoration.playModeFrame = serializer.i32(decoration.playModeFrame);
        decoration.planGUID = serializer.i32(decoration.planGUID);
        
        return decoration;
    }
    
}
