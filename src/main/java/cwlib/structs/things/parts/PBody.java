package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import org.joml.Vector3f;

public class PBody implements Serializable {
    public Vector3f posVel = new Vector3f(0, 0, 0);
    public float angVel;
    public int frozen;
    public Thing editingPlayer;
    
    
    public PBody serialize(Serializer serializer, Serializable structure) {
        PBody body = (structure == null) ? new PBody() : (PBody) structure;
        
        body.posVel = serializer.v3(body.posVel);
        body.angVel = serializer.f32(body.angVel);
        body.frozen = serializer.i32(body.frozen);
        
        if (serializer.revision.head > 0x22c)
            body.editingPlayer = serializer.reference(body.editingPlayer, Thing.class);
        
        return body;
    }
    
}
