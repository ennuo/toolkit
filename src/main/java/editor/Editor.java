package editor;

import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import configurations.Config;
import configurations.Profile;
import cwlib.enums.BoxType;
import cwlib.enums.GfxMaterialFlags;
import cwlib.enums.NodeType;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.enums.ShadowCastMode;
import cwlib.resources.RGfxMaterial;
import cwlib.resources.custom.RSceneGraph;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.gmat.MaterialWire;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PCostume;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.types.Resource;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.databases.FileDB;
import cwlib.util.FileIO;
import cwlib.util.Strings;
import editor.gl.Camera;
import editor.gl.RenderSystem;
import editor.gl.objects.Shader;
import editor.gl.objects.Texture;
import executables.gfx.GfxAssembler;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiViewport;
import imgui.ImVec2;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.io.File;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Editor {
    private long window;

    static int w;
    static int h;

    private static final ImNodesContext CONTEXT = new ImNodesContext();


    private final ImGuiImplGlfw imGuiGLFW = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imguiGL3 = new ImGuiImplGl3();

    private static Shader shader = null;
    private static Thing sackboy = null;
    public static ResourceDescriptor WORLD_SHADER = new ResourceDescriptor(4294967294l, ResourceType.GFX_MATERIAL);

    private static RGfxMaterial material;
    static {
        material = new RGfxMaterial();
        material.boxes.add(new MaterialBox());
        
        // material = new Resource("C:/Users/Aidan/Desktop/flowers.gmat").loadResource(RGfxMaterial.class);
    }

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
            
        // glfwSetScrollCallback(window, GLFWScrollCallback.create((window, xoffset, yoffset) -> {
        //     RenderSystem.getMainCamera().setPosZ(RenderSystem.getMainCamera().getTranslation().z - (float) (yoffset * 12.0f * 800.0f * RenderSystem.getDeltaTime()));
        // }));

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

        GL.createCapabilities();
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        // io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        imGuiGLFW.init(this.window, true);
        imguiGL3.init("#version 330");

        ImNodes.createContext();
        ImNodes.editorContextSet(CONTEXT);

        for (MaterialBox box  : material.boxes) {
            if (box.type == BoxType.MULTIPLY) {
                box.type = BoxType.MATH;
                box.getParameters()[6] = 2;
            }
            ImNodes.setNodeScreenSpacePos(box.id, box.x, box.y);
        }
    }

    private void createInputPort(String name, int id) {
        ImNodes.beginInputAttribute(id, ImNodesPinShape.CircleFilled);
        ImGui.textUnformatted(name);
        ImNodes.endInputAttribute();
    }

    private void createOutputPort(String name, int id) {
        this.createOutputPort(name, id, 100.0f);
    }

    private void createOutputPort(String name, int id, float boxSize) {
        ImVec2 size = new ImVec2();
        
        ImGui.calcTextSize(size, name);
        float width = size.x;

        ImNodes.beginOutputAttribute(id);
        ImGui.indent(boxSize - width);
        ImGui.textUnformatted(name);
        ImNodes.endOutputAttribute();
    }

    private void ui() {


        ImNodes.beginNodeEditor();

        
        boolean hasChanges = false;

        for (MaterialBox box : material.boxes) {
            
            ImNodes.beginNode(box.id);

            int[] parameters = box.getParameters();

            switch (box.type) {
                case BoxType.OUTPUT: {
                    String title = "BRDF Output";

                    final String[] brdfPorts = {
                        "Diffuse",
                        "Alpha",
                        "Specular",
                        "Bump",
                        "Glow",
                        null,
                        "Reflection",
                        null,
                        "Anisotrophic Direction",
                        "Trans",
                        "Color Correction",
                        "Fuzz",
                        "Reflectance",
                        "Toon Ramp"
                    };

                    final String[] pbrPorts = {
                        "Albedo",
                        "Normal",
                        "Specular",
                        "Roughness",
                        "Metallic",
                        "Ambient Occlusion"
                    };

                    final String[] unlitPorts = {
                        "Diffuse"
                    };

                    String[] ports = brdfPorts;
                    if (parameters[0] == 1) {
                        title = "PBR Output";
                        ports = pbrPorts;
                    } else if (parameters[0] == 2) {
                        title = "Unlit Output";
                        ports = unlitPorts;
                    }

                    ImNodes.beginNodeTitleBar();
                    ImGui.text(title);
                    ImNodes.endNodeTitleBar();

                    for (int i = 0; i < ports.length; ++i) {
                        if (ports[i] == null) continue;

                        this.createInputPort(ports[i], box.inputs[i]);
                    }
                    
                    break;
                }


                case BoxType.TEXTURE_SAMPLE: {
                    ImNodes.beginNodeTitleBar();
                    ImGui.text("Texture Sample");
                    ImNodes.endNodeTitleBar();

                    float[] scale = new float[] { 
                        Float.intBitsToFloat(parameters[0]),
                        Float.intBitsToFloat(parameters[1])
                    };

                    float[] offset = new float[] { 
                        Float.intBitsToFloat(parameters[2]),
                        Float.intBitsToFloat(parameters[3])
                    };

                    ImInt channel = new ImInt(parameters[4]);
                    ImInt texture = new ImInt(parameters[5]);

                    ImGui.pushItemWidth(100.0f);
                    Texture glTexture = Texture.get(material.textures[parameters[5]]);
                    if (glTexture != null) {

                        ImGui.image(glTexture.textureID, 32.0f, 32.0f);

                        ImGui.sameLine();

                        ImGui.beginGroup();

                        hasChanges |= ImGui.dragFloat2("Offset", offset, 0.1f);
                        hasChanges |= ImGui.dragFloat2("Scale", scale, 0.1f);

                        ImGui.endGroup();
                    } else {
                        hasChanges |= ImGui.dragFloat2("Offset", offset, 0.1f);
                        hasChanges |= ImGui.dragFloat2("Scale", scale, 0.1f);
                    }
                    

                    hasChanges |= ImGui.combo("Channel", channel, new String[] { "UV0", "UV1" });
                    hasChanges |= ImGui.combo("Texture", texture, new String[] { "TEXTURE0", "TEXTURE1", "TEXTURE2", "TEXTURE3", "TEXTURE4", "TEXTURE5", "TEXTURE6", "TEXTURE7" });



                    ImGui.popItemWidth();

                    parameters[0] = Float.floatToIntBits(scale[0]);
                    parameters[1] = Float.floatToIntBits(scale[1]);

                    parameters[2] = Float.floatToIntBits(offset[0]);
                    parameters[3] = Float.floatToIntBits(offset[1]);

                    parameters[4] = channel.get();

                    parameters[5] = texture.get();

                    ImGui.spacing();

                    this.createInputPort("Add", box.inputs[0]);
                    this.createInputPort("Scale", box.inputs[1]);
                    this.createInputPort("Subtract", box.inputs[2]);

                    ImGui.sameLine();


                    this.createOutputPort("Color", box.outputs[0], 100);
                    this.createOutputPort("Alpha", box.outputs[1], 100);


                    break;
                }

                case BoxType.MULTIPLY: {
                    ImNodes.beginNodeTitleBar();
                    ImGui.text("Multiply");
                    ImNodes.endNodeTitleBar();

                    this.createInputPort("X", box.inputs[0]);
                    this.createInputPort("Y", box.inputs[1]);

                    ImGui.sameLine();

                    this.createOutputPort("Output", box.outputs[0]);

                    break;
                }

                case BoxType.COLOR: {
                    ImNodes.beginNodeTitleBar();
                    ImGui.text("RGBA");
                    ImNodes.endNodeTitleBar();

                    float[] color = new float[] {
                        Float.intBitsToFloat(parameters[0]),
                        Float.intBitsToFloat(parameters[1]),
                        Float.intBitsToFloat(parameters[2]),
                        Float.intBitsToFloat(parameters[3])
                    };

                    // ImGui.colorButton("Color", color);


                    ImGui.pushItemWidth(100.0f);
                    if (ImGui.colorPicker4("Color", color, ImGuiColorEditFlags.PickerHueWheel | ImGuiColorEditFlags.DisplayRGB)) {
                        parameters[0] = Float.floatToIntBits(color[0]);
                        parameters[1] = Float.floatToIntBits(color[1]);
                        parameters[2] = Float.floatToIntBits(color[2]);
                        parameters[3] = Float.floatToIntBits(color[3]);
                        hasChanges |= true;
                    }
                    ImGui.popItemWidth();


                    // ImGui.sameLine();
                
                    // ImGui.colorEdit4("Color", color);



                    this.createOutputPort("Output", box.outputs[0]);

                    break;
                }

                case BoxType.CONSTANT: {
                    ImNodes.beginNodeTitleBar();
                    ImGui.text("Value");
                    ImNodes.endNodeTitleBar();

                    float[] value = new float[] { Float.intBitsToFloat(parameters[0]) };

                    ImGui.pushItemWidth(100.0f);
                    if (ImGui.dragFloat("##hidelabel", value)) {
                        hasChanges = true;
                        parameters[0] = Float.floatToIntBits(value[0]);
                    }

                    ImGui.sameLine();
                    
                    this.createOutputPort("", box.outputs[0]);


                    ImGui.popItemWidth();

                    break;
                }

                case 0x82: {
                    ImNodes.beginNodeTitleBar();
                    ImGui.text("Texture Coordinate");
                    ImNodes.endNodeTitleBar();

                    ImInt channel = new ImInt(parameters[0]);
                    ImGui.pushItemWidth(100.0f);
                    hasChanges |= ImGui.combo("Channel", channel, new String[] { "UV0", "UV1", "Decal UV" });
                    ImGui.popItemWidth();
                    parameters[0] = channel.get();

                    this.createOutputPort("Output", box.outputs[0]);

                    break;
                }

                case 0x89: {
                    String name = NodeType.MATH_MODES[parameters[6]];
                    ImNodes.beginNodeTitleBar();
                    ImGui.text(name);
                    ImNodes.endNodeTitleBar();

                    // ImVec2 size = new ImVec2();
                    // ImGui.calcTextSize(size, name);
                    // float width = size.x;
                    // ImGui.calcTextSize(size, "Value");
        
                    // ImGui.indent(width - size.x);
            
                    ImNodes.beginOutputAttribute(box.outputs[0]);
                    ImGui.text("Value");
                    ImNodes.endOutputAttribute();


                    ImGui.spacing();

                    ImInt type = new ImInt(parameters[6]);
                    ImGui.pushItemWidth(100.0f);
                    if (ImGui.combo("", type, NodeType.MATH_MODES)) {
                        hasChanges = true;
                        int newType = type.get();
                        if (parameters[6] == 4 && newType != 4) {
                            int wire = material.isWireConnected(box.inputs[2]);
                            if (wire != -1)
                                material.removeWire(wire);
                        }
                        parameters[6] = type.get();
                    }

                    ImGui.checkbox("Clamp", false);
                    ImGui.spacing();

                    ImGui.popItemWidth();

                    
                    float[] a = new float[] { Float.intBitsToFloat(parameters[0]) };
                    float[] b = new float[] { Float.intBitsToFloat(parameters[1]) };

                    ImNodes.beginInputAttribute(box.inputs[0]);
                    ImGui.textUnformatted("A");
                    if (material.isWireConnected(box.inputs[0]) == -1) {
                        ImGui.pushItemWidth(100.0f);
                        ImGui.sameLine();
                        if (ImGui.dragFloat("##hidelabel", a)) {
                            hasChanges = true;
                            parameters[0] = Float.floatToIntBits(a[0]);
                        }

                        ImGui.popItemWidth();

                    }
                    ImNodes.endInputAttribute();


                    ImNodes.beginInputAttribute(box.inputs[1]);
                    ImGui.textUnformatted("B");
                    if (material.isWireConnected(box.inputs[1]) == -1) {
                        ImGui.pushItemWidth(100.0f);
                        ImGui.sameLine();
                        if (ImGui.dragFloat("##hidelabel", b)) {
                            hasChanges = true;
                            parameters[1] = Float.floatToIntBits(b[0]);
                        }
                        ImGui.popItemWidth();
                    }
                    ImNodes.endInputAttribute();

                    if (parameters[6] == 4) {
                        float[] c = new float[] { Float.intBitsToFloat(parameters[2]) };
                        ImNodes.beginInputAttribute(box.inputs[2]);
                        ImGui.textUnformatted("C");
                        if (material.isWireConnected(box.inputs[2]) == -1) {
                            ImGui.pushItemWidth(100.0f);
                            ImGui.sameLine();
                            if (ImGui.dragFloat("##hidelabel", c)) {
                                hasChanges = true;
                                parameters[2] = Float.floatToIntBits(c[0]);
                            }
                            ImGui.popItemWidth();
                        }
                        ImNodes.endInputAttribute();
                    }

                    break;
                }

                default: {
                    ImNodes.beginNodeTitleBar();
                    ImGui.text(NodeType.getNode(box.type).getName());
                    ImNodes.endNodeTitleBar();

                    this.createOutputPort("Output", box.outputs[0]);


                }
            }

            // ImNodes.beginNodeTitleBar();
            // ImGui.text("Texture Sample");
            // ImNodes.endNodeTitleBar();

            // ImNodes.beginInputAttribute(box.inputs[0], ImNodesPinShape.CircleFilled);
            // ImGui.text("Input");
            // ImNodes.endInputAttribute();

            // ImGui.sameLine();

            // ImNodes.beginOutputAttribute(box.outputs[0]);
            // ImGui.text("Output");
            // ImNodes.endOutputAttribute();

            ImNodes.endNode();
        }

        for (MaterialWire wire : material.wires) {
            int portTo = wire.portTo;
            int portFrom = wire.portFrom;

            if (portTo > 12)
                portTo -= 0xa0;
            
            ImNodes.link(
                wire.id, 
                material.boxes.get(wire.boxFrom).outputs[portFrom],
                material.boxes.get(wire.boxTo).inputs[portTo]
            ); 
        }

        boolean isEditorHovered = ImNodes.isEditorHovered();
        ImNodes.endNodeEditor();

        ImInt start = new ImInt(), end = new ImInt();
        if (ImNodes.isLinkCreated(start, end))  {
            material.addWire(start.get(), end.get());
            this.recompile();
        }

        ImInt destroyedLinkID = new ImInt();
        if (ImNodes.isLinkDestroyed(end)) {
            material.removeWire(destroyedLinkID.get());
            this.recompile();
        }


        if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
            int node = ImNodes.getHoveredNode();
            int wire = ImNodes.getHoveredLink();

            if (wire != -1) {
                ImGui.openPopup("wire_context");
                ImGui.getStateStorage().setInt(ImGui.getID("delete_wire_id"), wire);
            } else if (node != -1) {
                ImGui.openPopup("node_context");
                ImGui.getStateStorage().setInt(ImGui.getID("delete_node_id"), node);
            } else if (isEditorHovered) {
                ImGui.openPopup("node_editor_context");
            }
        }

        if (ImGui.isPopupOpen("wire_context")) {
            int wire = ImGui.getStateStorage().getInt(ImGui.getID("delete_wire_id"));
            if (ImGui.beginPopup("wire_context")) {
                if (ImGui.button("Delete Wire")) {
                    material.removeWire(wire);
                    this.recompile();
                    ImGui.closeCurrentPopup();
                }
                ImGui.endPopup();
            }
        }

        if (ImGui.isPopupOpen("node_context")) {
            int node = ImGui.getStateStorage().getInt(ImGui.getID("delete_node_id"));
            if (ImGui.beginPopup("node_context")) {
                if (ImGui.button("Delete Node")) {
                    material.removeBox(node);
                    ImGui.closeCurrentPopup();
                    this.recompile();
                }
                ImGui.endPopup();
            }
        }

        if (ImGui.beginPopup("node_editor_context")) {
            ImGui.text("Add Node");
            ImGui.spacing();

            NodeType[] colorNodes = NodeType.getNodesInFolder("Color");
            NodeType[] inputNodes = NodeType.getNodesInFolder("Input");
            NodeType[] textureNodes = NodeType.getNodesInFolder("Texture");
            NodeType[] converterNodes = NodeType.getNodesInFolder("Converter");
            

            
            if (ImGui.beginMenu("Input")) {
                for (NodeType type : inputNodes) {
                    if (ImGui.menuItem(type.getName())) {
                        MaterialBox box = type.create();
                        ImNodes.setNodeScreenSpacePos(box.id, ImGui.getMousePosX(), ImGui.getMousePosY());
                        material.boxes.add(box);
                    }
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Shader")) {
                MaterialBox box = null;

                
                if (ImGui.menuItem("BRDF"))
                    box = new MaterialBox();

                if (ImGui.menuItem("PBR")) {
                    box = new MaterialBox();
                    box.getParameters()[0] = 1;
                }

                if (ImGui.menuItem("Unlit")) {
                    box = new MaterialBox();
                    box.getParameters()[0] = 2;
                }

                if (box != null) {
                    ImNodes.setNodeScreenSpacePos(box.id, ImGui.getMousePosX(), ImGui.getMousePosY());
                    material.boxes.add(box);
                }

                ImGui.endMenu();
            }


            if (ImGui.beginMenu("Texture")) {
                for (NodeType type : textureNodes) {
                    if (ImGui.menuItem(type.getName())) {
                        MaterialBox box = type.create();
                        ImNodes.setNodeScreenSpacePos(box.id, ImGui.getMousePosX(), ImGui.getMousePosY());
                        material.boxes.add(box);
                    }
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Color")) {
                for (NodeType type : colorNodes) {
                    if (ImGui.menuItem(type.getName())) {
                        MaterialBox box = type.create();
                        ImNodes.setNodeScreenSpacePos(box.id, ImGui.getMousePosX(), ImGui.getMousePosY());
                        material.boxes.add(box);
                    }
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Converter")) {
                for (NodeType type : converterNodes) {
                    if (ImGui.menuItem(type.getName())) {
                        MaterialBox box = type.create();
                        ImNodes.setNodeScreenSpacePos(box.id, ImGui.getMousePosX(), ImGui.getMousePosY());
                        material.boxes.add(box);
                    }
                }
                ImGui.endMenu();
            }


            ImGui.endPopup();
        }

        if (ImGui.begin("Preview")) {

            Camera camera = RenderSystem.getMainCamera();


            ImVec2 viewportPanelSize = ImGui.getContentRegionAvail();

            camera.setAspectRatio(viewportPanelSize.x / viewportPanelSize.y);
            camera.recomputeProjectionMatrix();
            camera.recomputeViewMatrix();

            ImGui.image(RenderSystem.getColorBufferTexture(), viewportPanelSize.x, viewportPanelSize.y, 0.0f, 1.0f, 1.0f, 0.0f);


            ImGui.end();
        }
        

        ImInt flags = new ImInt(material.flags);
        if (ImGui.begin("Material")) {
            if (ImGui.collapsingHeader("Flags")) {
                ImGui.checkboxFlags("Receive Shadows", flags, GfxMaterialFlags.RECEIVE_SHADOWS);
                ImGui.checkboxFlags("Receive Sun", flags, GfxMaterialFlags.RECEIVE_SUN);
                ImGui.checkboxFlags("Receive Spritelight", flags, GfxMaterialFlags.RECEIVE_SPRITELIGHTS);
                ImGui.checkboxFlags("Max Priority", flags, GfxMaterialFlags.MAX_PRIORITY);
                ImGui.checkboxFlags("Squishy", flags, GfxMaterialFlags.SQUISHY);
                ImGui.checkboxFlags("No Instance Texture", flags, GfxMaterialFlags.NO_INSTANCE_TEXTURE);
                ImGui.checkboxFlags("Wire", flags, GfxMaterialFlags.WIRE);
                ImGui.checkboxFlags("Furry", flags, GfxMaterialFlags.FURRY);
                ImGui.checkboxFlags("Two-Sided", flags, GfxMaterialFlags.TWO_SIDED);

                material.flags = flags.get();
            }

            ImInt alphaLayer = new ImInt(material.alphaLayer & 0xff);
            ImFloat alphaCut = new ImFloat(material.alphaTestLevel);
            ImFloat bumpLevel = new ImFloat(material.bumpLevel);
            ImInt shadowCastMode = new ImInt(material.shadowCastMode.getValue());
            ImFloat cosinePower = new ImFloat(material.cosinePower);

            if (ImGui.collapsingHeader("Properties")) {

                hasChanges |= ImGui.sliderScalar("AlphaCut", ImGuiDataType.Float, alphaCut, 0.0f, 1.0f);
                hasChanges |= ImGui.sliderScalar("AlphaLayer", ImGuiDataType.S32, alphaLayer, 0, 255);
                hasChanges |= ImGui.combo("ShadowCastMode", shadowCastMode, new String[] { "OFF", "ON", "ALPHA" });
                hasChanges |= ImGui.sliderScalar("BumpLevel", ImGuiDataType.Float, bumpLevel, 0.0f, 1.0f);
                hasChanges |= ImGui.sliderScalar("CosinePower", ImGuiDataType.Float, cosinePower, 0.0f, 32.0f);

                material.alphaTestLevel = alphaCut.get();
                material.alphaLayer = (byte) alphaLayer.get();
                material.bumpLevel = bumpLevel.get();
                material.shadowCastMode = ShadowCastMode.fromValue((byte) shadowCastMode.get());
                material.cosinePower = cosinePower.get();
            }

            if (ImGui.collapsingHeader("Textures", ImGuiTreeNodeFlags.DefaultOpen)) {
                for (int i = 0; i < 8; ++i) {
                    String text = "<none>";
                    if (material.textures[i] != null)
                        text = material.textures[i].toString();

                    ImString imString = new ImString(text, 41);
                    boolean newDescriptor = ImGui.inputText("tex " + i, imString, ImGuiInputTextFlags.EnterReturnsTrue);
                    if (newDescriptor) {
                        text = imString.get();

                        boolean isSHA1 = Strings.isSHA1(text);
                        boolean isGUID = Strings.isGUID(text);

                        if (isSHA1 || isGUID) {
                            material.textures[i] = new ResourceDescriptor(text, ResourceType.TEXTURE);
                            hasChanges |= true;
                        }
                    }
                }
            }


            ImGui.end();
        }

        if (hasChanges) 
            this.recompile();
    }

    private void update() {
        RenderSystem.initialize();
        this.recompile();

        while (!glfwWindowShouldClose(window)) {
            // this.input();

            // Camera camera = RenderSystem.getMainCamera();
            // camera.setAspectRatio(1280.0f / 720.0f);
            // camera.setAspectRatio(1024.0f / 1024.0f);
            // camera.recomputeProjectionMatrix();
            // camera.recomputeViewMatrix();


            RenderSystem.RENDER_TO_FRAMEBUFFER = false;
            RenderSystem.render(w, h);

            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            imGuiGLFW.newFrame();
            ImGui.newFrame();

            if (ImGui.beginMainMenuBar()) {

                if (ImGui.beginMenu("File")) {

    
                    ImGui.endMenu();
                }
    
                ImGui.endMainMenuBar();
            }

            int flags = ImGuiWindowFlags.NoDocking | ImGuiWindowFlags.NoTitleBar |
            ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
            ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus |
            ImGuiWindowFlags.NoBackground;

            ImGuiViewport viewport = ImGui.getMainViewport();
            ImGui.setNextWindowPos(viewport.getPosX(), viewport.getPosY());
            ImGui.setNextWindowSize(viewport.getSizeX(), viewport.getSizeY());
            ImGui.setNextWindowViewport(viewport.getID());

            ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f);

            if (ImGui.begin("InvisibleWindow", new ImBoolean(), flags)) {
                ImGui.popStyleVar(3);

                this.ui();


                ImGui.end();
            }

            // int dockSpaceId = ImGui.getID("InvisibleWindowDockSpace");
            // ImGui.dockSpace(dockSpaceId, 0.0f, 0.0f, ImGuiDockNodeFlags.PassthruCentralNode);


            ImGui.render();
            imguiGL3.renderDrawData(ImGui.getDrawData());

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public void recompile() {
        try {
            if (shader != null)
                shader.delete();
            shader = new Shader(RenderSystem.getVertexShader(), GfxAssembler.generateShaderSource(material, 0xDEADBEEF, false));
            shader.descriptor = WORLD_SHADER;
            shader.textures = material.textures;
            Shader.PROGRAMS.put(WORLD_SHADER, shader);
            // RenderSystem.OVERRIDE_SHADER = shader;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed to compile!");
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        Config.initialize();
        ResourceSystem.GUI_MODE = false;

        Profile profile = Config.instance.getCurrentProfile();
        if (profile == null) return;
        
        if (profile.archives != null) {
            for (String path : profile.archives) {
                if (Files.exists(Paths.get(path)))
                    ResourceSystem.getArchives().add(new FileArchive(new File(path)));
            }
        }
        
        if (profile.databases != null) {
            for (String path : profile.databases) {
                if (Files.exists(Paths.get(path)))
                    ResourceSystem.getDatabases().add(new FileDB(new File(path)));
            }
        }
        
        // byte[] data = FileIO.getResourceFile("/binary/shapes.sg");
        // RSceneGraph graph = new Resource(data).loadResource(RSceneGraph.class);
        // RenderSystem.setSceneGraph(graph);

        material = new Resource(ResourceSystem.extract(new GUID(1058971))).loadResource(RGfxMaterial.class);
        
        RSceneGraph graph = new RSceneGraph();
        RenderSystem.setSceneGraph(graph);
        sackboy = graph.addMesh(new ResourceDescriptor(1058975, ResourceType.MESH));

        // PCostume costume = new PCostume();
        // sackboy.setPart(Part.COSTUME, costume);
        // costume.material = WORLD_SHADER;

        // PRenderMesh mesh = sackboy.getPart(Part.RENDER_MESH);
        // mesh.anim = new ResourceDescriptor(4673, ResourceType.ANIMATION);
        

        Camera camera = RenderSystem.getMainCamera();

        Vector3f translation = camera.getTranslation();
        Vector3f rotation = camera.getEulerRotation();

        // translation.x = 0.0f;
        // translation.y = 150.0f;
        // translation.z = 225.0f;

        rotation.y = 0.0f;


        translation.x = 150.0f;
        translation.y = 200.0f;
        translation.z = 225.0f;
        rotation.y = -35.0f;

        camera.setTranslation(translation);
        camera.setEulerRotation(rotation);


        // RenderSystem.setSceneGraph(new RSceneGraph());


        new Editor().run();
    }
}
