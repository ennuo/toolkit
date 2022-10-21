package editor.gl;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.structs.things.components.decals.Decal;
import cwlib.types.data.ResourceDescriptor;
import editor.gl.RenderSystem.DrawCall;
import editor.gl.objects.Mesh;
import editor.gl.objects.Texture;

public class MeshInstance {
    public Mesh mesh;
    public ResourceDescriptor texture;
    public MeshPrimitive[] primitives;
    public Decal[] decals;
    public boolean isStaticMesh;

    public MeshInstance(Mesh mesh) {
        this.mesh = mesh;
        this.texture = null;

        MeshPrimitive[] primitives = mesh.getPrimitives();
        this.primitives = new MeshPrimitive[primitives.length];
        for (int i = 0; i < primitives.length; ++i)
            this.primitives[i] = new MeshPrimitive(primitives[i]);
    }

    public void override(ResourceDescriptor base, ResourceDescriptor override) {
        for (MeshPrimitive primitive : this.primitives) {
            if (base.equals(primitive.getBaseMaterial()))
                primitive.override(override);
        }
    }

    public void draw(Matrix4f[] model, Vector4f color) { this.draw(model, color, null); }
    public void draw(Matrix4f[] model, Vector4f color, int[] regionIDsToHide) {
        Texture instance = Texture.get(this.texture);
        if (regionIDsToHide != null) {
            for (MeshPrimitive primitive : this.primitives) {
                boolean isHidden = false;
                for (int region : regionIDsToHide) {
                    if (primitive.getRegion() == region) {
                        isHidden = true;
                        break;
                    }
                }
                if (!isHidden)
                    RenderSystem.queue(new DrawCall(this.mesh.getVAO(), primitive, instance, model, color));
            }
        } else for (MeshPrimitive primitive : this.primitives) 
            RenderSystem.queue(new DrawCall(this.mesh.getVAO(), primitive, instance, model, color));
            

    }

    // SunCamera
    // OrthoLength = 0
    // FOV = 0
    // Aspect = 1.0
    // ZNear = 96051.5625
    // ZFar = 105312.703125

    // Bind(int shader, CP<RTexture> instance_texture, Vector3f instance_color, Vector4f param_4, int instance_texture_remap, Thing thing)
}
