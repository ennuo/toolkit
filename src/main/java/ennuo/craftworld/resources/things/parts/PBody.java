package ennuo.craftworld.resources.things.parts;

import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.resources.things.ThingPtr;
import org.joml.Vector3f;

public class PBody implements Part {
    public static long PART_FLAG = 1;
    
    public Vector3f posVel = new Vector3f(0, 0, 0);
    public float angVel = 0;
    public int frozen = 0;
    public ThingPtr editingPlayer;

    @Override
    public void Serialize(Serializer serializer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Deserialize(Serializer serializer) {
        posVel = serializer.input.v3();
        angVel = serializer.input.f32();
        frozen = serializer.input.i32();
        editingPlayer = serializer.deserializeThing();
    }
}