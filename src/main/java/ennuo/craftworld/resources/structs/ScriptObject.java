package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.enums.MachineType;
import ennuo.craftworld.resources.things.parts.Part;
import ennuo.craftworld.resources.things.ThingPtr;

public class ScriptObject {
    public MachineType type;
    public float[] matrix;
    public boolean bool;
    public float f;
    public int integer;
    public ThingPtr thing;
    public Part part;
}
