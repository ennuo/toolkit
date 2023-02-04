package editor.gl;

import cwlib.resources.RLevel;
import cwlib.resources.custom.RSceneGraph;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.FileIO;
import editor.gl.objects.Mesh;
import editor.gl.objects.Shader;
import editor.gl.objects.Texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

public class RenderSystem {
    public static class DrawCall implements Comparable<DrawCall> {
        private int VAO;
        private Texture instance;
        private Matrix4f[] model;
        private Vector4f color;
        private MeshPrimitive primitive;

        public DrawCall(int VAO, MeshPrimitive primitive, Texture instance, Matrix4f[] model, Vector4f color) {
            this.VAO = VAO;
            this.primitive = primitive;
            this.instance = instance;
            this.model = model;
            this.color = color;
        }

        public void draw() {
            glBindVertexArray(this.VAO);

            if (primitive.getPrimitiveType() == GL_TRIANGLE_STRIP) glPrimitiveRestartIndex(0xFFFF);
            else glPrimitiveRestartIndex(0xFFFFFFFF);

            if (RenderSystem.getRenderPath() == RenderMode.SHADOW) {
                RenderSystem.getShadowMapShader().bind(null, model, color);
                glDrawElements(primitive.getPrimitiveType(), primitive.getNumIndices(), GL_UNSIGNED_INT, primitive.getFirstIndex() * 4);
            }
            else
                primitive.draw(instance, model, color);
            
            glBindVertexArray(0);
            glUseProgram(0);
        }

        @Override public int compareTo(DrawCall call) {
            return this.primitive.getAlphaLayer() - call.primitive.getAlphaLayer();
        }
    }

    public static enum RenderMode {
        RENDER,
        SHADOW
    };

    private static RSceneGraph sceneGraph;
    private static RenderMode renderPath = RenderMode.RENDER;
    private static Camera camera;
    private static ArrayList<DrawCall> DRAW_CALLS = new ArrayList<>(8196);

    private static float deltaTime;
    private static long lastTime;

    private static int vertexShader;
    private static Shader shadow;
    private static Shader fallback;
    private static int composition;

    public static boolean RENDER_TO_FRAMEBUFFER = true;

    private static int SCREEN_VAO, SCREEN_VBO;

    private static int PRT_C_BUFFER, PRT_SHADOW_MAP;
    private static int C_BUF_TEX, SHADOW_MAP_TEX;
    private static int C_BUF_RBO;

    private static final int SHADOWMAP_RESOLUTION = 1024;

    private static final int COLOR_RES_X = 1280;
    private static final int COLOR_RES_Y = 720;

    private static boolean SHADOWS_ENABLED = false;

    private static boolean waitForGarbageCollect = false;
    private static boolean initialized = false;

    public static Shader OVERRIDE_SHADER = null;

    public static void queue(DrawCall call) { DRAW_CALLS.add(call); }

    public static void initialize() {
        if (initialized) return;

        glEnable(GL_PRIMITIVE_RESTART);
        glEnable(GL_STENCIL_TEST);

        vertexShader = Shader.compileSource(
            FileIO.getResourceFileAsString("/shaders/default.vs"),
            GL_VERTEX_SHADER
        );

        fallback = new Shader(vertexShader, FileIO.getResourceFileAsString("/shaders/fallback.fs"));
        shadow = new Shader(
            FileIO.getResourceFileAsString("/shaders/shadow.vs"),
            FileIO.getResourceFileAsString("/shaders/nop.fs")
        );

        lastTime = System.nanoTime();

        // Create framebuffer and texture
        PRT_SHADOW_MAP = glGenFramebuffers();
        SHADOW_MAP_TEX = glGenTextures();

        // Initialize depth buffer
        glBindTexture(GL_TEXTURE_2D, SHADOW_MAP_TEX);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, SHADOWMAP_RESOLUTION, SHADOWMAP_RESOLUTION, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Attach texture to depth buffer of framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, PRT_SHADOW_MAP);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, SHADOW_MAP_TEX, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        // Color buffer
        PRT_C_BUFFER = glGenFramebuffers();
        C_BUF_TEX = glGenTextures();

        glBindFramebuffer(GL_FRAMEBUFFER, PRT_C_BUFFER);

