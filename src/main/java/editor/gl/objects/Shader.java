package editor.gl.objects;

import cwlib.enums.GfxMaterialFlags;
import cwlib.resources.RGfxMaterial;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.util.FileIO;
import editor.gl.RenderSystem;
import editor.gl.RenderSystem.RenderMode;
import executables.gfx.GfxAssembler;
import executables.gfx.GfxAssembler.BrdfPort;

import static org.lwjgl.opengl.GL20.*;

import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

public class Shader {
    public static HashMap<ResourceDescriptor, Shader> PROGRAMS = new HashMap<>();
    public static final int MAX_BONES = 100;

    public int programID;
    public ResourceDescriptor descriptor;
    public boolean twoSided = false;
    public int alphaMode, alphaLayer;

    public int view, projection, light;
    public int[] matrices = new int[MAX_BONES];
    public int ambcol, fogcol, suncol, sunpos, rimcol, rimcol2;
    public int campos;
    public int lighscaleadd;
    public int color;

    public int[] locations = new int[8];

    public ResourceDescriptor[] textures = new ResourceDescriptor[8];
    public int shadowtex, cbuf;

    public Shader(int programID) {
        this.programID = programID;
        this.descriptor = null;
        this.twoSided = true;

        this.getUniforms();
    }

    public Shader(int vertex, String fragmentSource) {
        this.descriptor = null;
        this.twoSided = true;

        int fragment = Shader.compileSource(fragmentSource, GL_FRAGMENT_SHADER);
        this.programID = Shader.compileProgram(vertex, fragment);

        glDeleteShader(fragment);

        this.getUniforms();
    }

    public Shader(String vertexSource, String fragmentSource) {
        this.descriptor = null;
        this.twoSided = true;

        int vertex = Shader.compileSource(vertexSource, GL_VERTEX_SHADER);
        int fragment = Shader.compileSource(fragmentSource, GL_FRAGMENT_SHADER);
        this.programID = Shader.compileProgram(vertex, fragment);

        glDeleteShader(fragment);
        glDeleteShader(vertex);

        this.getUniforms();
    }

    public Shader(ResourceDescriptor descriptor) {
        System.out.println("Linking RGfxMaterial: " + descriptor);

        if (PROGRAMS.containsKey(descriptor))
            throw new RuntimeException("Program is already compiled and linked!");
        
        this.descriptor = descriptor;

        byte[] data = RenderSystem.getSceneGraph().getResourceData(descriptor);
        if (data == null)
            throw new RuntimeException("Unable to retrieve data for program!");
        RGfxMaterial gfx = new Resource(data).loadResource(RGfxMaterial.class);

        if (descriptor.isGUID()) {
            long guid = descriptor.getGUID().getValue();
            if (guid == 0x5407 || guid == 0x2a32 || guid == 0x436f || guid == 0x665f || guid == 0x2a35 || guid == 0x10775 || guid == 0x10c2c) {
                gfx.alphaLayer = (byte) 0xc0;
                gfx.alphaMode = 4;
            }
        }

        this.twoSided = (gfx.flags & GfxMaterialFlags.TWO_SIDED) != 0;
        this.alphaMode = gfx.alphaMode & 0xff;
        this.alphaLayer = gfx.alphaLayer & 0xff;

        if (this.alphaLayer == 0 && gfx.getBoxConnectedToPort(gfx.getOutputBox(), BrdfPort.ALPHA_CLIP) != null)
            this.alphaLayer = 0x1;
        
        String source = GfxAssembler.generateShaderSource(gfx, 0xDEADBEEF, false);
        int fragmentID = Shader.compileSource(source, GL_FRAGMENT_SHADER);
        this.programID = Shader.compileProgram(RenderSystem.getVertexShader(), fragmentID);
        glDeleteShader(fragmentID);

        this.getUniforms();
        this.textures = gfx.textures;

        // Preload textures
        for (ResourceDescriptor desc : gfx.textures)
            Texture.get(desc);

        PROGRAMS.put(descriptor, this);
    }

    private void getUniforms() {
        this.ambcol = glGetUniformLocation(this.programID, "ambcol");
        this.fogcol = glGetUniformLocation(this.programID, "fogcol");
        this.suncol = glGetUniformLocation(this.programID, "suncol");
        this.sunpos = glGetUniformLocation(this.programID, "sunpos");
        this.rimcol = glGetUniformLocation(this.programID, "rimcol");
        this.rimcol2 = glGetUniformLocation(this.programID, "rimcol2");

        this.light = glGetUniformLocation(this.programID, "light");
        this.view = glGetUniformLocation(this.programID, "view");
        this.projection = glGetUniformLocation(this.programID, "projection");
        for (int i = 0; i < MAX_BONES; ++i)
            this.matrices[i] = glGetUniformLocation(this.programID, "matrices[" + i + "]");
        
        this.campos = glGetUniformLocation(this.programID, "campos");

        this.lighscaleadd = glGetUniformLocation(this.programID, "lightscaleadd");

        this.color = glGetUniformLocation(this.programID, "thing_color");

        for (int i = 0; i < 8; ++i)
            this.locations[i] = glGetUniformLocation(this.programID, "s" + i);

        this.shadowtex = glGetUniformLocation(this.programID, "shadowtex");
        this.cbuf = glGetUniformLocation(this.programID, "cbuf");
    }

