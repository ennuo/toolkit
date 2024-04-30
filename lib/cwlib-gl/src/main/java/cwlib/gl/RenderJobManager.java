package cwlib.gl;

import org.lwjgl.opengl.GL;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class RenderJobManager
{
    private static RenderJobManager _instance;

    public static final int RENDER_TARGET_WIDTH = 2048;
    public static final int RENDER_TARGET_HEIGHT = 2048;

    public boolean wantQuit;

    private final Queue<RenderJob> workQueue = new ConcurrentLinkedQueue<>();

    private int linearFramebuffer;
    private int linearTexture;
    private int srgbFramebuffer;
    private int srbTexture;

    private boolean useSrgbFramebuffer;

    private long windowHandle;

    public RenderJobManager()
    {
        if (_instance != null)
            throw new RuntimeException("Only a single RenderJobManager may be instantiated!");
        Initialize();
    }

    public static RenderJobManager GetInstance()
    {
        if (_instance == null)
            _instance = new RenderJobManager();
        return _instance;
    }

    public void SetTransparentClearColor()
    {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void SetNormalClearColor()
    {
        glClearColor(0.5f, 0.5f, 1.0f, 1.0f);
    }

    private void Loop()
    {
        SetTransparentClearColor();
        while (!wantQuit && !glfwWindowShouldClose(windowHandle))
        {
            RenderJob job = workQueue.poll();
            if (job != null)
            {
                job.function.accept(this, job.userData);
                job.latch.countDown();
            }

            // This is running in the background, make sure it
            // doesn't consume a thread doing absolutely nothing.
            try { Thread.sleep(25); }
            catch (Exception ex) { continue; }
        }
    }

    public CountDownLatch EnqueueJob(BiConsumer<RenderJobManager, Object> function,
                                     Object argument)
    {
        RenderJob job = new RenderJob(function, argument);
        workQueue.add(job);
        return job.latch;
    }

    public BufferedImage GetResult(int startX, int startY, int w, int h)
    {
        ByteBuffer buffer =
            ByteBuffer.allocateDirect(RENDER_TARGET_WIDTH * RENDER_TARGET_HEIGHT * 4).order(ByteOrder.nativeOrder());
        glBindTexture(GL_TEXTURE_2D, useSrgbFramebuffer ? srbTexture : linearTexture);
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);


        for (int x = 0; x < w; ++x)
            for (int y = 0; y < h; ++y)
            {
                int i = ((startX + x) + (RENDER_TARGET_WIDTH * (startY + y))) * 4;
                int r = buffer.get(i) & 0xff;
                int g = buffer.get(i + 1) & 0xff;
                int b = buffer.get(i + 2) & 0xff;
                int a = buffer.get(i + 3) & 0xff;
                image.setRGB(w - (x + 1), h - (y + 1),
                    (a << 24) | (r << 16) | (g << 8) | b);
            }

        return image;
    }

    public void SetSRGB(boolean value)
    {
        useSrgbFramebuffer = value;

        if (value) glEnable(GL_FRAMEBUFFER_SRGB);
        else glDisable(GL_FRAMEBUFFER_SRGB);

        glBindFramebuffer(GL_FRAMEBUFFER, value ? srgbFramebuffer : linearFramebuffer);
    }

    private void Setup()
    {
        glDisable(GL_BLEND);
        glDisable(GL_STENCIL_TEST);
        glEnable(GL_PRIMITIVE_RESTART);
        glPrimitiveRestartIndex(0xFFFF);

        glEnable(GL_FRAMEBUFFER_SRGB);
        {
            srgbFramebuffer = glGenFramebuffers();
            srbTexture = glGenTextures();

            glBindFramebuffer(GL_FRAMEBUFFER, srgbFramebuffer);
            glBindTexture(GL_TEXTURE_2D, srbTexture);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB_ALPHA, RENDER_TARGET_WIDTH,
                RENDER_TARGET_HEIGHT, 0, GL_RGBA,
                GL_UNSIGNED_BYTE, (ByteBuffer) null);
            glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                srbTexture, 0);
        }

        glDisable(GL_FRAMEBUFFER_SRGB);
        {
            linearFramebuffer = glGenFramebuffers();
            linearTexture = glGenTextures();

            glBindFramebuffer(GL_FRAMEBUFFER, linearFramebuffer);
            glBindTexture(GL_TEXTURE_2D, linearTexture);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, RENDER_TARGET_WIDTH, RENDER_TARGET_HEIGHT, 0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                (ByteBuffer) null);
            glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                linearTexture, 0);
        }
    }

    private void Close()
    {
        glDeleteFramebuffers(linearFramebuffer);
        glDeleteTextures(linearTexture);

        glDeleteFramebuffers(srgbFramebuffer);
        glDeleteTextures(srbTexture);
    }

    private void Initialize()
    {
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW!");
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_VERSION_MINOR, 2);
        windowHandle = glfwCreateWindow(1, 1, "Toolkit Offscreen Renderer", NULL, NULL);
        if (windowHandle == NULL)
        {
            throw new RuntimeException("Failed to create GLFW window!");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            if (_instance != null)
                _instance.wantQuit = true;
        }));

        Thread jobThread = new Thread(() ->
        {
            glfwMakeContextCurrent(windowHandle);
            GL.createCapabilities();

            Setup();
            Loop();
            Close();

            glfwDestroyWindow(windowHandle);
            glfwTerminate();
        });

        jobThread.start();
    }
}
