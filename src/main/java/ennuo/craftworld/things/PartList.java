package ennuo.craftworld.things;

import ennuo.craftworld.things.parts.PBody;
import ennuo.craftworld.things.parts.PGeneratedMesh;
import ennuo.craftworld.things.parts.PGroup;
import ennuo.craftworld.things.parts.PPos;
import ennuo.craftworld.things.parts.PRenderMesh;
import ennuo.craftworld.things.parts.PScript;
import ennuo.craftworld.things.parts.PScriptName;
import ennuo.craftworld.things.parts.PShape;
import ennuo.craftworld.things.parts.PStickers;
import ennuo.craftworld.things.parts.PTrigger;

public class PartList {
    public PBody body;
    public PPos pos;
    public PRenderMesh renderMesh;
    public PTrigger trigger;
    public PStickers stickers;
    public PScript script;
    public PScriptName scriptName;
    public PGeneratedMesh generatedMesh;
    public PShape shape;
    public PGroup group;
    
    public static Part getPart(String name) {
        switch (name) {
            case "Body": return new PBody();
            case "Pos": return new PPos();
            case "RenderMesh": return new PRenderMesh();
            case "Trigger": return new PTrigger();
            case "Stickers": return new PStickers();  
            case "Script": return new PScript();
            case "ScriptName": return new PScriptName();
            case "GeneratedMesh": return new PGeneratedMesh();
            case "Shape": return new PShape();
            case "Group": return new PGroup();
        }
        return null;
    }
    
    
    public Part add(String name) throws Exception {
        switch (name) {
            case "Body": {
                body = new PBody();
                return body;
            }
            case "Pos": {
                pos = new PPos();
                return pos;
            }
            case "RenderMesh": {
                renderMesh = new PRenderMesh();
                return renderMesh;
            }
            case "Trigger": {
                trigger = new PTrigger();
                return trigger;
            }
            case "Stickers": {
                stickers = new PStickers();
                return stickers;
            }
            case "Script": {
                script = new PScript();
                return script;
            }
            case "ScriptName": {
                scriptName = new PScriptName();
                return scriptName;
            }
            case "GeneratedMesh": {
                generatedMesh = new PGeneratedMesh();
                return generatedMesh;
            }
            case "Shape": {
                shape = new PShape();
                return shape;
            }
            case "Group": {
                group = new PGroup();
                return group;
            }
        }
        throw new Exception("P" + name + " is not implemented! Stopping parser!");
    }
}
