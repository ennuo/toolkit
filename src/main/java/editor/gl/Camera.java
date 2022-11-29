package editor.gl;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import cwlib.enums.Branch;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class Camera implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    private float fov = 1.0f;
    private float zNear = 2.0f;
    private float zFar = 150000.0f;

    @GsonRevision(branch=0x4d5a, max=0x6)
    private float aspectRatio = (float) (16.0 / 9.0);
    
    private Vector3f translation, rotation;

    private transient Matrix4f projectionMatrix, viewMatrix;

    public Camera() {
        this(-19518.318359375f, 1997.4072265625f, 1195.43371582031f);
    };

    public Camera(Vector3f translation, Vector3f euler) {
        this.translation = translation;
        this.rotation = euler;

        this.recomputeProjectionMatrix();
        this.recomputeViewMatrix();
    }

    public Camera(Vector3f translation) { this(translation, new Vector3f()); }
    public Camera(float x, float y, float z) { this(new Vector3f(x, y, z)); }

    public void setFov(float fov) {  this.fov = fov; this.recomputeProjectionMatrix(); }
    public void setZNear(float value) { this.zNear = value; this.recomputeProjectionMatrix(); }
    public void setZFar(float value) { this.zFar = value; this.recomputeProjectionMatrix(); }
    public void setAspectRatio(float value) { 
        if (value == this.aspectRatio) return;
        this.aspectRatio = value;
        this.recomputeProjectionMatrix();
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
        this.recomputeViewMatrix();
    }

    public void setPosX(float x) { 
        this.translation.x = x;
        this.recomputeViewMatrix();
    }

    public void setPosY(float y) { 
        this.translation.y = y;
        this.recomputeViewMatrix();
    }
    
    public void setPosZ(float z) { 
        this.translation.z = z;
        this.recomputeViewMatrix();
    }
    
    public void recomputeProjectionMatrix() {
        this.projectionMatrix = new Matrix4f().identity().setPerspective(this.fov, this.aspectRatio, this.zNear, this.zFar);
    }

    public void recomputeViewMatrix() {
        this.viewMatrix = new Matrix4f()
            .identity()
            .rotate((float) Math.toRadians(this.rotation.x), new Vector3f(1.0f, 0.0f, 0.0f))
            .rotate((float) Math.toRadians(this.rotation.y), new Vector3f(0.0f, 1.0f, 0.0f))
            .rotate((float) Math.toRadians(this.rotation.z), new Vector3f(0.0f, 0.0f, 1.0f))
            .translate(-this.translation.x, -this.translation.y, -this.translation.z);
    }

    public float getZNear() { return this.zNear; }
    public float getZFar() { return this.zFar; }
    public Matrix4f getViewMatrix() { return this.viewMatrix; }
    public Matrix4f getProjectionMatrix() { return this.projectionMatrix; }
    public Vector3f getTranslation() { return this.translation; }
    public Vector3f getEulerRotation() { return this.rotation; }

    public void setEulerRotation(Vector3f rot) { this.rotation = rot; }

    @SuppressWarnings("unchecked")
    @Override public Camera serialize(Serializer serializer, Serializable structure) {
        Camera camera = (structure == null) ? new Camera() : (Camera) structure;

        camera.fov = serializer.f32(camera.fov);
        camera.zNear = serializer.f32(camera.zNear);
        camera.zFar = serializer.f32(camera.zFar);
        if (serializer.getRevision().before(Branch.MIZUKI, Revisions.MZ_REMOVE_ASPECT))
            camera.aspectRatio = serializer.f32(camera.aspectRatio);
        camera.translation = serializer.v3(camera.translation);
        camera.rotation = serializer.v3(camera.rotation);

        if (!serializer.isWriting()) {
            camera.recomputeProjectionMatrix();
            camera.recomputeViewMatrix();
        }

        return camera;
    }

    @Override public int getAllocatedSize() { return Camera.BASE_ALLOCATION_SIZE; }
}