        glBindTexture(GL_TEXTURE_2D, C_BUF_TEX);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, COLOR_RES_X, COLOR_RES_Y, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, C_BUF_TEX, 0);
        
        C_BUF_RBO = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, C_BUF_RBO);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, COLOR_RES_X, COLOR_RES_Y);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, C_BUF_RBO);

        // Rebind default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Create full screen quad

        FloatBuffer quadBuffer = BufferUtils.createFloatBuffer(20);
        quadBuffer.put(new float[] {
            -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f
        });
        quadBuffer.flip();

        SCREEN_VAO = glGenVertexArrays();
        SCREEN_VBO = glGenBuffers();
        glBindVertexArray(SCREEN_VAO);
        glBindBuffer(GL_ARRAY_BUFFER, SCREEN_VBO);
        glBufferData(GL_ARRAY_BUFFER, quadBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 0x3, GL_FLOAT, false, 5 * 4, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 0x2, GL_FLOAT, false, 5 * 4, 3 * 4);
        glBindVertexArray(0);

        // Compile final composition shader

        composition = Shader.compileProgram(
            Shader.compileSource(FileIO.getResourceFileAsString("/shaders/quad.vs"), GL_VERTEX_SHADER),
            Shader.compileSource(FileIO.getResourceFileAsString("/shaders/quad.fs"), GL_FRAGMENT_SHADER)
        );
        
        initialized = true;
    }

    public static void render(int w, int h) {
        long now = System.nanoTime();
        deltaTime = (now - lastTime) / 1_000_000_000f;
        lastTime = now; 

        if (waitForGarbageCollect) return;
        
        if (sceneGraph != null) {
            Vector4f fogColor = sceneGraph.getLighting().fogColor;
            glClearColor(fogColor.x, fogColor.y, fogColor.z, 1.0f);
        }

        glEnable(GL_DEPTH_TEST);

        RenderSystem.DRAW_CALLS.clear();
        RenderSystem.getSceneGraph().update();
        Collections.sort(RenderSystem.DRAW_CALLS);

        if (SHADOWS_ENABLED) {
            renderPath = RenderMode.SHADOW;

            glBindFramebuffer(GL_FRAMEBUFFER, PRT_SHADOW_MAP);
            glViewport(0, 0, SHADOWMAP_RESOLUTION, SHADOWMAP_RESOLUTION);
            glClear(GL_DEPTH_BUFFER_BIT);

            for (DrawCall call : RenderSystem.DRAW_CALLS)
                call.draw();
        }

        renderPath = RenderMode.RENDER;

        glBindFramebuffer(GL_FRAMEBUFFER, PRT_C_BUFFER);
        glViewport(0, 0, COLOR_RES_X, COLOR_RES_Y);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        for (DrawCall call : RenderSystem.DRAW_CALLS)
            call.draw();

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, w, h);
        glClear(GL_COLOR_BUFFER_BIT);

        if (RENDER_TO_FRAMEBUFFER) {
            glUseProgram(composition);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, C_BUF_TEX);
            glBindVertexArray(SCREEN_VAO);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        }

        glBindVertexArray(0);
        glUseProgram(0);
    }

    public static void garbageCollect() {
        if (sceneGraph == null) return;
        waitForGarbageCollect = true;

        HashSet<ResourceDescriptor> descriptors = sceneGraph.getResourcesInUse();

        int shaders = 0, textures = 0, meshes = 0, animations = 0;
        int procs = Mesh.PROCEDURAL_MESHES.size();

        for (Shader shader : Shader.PROGRAMS.values().toArray(Shader[]::new)) {
            if (descriptors.contains(shader.descriptor)) continue;
            if (shader == RenderSystem.fallback) continue;
            shader.delete();
            shaders++;
        }

        for (Texture texture : Texture.TEXTURES.values().toArray(Texture[]::new)) {
            if (descriptors.contains(texture.descriptor)) continue;
            texture.delete();
            textures++;
        }

        for (Mesh mesh : Mesh.MESHES.values().toArray(Mesh[]::new)) {
            if (descriptors.contains(mesh.getDescriptor())) continue;
            mesh.delete();
            meshes++;
        }

        for (ResourceDescriptor descriptor : PRenderMesh.ANIMATIONS.keySet().toArray(ResourceDescriptor[]::new)) {
            if (descriptors.contains(descriptor)) continue;
            PRenderMesh.ANIMATIONS.remove(descriptor);
            animations++;
        }

        for (Mesh mesh : Mesh.PROCEDURAL_MESHES) mesh.delete();
        Mesh.PROCEDURAL_MESHES.clear();

        System.out.println(String.format("[Garbage Collect] %d shaders freed", shaders));
        System.out.println(String.format("[Garbage Collect] %d textures freed", textures));
        System.out.println(String.format("[Garbage Collect] %d meshes freed", meshes));
        System.out.println(String.format("[Garbage Collect] %d animations freed", animations));
        System.out.println(String.format("[Garbage Collect] %d procs freed", procs));

        System.gc();

        waitForGarbageCollect = false;
    }

    public static PLevelSettings getLevelSettings() { return sceneGraph.getLighting(); }
    public static RSceneGraph getSceneGraph() { return sceneGraph; }
    public static RenderMode getRenderPath() { return renderPath; }
    public static Camera getMainCamera() { return camera; }
    public static float getDeltaTime() { return deltaTime; }
    public static int getVertexShader() { return vertexShader; }
    public static Shader getShadowMapShader() { return shadow; }
    public static Shader getFallbackShader() { return fallback; }

    public static int getColorBufferTexture() { return C_BUF_TEX; }
    public static int getShadowMapTexture() { return SHADOW_MAP_TEX; }

    public static void setRenderPath(RenderMode mode) { renderPath = mode; }
    public static void setSceneGraph(RSceneGraph graph) {
        waitForGarbageCollect = true;
        sceneGraph = graph;
        camera = graph.getCamera();
        garbageCollect();
    }

    public static void setLevel(RLevel level) {
        setSceneGraph(new RSceneGraph(level));
    }
}
