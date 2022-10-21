package editor;

import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import cwlib.resources.custom.RSceneGraph;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.util.FileIO;
import editor.gl.Camera;
import editor.gl.RenderSystem;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Editor {
    private long window;

    static int w;
    static int h;

    private void run() {
        this.create();
        this.update();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void input() {
        Vector3f translation = RenderSystem.getMainCamera().getTranslation();
        float displacement = 3.0f * 800.0f * RenderSystem.getDeltaTime();

        boolean hypershift = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_TRUE;

        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_TRUE) translation.x += displacement;
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_TRUE) translation.x -= displacement;
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_TRUE) {
            if (hypershift) translation.z -= displacement;
            else translation.y += displacement;
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_TRUE) {
            if (hypershift) translation.z += displacement;
            else translation.y -= displacement;
        }

        RenderSystem.getMainCamera().setTranslation(translation);
    }

    private void create() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW!");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);


        this.window = glfwCreateWindow(1280, 720, "Editor", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window!");
            
        glfwSetScrollCallback(window, GLFWScrollCallback.create((window, xoffset, yoffset) -> {
            RenderSystem.getMainCamera().setPosZ(RenderSystem.getMainCamera().getTranslation().z - (float) (yoffset * 12.0f * 800.0f * RenderSystem.getDeltaTime()));
        }));

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            IntBuffer fWidth = stack.mallocInt(1);
            IntBuffer fHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwGetFramebufferSize(window, fWidth, fHeight);

            w = fWidth.get(0);
            h = fHeight.get(0);

            glfwSetWindowPos(
                window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private void update() {
        GL.createCapabilities();
        RenderSystem.initialize();

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        while (!glfwWindowShouldClose(window)) {
            this.input();

            Camera camera = RenderSystem.getMainCamera();
            camera.setAspectRatio(1280.0f / 720.0f);
            camera.recomputeProjectionMatrix();
            camera.recomputeViewMatrix();


            RenderSystem.render(w, h);
            
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        // Config.initialize();
        ResourceSystem.GUI_MODE = false;

        // Profile profile = Config.instance.getCurrentProfile();
        // if (profile == null) return;
        
        // if (profile.archives != null) {
        //     for (String path : profile.archives) {
        //         if (Files.exists(Paths.get(path)))
        //             ResourceSystem.getArchives().add(new FileArchive(new File(path)));
        //     }
        // }
        
        // if (profile.databases != null) {
        //     for (String path : profile.databases) {
        //         if (Files.exists(Paths.get(path)))
        //             ResourceSystem.getDatabases().add(new FileDB(new File(path)));
        //     }
        // }
        
        byte[] data = FileIO.getResourceFile("/binary/default.sg");
        RSceneGraph graph = new Resource(data).loadResource(RSceneGraph.class);
        RenderSystem.setSceneGraph(graph);

        new Editor().run();
    }
}
