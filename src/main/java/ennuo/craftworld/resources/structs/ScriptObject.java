package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.enums.MachineType;
import ennuo.craftworld.resources.things.parts.Part;
import ennuo.craftworld.resources.things.ThingPtr;
import org.joml.Matrix4f;

public class ScriptObject {
    public MachineType type;
    public Matrix4f matrix;
    public boolean bool;
    public float f;
    public int integer;
    public ThingPtr thing;
    public Part part;
}
