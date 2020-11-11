package ennuo.craftworld.things.parts;

import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.memory.Vector3f;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.structs.Polygon;
import ennuo.craftworld.things.Part;
import ennuo.craftworld.things.Serializer;

public class PShape implements Part {
    public Polygon polygon;
    public ResourcePtr material = new ResourcePtr(null, RType.MATERIAL);
    public ResourcePtr oldMaterial = new ResourcePtr(null, RType.MATERIAL);
    public float thickness = 90;
    public float massDepth = 1;
    
    public Vector3f editorColourLegacy = new Vector3f(0.1f, 0.1f, 0.1f);
    public long editorColour = 0xFFFFFFFF;
    
    public float editorColourBrightness = 1;
    
    public float bevelSize = 10;
    
    public float[] COM;
    
    public int behaviour = 0;
    
    public long editorColourOff = 0xFFFFFFFF;
    public float editorColourOffBrightness = 0;
    
    public byte interactPlayMode = 0;
    public byte interactEditMode = 2;
    
    public int lethalType = 0;
    
    public boolean collidableGame = true;
    public boolean collidablePoppet = true;
    public boolean collidableWithParent = true;
    
    public int soundEnumOverride = 0;
    public int playerNumberColour;
    
    public short flags = 7;
    public ContactCache contactCache;
    
    public int stickiness = 0;
    public int grabbability = 0;
    public int grabFilter = 0;
    
    public int editorColourOpacity = 100;
    public int editorColourOffOpacity = 100;
    public int editorColourShiny = 0;
    
    public boolean canCollect = false;
    public boolean ghosty = true;
    
    public boolean climbable = false;
    public boolean currentlyClimbable = false;
    
    public boolean headDucking = true;
    public boolean isLBP2Shape = false;
    public boolean isStatic = true;
    
    public boolean collidableSackboy = true;
    
    public boolean partOfPowerUp = false;
    
    public boolean cameraExcluderIsSticky = false;
    public boolean ethereal = false;
    
    public int zBias = 0;
    
    public int fireDensity = 128;
    public int fireLifetime = 128;
    
    
    
    
    @Override
    public void Serialize(Serializer serializer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Deserialize(Serializer serializer) {
       polygon = new Polygon(serializer.input, serializer.partsRevision);
       
       material = serializer.input.resource(RType.MATERIAL);
       oldMaterial = serializer.input.resource(RType.MATERIAL);
       
       if (serializer.partsRevision >= 0x4e)
        thickness = serializer.input.float32();
       massDepth = serializer.input.float32();
       
       if (serializer.partsRevision >= 0x5e)
           editorColour = serializer.input.uint32();
       else editorColourLegacy = serializer.input.v3();
       
       editorColourBrightness = serializer.input.float32();
       
       bevelSize = serializer.input.float32();
       
       COM = serializer.input.matrix();
       
       if (serializer.partsRevision >= 0x5e) {
           behaviour = serializer.input.int32();
           editorColourOff = serializer.input.uint32();
           editorColourOffBrightness = serializer.input.float32();
           lethalType = serializer.input.int16();
       } else {
           interactPlayMode = serializer.input.int8();
           interactEditMode = serializer.input.int8();
           lethalType = serializer.input.int32();
           collidableGame = serializer.input.bool();
           collidablePoppet = serializer.input.bool();
           if (serializer.partsRevision >= 0x4e)
               collidableWithParent = serializer.input.bool();
       }
       
       soundEnumOverride = serializer.input.int32();
       
       if (serializer.partsRevision < 0x5e) return;
       
       playerNumberColour = serializer.input.int8();
       
       flags = serializer.input.int16();
       
       contactCache = new ContactCache(serializer);
       
       stickiness = serializer.input.int32();
       
       if (serializer.partsRevision < 0x76) return;
       
       grabbability = serializer.input.int32();
       grabFilter = serializer.input.int32();
       
       editorColourOpacity = serializer.input.int32();
       editorColourOffOpacity = serializer.input.int32();
       
       if (serializer.partsRevision >= 0x7a)
           editorColourShiny = serializer.input.int32();
       
       canCollect = serializer.input.bool();
       ghosty = serializer.input.bool();
       
       climbable = serializer.input.bool();
       
       if (serializer.partsRevision >= 0x7e)
           currentlyClimbable = serializer.input.bool();
       
       headDucking = serializer.input.bool();
       
       if (serializer.partsRevision >= 0x7a) {
           isLBP2Shape = serializer.input.bool();
           isStatic = serializer.input.bool();
           if (serializer.gameRevision >= 0xCB03E8)
               collidableSackboy = serializer.input.bool();
       }
       
       if (serializer.partsRevision < 0x7e && serializer.gameRevision < 0x010503f0)
           return;
       
       partOfPowerUp = serializer.input.bool();
       cameraExcluderIsSticky = serializer.input.bool();
       
       ethereal = serializer.input.bool();
       zBias = serializer.input.int32();
       
       fireDensity = serializer.input.int8();
       fireLifetime = serializer.input.int8();
    }
    
}