    public static Shader get(ResourceDescriptor descriptor) {        
        if (descriptor == null) return RenderSystem.getFallbackShader();
        if (PROGRAMS.containsKey(descriptor))
            return PROGRAMS.get(descriptor);
        if (RenderSystem.getSceneGraph().getResourceData(descriptor) == null)
            return RenderSystem.getFallbackShader();
        try { return new Shader(descriptor); } 
        catch (Exception ex) { 
            System.out.println(descriptor + " failed to load! Falling back to default shader.");
            PROGRAMS.put(descriptor, RenderSystem.getFallbackShader());
            return RenderSystem.getFallbackShader(); 
        }
    }

    public void setUniformFloat4(int location, Vector4f v) {
        if (location == -1) return;
        glUniform4f(location, v.x, v.y, v.z, v.w);
    }

    public void setUniformFloat3(int location, Vector3f v) {
        if (location == -1) return;
        glUniform3f(location, v.x, v.y, v.z);
    }

    public void setUniformFloat2(int location, Vector2f v) {
        if (location == -1) return;
        glUniform2f(location, v.x, v.y);
    }

    public void setUniformMatrix(int location, Matrix4f matrix) {
        if (location == -1) return;
        if (matrix == null) matrix = new Matrix4f().identity();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(location, false, matrix.get(stack.mallocFloat(16)));
        }
    }

    public void bind(Texture remap, Matrix4f[] model, Vector4f color) {
        PLevelSettings lighting = RenderSystem.getLevelSettings();
        glUseProgram(this.programID);
        
        // if (this.twoSided) glDisable(GL_CULL_FACE);
        // else glEnable(GL_CULL_FACE);

        if (this.alphaMode != 0) {
            glEnable(GL_BLEND);
            if (this.alphaMode == 4) 
                glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            else if (this.alphaMode == 3)
                glBlendFunc(GL_ONE, GL_ONE);
            else if (this.alphaMode == 2)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            else
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        } else glDisable(GL_BLEND);
        

        // Set level lighting uniforms
        setUniformFloat4(this.ambcol, lighting.ambientColor);
        setUniformFloat4(this.fogcol, lighting.fogColor);
        setUniformFloat4(this.suncol, lighting.sunColor);
        setUniformFloat4(this.rimcol, lighting.rimColor);
        setUniformFloat4(this.rimcol2, lighting.rimColor2);

        Vector3f sunPositionScaled = new Vector3f(lighting.sunPosition).mul(lighting.sunPositionScale);

        setUniformFloat3(this.sunpos, sunPositionScaled);

        // Set camera uniforms
        setUniformFloat3(this.campos, RenderSystem.getMainCamera().getTranslation());
        setUniformFloat2(this.lighscaleadd, new Vector2f(lighting.sunMultiplier, lighting.exposure));
        
        setUniformMatrix(this.projection, RenderSystem.getMainCamera().getProjectionMatrix());
        for (int i = 0; i < model.length; ++i)
            setUniformMatrix(this.matrices[i], model[i]);
        setUniformMatrix(this.view, RenderSystem.getMainCamera().getViewMatrix());

        // shadow_z_min = -1700
        // shadow_z_max = 100

        // shadow_zfar_max = 50,000
        // shadow_zfar_min = 4000
        // shadow_znear_max = 10.0
        // shadow_znear_min = 200.0

        Vector3f sunDir = lighting.sunPosition;
        // Vector3f lightDir = lighting.sunPosition.mul(-1.0f, new Vector3f());
        Vector3f campos = RenderSystem.getMainCamera().getTranslation();

        float fov = 1.0f;
        float nearDist = 2.0f;
        float farDist = 10000.0f;
        float Hnear = (float) (2 * Math.tan(fov / 2.0) * nearDist);
        float Wnear = Hnear;
        float Hfar = (float) (2 * Math.tan(fov / 2.0) * farDist);
        float Wfar = Hfar;
        
        Vector3f centerFar = campos.add(new Vector3f(0.0f, 0.0f, -1.0f).mul(farDist), new Vector3f());

        Vector3f topLeftFar = centerFar.add(new Vector3f(0.0f, 1.0f, 0.0f).mul(Hfar / 2.0f), new Vector3f()).sub(new Vector3f(1.0f, 0.0f, 0.0f).mul(Wfar / 2.0f));
        Vector3f topRightFar = centerFar.add(new Vector3f(0.0f, 1.0f, 0.0f).mul(Hfar / 2.0f), new Vector3f()).add(new Vector3f(1.0f, 0.0f, 0.0f).mul(Wfar / 2.0f));
        Vector3f bottomLeftFar = centerFar.sub(new Vector3f(0.0f, 1.0f, 0.0f).mul(Hfar / 2.0f), new Vector3f()).sub(new Vector3f(1.0f, 0.0f, 0.0f).mul(Wfar / 2.0f));
        Vector3f bottomRightFar = centerFar.sub(new Vector3f(0.0f, 1.0f, 0.0f).mul(Hfar / 2.0f), new Vector3f()).add(new Vector3f(1.0f, 0.0f, 0.0f).mul(Wfar / 2.0f));

        Vector3f centerNear = campos.add(new Vector3f(0.0f, 0.0f, -1.0f).mul(nearDist), new Vector3f());

        Vector3f topLeftNear = centerNear.add(new Vector3f(0.0f, 1.0f, 0.0f).mul(Hnear / 2.0f), new Vector3f()).sub(new Vector3f(1.0f, 0.0f, 0.0f).mul(Wnear / 2.0f));
        Vector3f topRightNear = centerNear.add(new Vector3f(0.0f, 1.0f, 0.0f).mul(Hnear / 2.0f), new Vector3f()).add(new Vector3f(1.0f, 0.0f, 0.0f).mul(Wnear / 2.0f));
        Vector3f bottomLeftNear = centerNear.sub(new Vector3f(0.0f, 1.0f, 0.0f).mul(Hnear / 2.0f), new Vector3f()).sub(new Vector3f(1.0f, 0.0f, 0.0f).mul(Wnear / 2.0f));
        Vector3f bottomRightNear = centerNear.sub(new Vector3f(0.0f, 1.0f, 0.0f).mul(Hnear / 2.0f), new Vector3f()).add(new Vector3f(1.0f, 0.0f, 0.0f).mul(Wnear / 2.0f));

        // Vector3f frustumCenter = centerFar.sub(centerNear, new Vector3f()).mul(0.5f);

        Matrix4f lightView = new Matrix4f().lookAt(
            sunDir,
            new Vector3f(0.0f, 0.0f, 0.0f),
            new Vector3f(0.0f, 1.0f, 0.0f)
        );
        
        Vector4f[] frustumToLightview = new Vector4f[] {
            new Vector4f(bottomRightNear, 1.0f).mul(lightView),
            new Vector4f(topRightNear, 1.0f).mul(lightView),
            new Vector4f(bottomLeftNear, 1.0f).mul(lightView),
            new Vector4f(topLeftNear, 1.0f).mul(lightView),
            new Vector4f(bottomRightFar, 1.0f).mul(lightView),
            new Vector4f(topRightFar, 1.0f).mul(lightView),
            new Vector4f(bottomLeftFar, 1.0f).mul(lightView),
            new Vector4f(topLeftFar, 1.0f).mul(lightView),
        };

        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        for (Vector4f v : frustumToLightview) {
            if (v.x < min.x) min.x = v.x;
            if (v.y < min.y) min.y = v.y;
            if (v.z < min.z) min.z = v.z;

            if (v.x > max.x) max.x = v.x;
            if (v.y > max.y) max.y = v.y;
            if (v.z > max.z) max.z = v.z;
        }

        float l = min.x, r = max.x, b = min.y, t = max.y;
        float n = -max.z, f = -min.z;

        Matrix4f lightProjection = (new Matrix4f()).ortho(l, r, b, t, n, f);

        // setUniformMatrix(this.projection, lightProjection);
        // setUniformMatrix(this.view, lightView);

        Matrix4f lightmatrix = lightProjection.mul(lightView);
        if (RenderSystem.getRenderPath() == RenderMode.RENDER) {
            lightmatrix = new Matrix4f(
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f
            ).mul(lightmatrix);
        }
        setUniformMatrix(this.light, lightmatrix);

        if (color != null)
            setUniformFloat4(this.color, color);

        for (int i = 0; i < 8; ++i) {
            if (this.textures[i] == null) continue;
            Texture texture = null;
            
            if (i == 0 && remap != null)
                texture = remap;
            else
                texture = Texture.get(this.textures[i]);

            if (texture == null) continue;

            glActiveTexture(GL_TEXTURE0 + i);

            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glUniform1i(this.locations[i], i);
            glBindTexture(GL_TEXTURE_2D, texture.textureID);
        }

        int shadowTextureID = RenderSystem.getShadowMapTexture();
        if (shadowTextureID != -1 && this.shadowtex != -1) {
            glActiveTexture(GL_TEXTURE0 + 12);
            glUniform1i(this.shadowtex, 12);
            glBindTexture(GL_TEXTURE_2D, shadowTextureID);
        }
        
        if (this.cbuf != -1) {
            glActiveTexture(GL_TEXTURE0 + 15);
            glUniform1i(this.cbuf, 15);
            glBindTexture(GL_TEXTURE_2D, RenderSystem.getColorBufferTexture());
        }

        glActiveTexture(GL_TEXTURE0);
    }

    public void delete() {
        PROGRAMS.remove(this.descriptor);
        if (this.programID == 0) return;

        glDeleteProgram(this.programID);
        this.programID = 0;
    }

    public static int compileProgram(int vertex, int fragment) {
        int program = glCreateProgram();
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == 0)
            throw new AssertionError("Could not link shader program! " + glGetProgramInfoLog(program));
        return program;
    }

    public static int compileSource(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.out.println(source);
            throw new AssertionError("Could not compile shader! " + glGetShaderInfoLog(shader));
        }
        return shader;
    }
}
