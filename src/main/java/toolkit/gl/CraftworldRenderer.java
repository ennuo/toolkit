package toolkit.gl;

import cwlib.resources.RLevel;
import cwlib.resources.custom.RSceneGraph;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.util.FileIO;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL40.*;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

public class CraftworldRenderer extends AWTGLCanvas {
    public static CraftworldRenderer INSTANCE;
    public static RSceneGraph SCENE_GRAPH;
    public static Camera MAIN_CAMERA;


    public static class ViewportListener implements KeyListener {
        @Override public void keyTyped(KeyEvent e) { return; }
        @Override public void keyReleased(KeyEvent e) { return; }
        @Override public void keyPressed(KeyEvent e) {
            Vector3f translation = MAIN_CAMERA.getTranslation();
            int code = e.getKeyCode();

            float displacement = 3.0f * 50.0f;
            if (code == KeyEvent.VK_D) translation.x += displacement;
            else if (code == KeyEvent.VK_A) translation.x -= displacement;
            else if (code == KeyEvent.VK_W) translation.y += displacement;
            else if (code == KeyEvent.VK_S) translation.y -= displacement;

            MAIN_CAMERA.setTranslation(translation);
        }
    }

    private static final long serialVersionUID = 1L;
    private static final GLData GL_DATA = new GLData();
    static {
        GL_DATA.samples = 4;
        GL_DATA.swapInterval = 0;
        GL_DATA.majorVersion = 4;
        GL_DATA.minorVersion = 0;
    }

    private float deltaTime;
    private long lastTime;

    private int vertexShader;
    private Shader fallbackShader;

    private int PRT_SHADOWMAP, PRT_SHADOWMAP_ZBUF;

    private RSceneGraph sceneGraph;

    public CraftworldRenderer() {
        super(GL_DATA);

        INSTANCE = this;

        ResourceSystem.DISABLE_LOGS = true;
        byte[] data = FileIO.getResourceFile("/binary/blank.bin");
        this.sceneGraph = new RSceneGraph(new Resource(data).loadResource(RLevel.class));
        ResourceSystem.DISABLE_LOGS = false;

        SCENE_GRAPH = this.sceneGraph;
        MAIN_CAMERA = this.sceneGraph.getCamera();

        this.setupAWT();
    }

    private void setupAWT() {
        this.addMouseWheelListener(event -> {
            MAIN_CAMERA.setPosZ(MAIN_CAMERA.getTranslation().z + (float) (event.getUnitsToScroll() * 25.0));
        });
        this.addKeyListener(new ViewportListener());
    }

    @Override public void initGL() {
        System.out.println("OpenGL version: " + effective.majorVersion + "." + effective.minorVersion + " (Profile: " + effective.profile + ")");
        createCapabilities();

        Vector4f ambcol = sceneGraph.getLighting().fogColor;
        glClearColor(ambcol.x, ambcol.y, ambcol.z, 1.0f);

        glEnable(GL_PRIMITIVE_RESTART);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);

        this.vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(this.vertexShader, FileIO.getResourceFileAsString("/shaders/default.vs"));
        glCompileShader(this.vertexShader);
        if (glGetShaderi(this.vertexShader, GL_COMPILE_STATUS) == 0)
            throw new AssertionError("Could not compile TGV SHADER! " + glGetShaderInfoLog(this.vertexShader));

        int fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, FileIO.getResourceFileAsString("/shaders/fallback.fs"));
        glCompileShader(fragment);
        if (glGetShaderi(fragment, GL_COMPILE_STATUS) == 0)
            throw new AssertionError("Could not compile FALLBACK SHADER! " + glGetShaderInfoLog(fragment));

        int fallback = glCreateProgram();
        glAttachShader(fallback, CraftworldRenderer.this.vertexShader);
        glAttachShader(fallback, fragment);
        glLinkProgram(fallback);

        if (glGetProgrami(fallback, GL_LINK_STATUS) == 0)
            throw new AssertionError("Could not link fallback program!");

        glDeleteShader(fragment);

        this.lastTime = System.nanoTime();

        this.fallbackShader = new Shader(fallback);

        // Create framebuffer and texture
        PRT_SHADOWMAP = glGenFramebuffers();
        PRT_SHADOWMAP_ZBUF = glGenTextures();

        // Initialize depth buffer
        glBindTexture(GL_TEXTURE_2D, PRT_SHADOWMAP_ZBUF);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 1024, 1024, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // Attach texture to depth buffer of framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, PRT_SHADOWMAP);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, PRT_SHADOWMAP_ZBUF, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        // Rebind default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void update() {
        long now = System.nanoTime();

        this.deltaTime = (now - this.lastTime) / 1_000_000_000f;
        this.lastTime = now;

        this.render();
    }

    @Override public void paintGL() {
        int w = (int) (this.getWidth() * this.getGraphicsConfiguration().getDefaultTransform().getScaleX());
        int h = (int) (this.getHeight() * this.getGraphicsConfiguration().getDefaultTransform().getScaleY());
        MAIN_CAMERA.setAspectRatio((float) (((double)w) / ((double)h)));

        MAIN_CAMERA.recomputeProjectionMatrix();
        MAIN_CAMERA.recomputeViewMatrix();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glViewport(0, 0, w, h);

        this.sceneGraph.update();

        swapBuffers();
        glfwPollEvents();
    }

    public float getDeltaTime() { return this.deltaTime; }
    public int getVertexShader() { return this.vertexShader; }
    public Shader getFallbackShader() { return this.fallbackShader; }
    public RSceneGraph getSceneGraph() { return this.sceneGraph; }

    public void setSceneGraph(RSceneGraph graph) { 
        this.sceneGraph = graph; 
        SCENE_GRAPH = graph;
        MAIN_CAMERA = this.sceneGraph.getCamera();
    }

    public void setLevel(RLevel level) { 
        this.setSceneGraph(new RSceneGraph(level));
    }
}
