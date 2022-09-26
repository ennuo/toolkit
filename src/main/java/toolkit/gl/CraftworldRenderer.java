package toolkit.gl;

import cwlib.enums.Part;
import cwlib.resources.RLevel;
import cwlib.resources.RPlan;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PBody;
import cwlib.structs.things.parts.PGroup;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRef;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.structs.things.parts.PWorld;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.FileIO;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL40.*;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import org.joml.Matrix4f;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

public class CraftworldRenderer extends AWTGLCanvas {
    public static class ViewportListener implements KeyListener {
        @Override public void keyTyped(KeyEvent e) { return; }
        @Override public void keyReleased(KeyEvent e) { return; }
        @Override public void keyPressed(KeyEvent e) {
            Vector3f translation = Camera.MAIN.getTranslation();
            int code = e.getKeyCode();

            float displacement = 3.0f * 50.0f;
            if (code == KeyEvent.VK_D) translation.x += displacement;
            else if (code == KeyEvent.VK_A) translation.x -= displacement;
            else if (code == KeyEvent.VK_W) translation.y += displacement;
            else if (code == KeyEvent.VK_S) translation.y -= displacement;

            Camera.MAIN.setTranslation(translation);
        }
    }

    private static final long serialVersionUID = 1L;
    public static final GLData GL_DATA = new GLData();
    public static final PLevelSettings DEFAULT_LIGHTING = new PLevelSettings();
    static {
        GL_DATA.samples = 4;
        GL_DATA.swapInterval = 0;
        GL_DATA.majorVersion = 4;
        GL_DATA.minorVersion = 0;
    }

    private static float deltaTime;
    private static double lastTime;
    
    public static int TGV_SHADER;
    public static Shader FALLBACK_PROGRAM;

    private RLevel level;
    private PLevelSettings lighting;

    public CraftworldRenderer() {
        super(GL_DATA);

        ResourceSystem.DISABLE_LOGS = true;
        byte[] data = FileIO.getResourceFile("/binary/mybutton.bin");
        this.setLevel(new Resource(data).loadResource(RLevel.class));
        ResourceSystem.DISABLE_LOGS = false;
        
        this.setupAWT();
    }

    public CraftworldRenderer(ResourceDescriptor descriptor) {
        super(GL_DATA);
        byte[] data = ResourceSystem.extract(descriptor);
        this.setLevel( new Resource(data).loadResource(RLevel.class));
        this.setupAWT();
    }

    public CraftworldRenderer(RLevel level) {
        super(GL_DATA);
        this.setLevel(level);
        this.setupAWT();
    }
    
    private void findLevelSettings() {
        for (Thing thing : this.getThings()) {
            if (thing == null) continue;
            if (thing.hasPart(Part.LEVEL_SETTINGS)) {
                this.lighting = thing.getPart(Part.LEVEL_SETTINGS);
                return;
            }
        }
        this.lighting = DEFAULT_LIGHTING;
    }

    private void setupAWT() {
        this.addMouseWheelListener(event -> {
            Camera.MAIN.setPosZ(Camera.MAIN.getTranslation().z + (float) (event.getUnitsToScroll() * 25.0));
        });
        this.addKeyListener(new ViewportListener());
    }

    public void setLevel(RLevel level) { 
        this.level = level; 
        this.findLevelSettings();
        
        PWorld world = this.getWorld();


        ResourceDescriptor plan = world.backdropPlan;
        if (plan == null && world.backdrop != null && world.backdrop.hasPart(Part.REF))
            plan = ((PRef)world.backdrop.getPart(Part.REF)).plan;
        
        if (plan != null) {
            byte[] planData = ResourceSystem.extract(plan);
            if (planData == null) return;
            Thing[] things =  new Resource(planData).loadResource(RPlan.class).getThings();
            world.backdrop = things[0];
            ArrayList<Thing> worldThings = this.getThings();
            synchronized(worldThings) {
                for (Thing thing : things)
                    worldThings.add(thing);
            }
        }
        
        this.findLevelSettings();
    }
    
    public RLevel getLevel() { return this.level; }
    public PWorld getWorld() { return this.level.world.getPart(Part.WORLD); }
    public ArrayList<Thing> getThings() { return this.getWorld().things; }
    public PLevelSettings getLighting() { return this.lighting; }

    public byte[] getPlanData() { return this.level.toPlan(); }
    
    public void createMeshInstance(ResourceDescriptor descriptor) {
        Thing thing = new Thing(this.level.getNextUID());
        
        thing.setPart(Part.POS, new PPos(thing, 0, new Matrix4f().identity()));
        thing.setPart(Part.BODY, new PBody());
        thing.setPart(Part.GROUP, new PGroup());
        thing.setPart(Part.RENDER_MESH, new PRenderMesh(descriptor));
        
        ArrayList<Thing> things = this.getThings();
        synchronized(things) {
            things.add(thing);
        }
        
    }
    
    @Override public void initGL() {
        System.out.println("OpenGL version: " + effective.majorVersion + "." + effective.minorVersion + " (Profile: " + effective.profile + ")");
        createCapabilities();

        Vector4f ambcol = this.getLighting().fogColor;
        glClearColor(ambcol.x, ambcol.y, ambcol.z, 1.0f);

        glEnable(GL_PRIMITIVE_RESTART);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);

        TGV_SHADER = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(TGV_SHADER, FileIO.getResourceFileAsString("/shaders/default.vs"));
        glCompileShader(TGV_SHADER);
        if (glGetShaderi(TGV_SHADER, GL_COMPILE_STATUS) == 0)
            throw new AssertionError("Could not compile TGV SHADER! " + glGetShaderInfoLog(TGV_SHADER));

        int fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, FileIO.getResourceFileAsString("/shaders/fallback.fs"));
        glCompileShader(fragment);
        if (glGetShaderi(fragment, GL_COMPILE_STATUS) == 0)
            throw new AssertionError("Could not compile FALLBACK SHADER! " + glGetShaderInfoLog(fragment));

        int fallback = glCreateProgram();
        glAttachShader(fallback, CraftworldRenderer.TGV_SHADER);
        glAttachShader(fallback, fragment);
        glLinkProgram(fallback);

        if (glGetProgrami(fallback, GL_LINK_STATUS) == 0)
            throw new AssertionError("Could not link fallback program!");

        glDeleteShader(fragment);

        lastTime = glfwGetTime();

        FALLBACK_PROGRAM = new Shader(fallback);
    }

    public void update() {
        double time = glfwGetTime();
        deltaTime = (float) (time - lastTime);
        lastTime = time;
        time += deltaTime;

        this.render();
    }

    @Override public void paintGL() {
        int w = (int) (this.getWidth() * this.getGraphicsConfiguration().getDefaultTransform().getScaleX());
        int h = (int) (this.getHeight() * this.getGraphicsConfiguration().getDefaultTransform().getScaleY());
        Camera.MAIN.setAspectRatio((float) (((double)w) / ((double)h)));

        Camera.MAIN.recomputeProjectionMatrix();
        Camera.MAIN.recomputeViewMatrix();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glViewport(0, 0, w, h);

        PLevelSettings lighting = this.getLighting();
        ArrayList<Thing> things = this.getThings();
        synchronized(things) {
            for (Thing thing : things) {
                if (thing != null) 
                    thing.render(lighting);
            }
        }

        swapBuffers();
        glfwPollEvents();
    }
}
