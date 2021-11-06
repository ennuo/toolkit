package ennuo.craftworld.types.savedata;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.structs.Slot;
import java.util.ArrayList;

public class BigProfile extends BaseProfile {
    public Slot[] slots = new Slot[82];
    public Resource planetDecorations;
    public ArrayList<Slot> downloads = new ArrayList<Slot>();
}
