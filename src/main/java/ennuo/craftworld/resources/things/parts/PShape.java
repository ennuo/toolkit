package ennuo.craftworld.resources.things.parts;

import ennuo.craftworld.resources.structs.ContactCache;
import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.structs.Polygon;
import ennuo.craftworld.serializer.Serializer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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
    
    public Matrix4f COM;
    
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
        thickness = serializer.input.f32();
       massDepth = serializer.input.f32();
       
       if (serializer.partsRevision >= 0x5e)
           editorColour = serializer.input.u32();
       else editorColourLegacy = serializer.input.v3();
       
       editorColourBrightness = serializer.input.f32();
       
       bevelSize = serializer.input.f32();
       
       COM = serializer.input.matrix();
       
       if (serializer.partsRevision >= 0x5e) {
           behaviour = serializer.input.i32();
           editorColourOff = serializer.input.u32();
           editorColourOffBrightness = serializer.input.f32();
           lethalType = serializer.input.i16();
       } else {
           interactPlayMode = serializer.input.i8();
           interactEditMode = serializer.input.i8();
           lethalType = serializer.input.i32();
           collidableGame = serializer.input.bool();
           collidablePoppet = serializer.input.bool();
           if (serializer.partsRevision >= 0x4e)
               collidableWithParent = serializer.input.bool();
       }
       
       soundEnumOverride = serializer.input.i32();
       
       if (serializer.partsRevision < 0x5e) return;
       
       playerNumberColour = serializer.input.i8();
       
       flags = serializer.input.i16();
       
       contactCache = new ContactCache(serializer);
       
       stickiness = serializer.input.i32();
       
       if (serializer.partsRevision < 0x76) return;
       
       grabbability = serializer.input.i32();
       grabFilter = serializer.input.i32();
       
       editorColourOpacity = serializer.input.i32();
       editorColourOffOpacity = serializer.input.i32();
       
       if (serializer.partsRevision >= 0x7a)
           editorColourShiny = serializer.input.i32();
       
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
       zBias = serializer.input.i32();
       
       fireDensity = serializer.input.i8();
       fireLifetime = serializer.input.i8();
    }
    
}
