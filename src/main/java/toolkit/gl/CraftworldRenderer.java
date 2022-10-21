package toolkit.gl;

import cwlib.resources.custom.RSceneGraph;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.util.FileIO;
import editor.gl.Camera;
import editor.gl.RenderSystem;

import static org.lwjgl.opengl.GL.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.joml.Vector3f;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

public class CraftworldRenderer extends AWTGLCanvas {
    public static class ViewportListener implements KeyListener {
        @Override public void keyTyped(KeyEvent e) { return; }
        @Override public void keyReleased(KeyEvent e) { return; }
        @Override public void keyPressed(KeyEvent e) {
            Vector3f translation = RenderSystem.getMainCamera().getTranslation();
            int code = e.getKeyCode();

            float displacement = 3.0f * 50.0f;
            if (code == KeyEvent.VK_D) translation.x += displacement;
            else if (code == KeyEvent.VK_A) translation.x -= displacement;
            else if (code == KeyEvent.VK_W) translation.y += displacement;
            else if (code == KeyEvent.VK_S) translation.y -= displacement;

            RenderSystem.getMainCamera().setTranslation(translation);
        }
    }

    private static final long serialVersionUID = 1L;
    private static final GLData GL_DATA = new GLData();
    static {
        GL_DATA.samples = 4;
        GL_DATA.swapInterval = 1;
        GL_DATA.majorVersion = 3;
        GL_DATA.minorVersion = 3;
    }

    public CraftworldRenderer() {
        super(GL_DATA);

        ResourceSystem.DISABLE_LOGS = true;
        
        byte[] data = FileIO.getResourceFile("/binary/default.sg");
        RSceneGraph graph = new Resource(data).loadResource(RSceneGraph.class);

        RenderSystem.setSceneGraph(graph);

        ResourceSystem.DISABLE_LOGS = false;

        this.setupAWT();
    }

    private void setupAWT() {
        this.addMouseWheelListener(event -> {
            RenderSystem.getMainCamera().setPosZ(RenderSystem.getMainCamera().getTranslation().z + (float) (event.getUnitsToScroll() * 25.0));
        });
        this.addKeyListener(new ViewportListener());
    }

    @Override public void initGL() {
        System.out.println("OpenGL version: " + effective.majorVersion + "." + effective.minorVersion + " (Profile: " + effective.profile + ")");
        createCapabilities();
        RenderSystem.initialize();
    }
    
    @Override public void paintGL() {
        int cw = (int) (this.getWidth() * this.getGraphicsConfiguration().getDefaultTransform().getScaleX());
        int ch = (int) (this.getHeight() * this.getGraphicsConfiguration().getDefaultTransform().getScaleY());
        
        Camera camera = RenderSystem.getMainCamera();

        camera.setAspectRatio((float) (((double)cw) / ((double)ch)));

        camera.recomputeProjectionMatrix();
        camera.recomputeViewMatrix();

        // Renders color and shadow buffers
        RenderSystem.render(cw, ch);

        swapBuffers();
    }
}
