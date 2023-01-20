package cwlib.io.exports;

import cwlib.util.FileIO;
import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Animation;
import de.javagl.jgltf.impl.v2.AnimationChannel;
import de.javagl.jgltf.impl.v2.AnimationChannelTarget;
import de.javagl.jgltf.impl.v2.AnimationSampler;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Image;
import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MaterialNormalTextureInfo;
import de.javagl.jgltf.impl.v2.MaterialOcclusionTextureInfo;
import de.javagl.jgltf.impl.v2.MaterialPbrMetallicRoughness;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.impl.v2.Skin;
import de.javagl.jgltf.impl.v2.TextureInfo;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.io.v2.GltfAssetWriterV2;
import cwlib.util.Bytes;
import cwlib.enums.BoxType;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.resources.RAnimation;
import cwlib.resources.RGfxMaterial;
import cwlib.resources.RMesh;
import cwlib.resources.RStaticMesh;
import cwlib.resources.RTexture;
import cwlib.resources.RAnimation.AnimationType;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.animation.AnimBone;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.gmat.MaterialWire;
import cwlib.structs.mesh.Bone;
import cwlib.structs.mesh.Morph;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.types.databases.FileEntry;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MeshExporter {
    public static class OBJ {
        public static void export(String path, RMesh mesh) { export(path, mesh, 0); }
        public static void export(String path, RMesh mesh, int channel) {
            int numVerts = mesh.getNumVerts();
            StringBuilder builder = new StringBuilder((numVerts * 82) + (numVerts * 42) + (mesh.getNumIndices() * 40));
            for (Vector3f vertex : mesh.getVertices())
                builder.append("v " + vertex.x + " " + vertex.y + " " + vertex.z + '\n');
            for (Vector3f vertex : mesh.getNormals())
                builder.append("vn " + vertex.x + " " + vertex.y + " " + vertex.z + '\n');
            for (Vector2f vertex : mesh.getUVs(channel))
                builder.append("vt " + vertex.x + " " + (1.0f - vertex.y) + '\n');
            int[] indices = mesh.getTriangles();
            // NOTE(Aidan): Wavefront OBJ has 1-based indices.
            for (int i = 0; i < indices.length; ++i)
                indices[i] += 1;
            for (int i = 0; i < indices.length; i += 3) {
                builder.append("f ");
                builder.append(indices[i] + "/" + indices[i] + "/" + indices[i] + " ");
                builder.append(indices[i + 1] + "/" + indices[i + 1] + "/" + indices[i + 1] + " ");
                builder.append(indices[i + 2] + "/" + indices[i + 2] + "/" + indices[i + 2] + '\n');
            }
            FileIO.write(builder.toString().getBytes(), path);
        }
    }
    
    public static class GLB {
        GlTF gltf = new GlTF();
        byte[] buffer = null;
        
        HashMap<String, Integer> bufferViews = new HashMap<String, Integer>();
        HashMap<String, Integer> materials = new HashMap<String, Integer>();
        HashMap<String, Integer> textures = new HashMap<String, Integer>();
        int accessorCount = 0;
        
        public static GLB FromAnimation(RAnimation animation, RMesh mesh) {
            GLB glb;
            
            if (mesh == null) {
                glb = new GLB();
                byte[] dataBuffer = glb.getBufferFromAnimation(animation);
                Buffer buffer = new Buffer();
                buffer.setByteLength(dataBuffer.length);
                glb.gltf.addBuffers(buffer);
                glb.buffer = dataBuffer;
                
                glb.setAsset("CRAFTWORLD", "2.0");


                Node root = new Node();
                root.setName("Armature");
                root.addChildren(1);
                root.setSkin(0);

                glb.gltf.addNodes(root);

                for (int i = 0; i < animation.getBoneCount(); ++i) {
                    Vector4f pos = animation.getBasePosition(i);
                    Vector4f rot = animation.getBaseRotation(i);
                    Vector4f scale = animation.getBaseScale(i);

                    Node child = new Node();

                    child.setTranslation(new float[] { pos.x, pos.y, pos.z });
                    child.setRotation(new float[] { rot.x, rot.y, rot.z, rot.w });
                    child.setScale(new float[] { scale.x, scale.y, scale.z });
                    
                    glb.gltf.addNodes(child);
                }

                Skin skin = new Skin();

                AnimBone[] bones = animation.getBones();
                for (int i = 0; i < bones.length; ++i) {
                    skin.addJoints(i + 1);
                    if (bones[i].parent != -1)
                        glb.gltf.getNodes().get(bones[i].parent + 1).addChildren(i + 1);
                }

                glb.gltf.addSkins(skin);
            } else {
                glb = GLB.FromMesh(mesh);
                byte[] dataBuffer = glb.getBufferFromAnimation(animation);
                glb.buffer = Bytes.combine(glb.buffer, dataBuffer);
                glb.gltf.getBuffers().get(0).setByteLength(glb.buffer.length);
                for (AnimBone bone : animation.bones) {
                    String name = Bone.getNameFromHash(mesh.getBones(), bone.animHash);
                    int boneIndex = animation.getBoneIndex(bone.animHash);

                    Node node = glb.getNode(name);

                    Vector4f pos = animation.getBasePosition(boneIndex);
                    Vector4f rot = animation.getBaseRotation(boneIndex);
                    Vector4f scale = animation.getBaseScale(boneIndex);

                    node.setTranslation(new float[] { pos.x, pos.y, pos.z });
                    node.setRotation(new float[] { rot.x, rot.y, rot.z, rot.w });
                    node.setScale(new float[] { scale.x, scale.y, scale.z });
                }
            }
            
            if (animation.getMorphCount() != 0)
                


                for (Node node : glb.gltf.getNodes()) {
                    float[] weights = animation.getBaseWeights();
                    List<Float> gltfWeights = new ArrayList<>();
                    for (float weight : weights) gltfWeights.add(weight);
                    if (node.getMesh() != null)
                        glb.gltf.getMeshes().get(node.getMesh()).setWeights(gltfWeights);
                }
            
            int time = glb.createAccessor("TIME", 5126, "SCALAR", 0, animation.getNumFrames());
            
            Animation glAnim = new Animation();
            
            int samplerIndex = 0;

            byte[] morphsAnimated = animation.getMorphsAnimated();
            if (morphsAnimated.length != 0) {
                for (int i = 0; i < glb.gltf.getNodes().size(); ++i) {
                    Node node = glb.gltf.getNodes().get(i);
                    if (node.getMesh() == null) continue;
                    AnimationChannel channel = new AnimationChannel();
                    AnimationChannelTarget target = new AnimationChannelTarget();
                    target.setNode(i);
                    target.setPath("weights");
                    channel.setTarget(target);
                    AnimationSampler sampler = new AnimationSampler();
                    sampler.setInput(time);
                    sampler.setInterpolation("LINEAR");
                    sampler.setOutput(glb.createAccessor("MORPHS_ANIMATED", 5126, "SCALAR", 0, animation.getMorphCount() * (animation.getNumFrames())));
                    channel.setSampler(samplerIndex);
                    samplerIndex++;
                    glAnim.addChannels(channel);
                    glAnim.addSamplers(sampler);
                }
            }
            
            AnimBone[] bones = animation.getBones();

            for (byte bone : animation.getPosAnimated()) {
                int pos = bone & 0xff;
                
                AnimationChannel channel = new AnimationChannel();
                AnimationChannelTarget target = new AnimationChannelTarget();
                if (mesh != null)
                    target.setNode(glb.getNodeIndex(Bone.getNameFromHash(mesh.getBones(), animation.bones[pos].animHash)));
                target.setNode(pos + 1);
                target.setPath("translation");
                channel.setTarget(target);
                AnimationSampler sampler = new AnimationSampler();
                sampler.setInput(time);
                sampler.setInterpolation("LINEAR");
                sampler.setOutput(glb.createAccessor("BONE_TRANSLATION_" + bones[pos].animHash, 5126, "VEC3", 0, animation.getNumFrames()));
                channel.setSampler(samplerIndex);
                samplerIndex++;
                glAnim.addChannels(channel);
                glAnim.addSamplers(sampler);
            }

            for (byte bone : animation.getScaleAnimated()) {
                int scale = bone & 0xff;
                
                AnimationChannel channel = new AnimationChannel();
                AnimationChannelTarget target = new AnimationChannelTarget();
                if (mesh != null)
                    target.setNode(glb.getNodeIndex(Bone.getNameFromHash(mesh.getBones(), animation.bones[scale].animHash)));
                target.setNode(scale + 1);
                target.setPath("scale");
                channel.setTarget(target);
                AnimationSampler sampler = new AnimationSampler();
                sampler.setInput(time);
                sampler.setInterpolation("LINEAR");
                sampler.setOutput(glb.createAccessor("BONE_SCALE_" + bones[scale].animHash, 5126, "VEC3", 0, animation.getNumFrames()));
                channel.setSampler(samplerIndex);
                samplerIndex++;
                glAnim.addChannels(channel);
                glAnim.addSamplers(sampler);
            }
            
            for (byte bone : animation.getRotAnimated()) {
                int rot = bone & 0xff;
                
                AnimationChannel channel = new AnimationChannel();
                AnimationChannelTarget target = new AnimationChannelTarget();
                if (mesh != null)
                    target.setNode(glb.getNodeIndex(Bone.getNameFromHash(mesh.getBones(), animation.bones[rot].animHash)));
                else target.setNode(rot + 1);
                target.setPath("rotation");
                channel.setTarget(target);
                AnimationSampler sampler = new AnimationSampler();
                sampler.setInput(time);
                sampler.setInterpolation("LINEAR");
                sampler.setOutput(glb.createAccessor("BONE_ROTATION_" + bones[rot].animHash, 5126, "VEC4", 0, animation.getNumFrames()));
                channel.setSampler(samplerIndex);
                samplerIndex++;
                glAnim.addChannels(channel);
                glAnim.addSamplers(sampler);
            }

            glb.gltf.addAnimations(glAnim);
            
            return glb;
        }
        
        public static GLB FromMesh(RStaticMesh mesh) {
            GLB glb = new GLB();
            byte[] dataBuffer = glb.getBufferFromMesh(mesh);
            Buffer buffer = new Buffer();
            buffer.setByteLength(dataBuffer.length);
            glb.gltf.addBuffers(buffer);
            glb.buffer = dataBuffer;
            glb.setAsset("CRAFTWORLD", "2.0");
            
            Mesh glMesh = new Mesh();
            for (int i = 0; i < mesh.getMeshInfo().primitives.length; ++i) {
                StaticPrimitive primitive = mesh.getMeshInfo().primitives[i];
                MeshPrimitive glPrimitive = new MeshPrimitive();
                glPrimitive.addAttributes("POSITION", 
                        glb.createAccessor(
                                "VERTICES", 
                                5126, 
                                "VEC3", 
                                primitive.vertexStart * 0xC, 
                                primitive.numVerts)
                );

                glPrimitive.addAttributes("NORMAL", 
                        glb.createAccessor(
                                "NORMALS", 
                                5126, 
                                "VEC3", 
                                primitive.vertexStart * 0xC, 
                                primitive.numVerts)
                );
                
                for (int j = 0; j < 2; ++j) {
                    glPrimitive.addAttributes("TEXCOORD_" + String.valueOf(j), 
                            glb.createAccessor(
                                    "TEXCOORD_" + String.valueOf(j), 
                                    5126, 
                                    "VEC2", 
                                    primitive.vertexStart * 0x8, 
                                    primitive.numVerts)
                    );
                }
                
                glPrimitive.setIndices(
                        glb.createAccessor(
                                "INDICES_" + i,
                                5123, 
                                "SCALAR", 
                                0, 
                                glb.gltf.getBufferViews().get(glb.getBufferView("INDICES_" + i)).getByteLength() / 2)
                );
                
                String materialName = "DIFFUSE";
                if (primitive.gmat != null) {
                    FileEntry entry = ResourceSystem.get(primitive.gmat);
                    if (entry != null) {
                        materialName = Paths.get(entry.getPath()).getFileName().toString().replaceFirst("[.][^.]+$", "");
                        try {
                            byte[] data = ResourceSystem.extract(entry);
                            if (data != null) 
                                glPrimitive.setMaterial(glb.createMaterial(materialName, new Resource(data).loadResource(RGfxMaterial.class)));   
                            else glPrimitive.setMaterial(glb.createMaterial(materialName));
                        } catch (Exception e) {
                            glPrimitive.setMaterial(glb.createMaterial(materialName));
                        }
                    }
                    else  {
                        materialName = primitive.gmat.toString();
                        glPrimitive.setMaterial(glb.createMaterial(materialName));
                    }
                }

                glPrimitive.setMode(4);

                glMesh.addPrimitives(glPrimitive);
            }
            
            glb.gltf.addMeshes(glMesh);
            
            Node root = new Node();
            root.setName("bg");
            root.setMesh(0);
            
            glb.gltf.addNodes(root);
            glb.gltf.setScene(0);
            
            Scene scene = new Scene();
            scene.setName("Scene");
            scene.addNodes(0);
            glb.gltf.addScenes(scene);
            
            return glb;
        }
       
        public static GLB FromMesh(RMesh mesh) {
            GLB glb = new GLB();
            
            
            byte[] dataBuffer = glb.getBufferFromMesh(mesh);
            Buffer buffer = new Buffer();
            buffer.setByteLength(dataBuffer.length);
            glb.gltf.addBuffers(buffer);
            glb.buffer = dataBuffer;
            
            glb.setAsset("CRAFTWORLD", "2.0");
           
            cwlib.structs.mesh.Primitive[][] subMeshes = mesh.getSubmeshes();
            for (int m = 0; m < subMeshes.length; ++m) {
                Mesh glMesh = new Mesh();
                if (mesh.getMorphCount() != 0) {
                    HashMap<String, Object> extras = new HashMap<String, Object>();
                    String[] morphs = new String[mesh.getMorphCount()];
                    for (int i = 0; i < morphs.length; ++i)
                        morphs[i] = mesh.getMorphNames()[i];
                    extras.put("targetNames", morphs);
                    glMesh.setExtras(extras);
                }
                for (int i = 0; i < subMeshes[m].length; ++i) {
                    cwlib.structs.mesh.Primitive primitive = subMeshes[m][i];
                    MeshPrimitive glPrimitive = new MeshPrimitive();
                    
                    glPrimitive.addAttributes("POSITION", 
                            glb.createAccessor(
                                    "VERTICES", 
                                    5126, 
                                    "VEC3", 
                                    primitive.getMinVert() * 0xC, 
                                    primitive.getMaxVert() - primitive.getMinVert() + 1)
                    );

                    glPrimitive.addAttributes("NORMAL", 
                            glb.createAccessor(
                                    "NORMAL", 
                                    5126, 
                                    "VEC3", 
                                    primitive.getMinVert() * 0xC, 
                                    primitive.getMaxVert() - primitive.getMinVert() + 1)
                    );

                    for (int j = 0; j < mesh.getAttributeCount(); ++j) {
                        glPrimitive.addAttributes("TEXCOORD_" + j, 
                                glb.createAccessor(
                                        "TEXCOORD_" + j, 
                                        5126, 
                                        "VEC2", 
                                        primitive.getMinVert() * 0x8, 
                                        primitive.getMaxVert() - primitive.getMinVert() + 1)
                        );
                    }

                    if (mesh.getMorphCount() != 0) {
                        for (int j = 0; j < mesh.getMorphCount(); ++j) {
                            HashMap<String, Integer> target = new HashMap<String, Integer>();
                            target.put("POSITION", glb.createAccessor(
                                    "MORPH_" + j,
                                    5126,
                                    "VEC3",
                                    primitive.getMinVert() * 0xC, 
                                    primitive.getMaxVert() - primitive.getMinVert() + 1)
                            );
                            target.put("NORMAL", glb.createAccessor(
                                    "MORPH_NORMAL_" + j,
                                    5126,
                                    "VEC3",
                                    primitive.getMinVert() * 0xC, 
                                    primitive.getMaxVert() - primitive.getMinVert() + 1)
                            );
                            glPrimitive.addTargets(target);
                        }
                    }

                    glPrimitive.addAttributes("JOINTS_0", 
                            glb.createAccessor(
                                    "JOINTS", 
                                    5121, 
                                    "VEC4", 
                                    primitive.getMinVert() * 0x4, 
                                    primitive.getMaxVert() - primitive.getMinVert() + 1
                            )
                    );

                   glPrimitive.addAttributes("WEIGHTS_0", 
                            glb.createAccessor(
                                    "WEIGHTS", 
                                    5126, 
                                    "VEC4", 
                                    primitive.getMinVert() * 0x10, 
                                    primitive.getMaxVert() - primitive.getMinVert() + 1
                            )
                    );

                    glPrimitive.addAttributes("COLOR_0", 
                            glb.createAccessor(
                                    "COLOR", 
                                    5123, 
                                    "VEC4", 
                                    primitive.getMinVert() * 0x8, 
                                    primitive.getMaxVert() - primitive.getMinVert() + 1
                            )
                    );

                    glPrimitive.setIndices(
                            glb.createAccessor(
                                    "INDICES_" + m + "_" + i, 
                                    5123, 
                                    "SCALAR", 
                                    0, 
                                    glb.gltf.getBufferViews().get(glb.getBufferView("INDICES_" + m + "_" + i)).getByteLength() / 2)
                    );

                    String materialName = "DIFFUSE";
                    if (primitive.getMaterial() != null) {
                        FileEntry entry = ResourceSystem.get(primitive.getMaterial());
                        if (entry != null) {
                            materialName = Paths.get(entry.getPath()).getFileName().toString().replaceFirst("[.][^.]+$", "");
                            try {
                                byte[] data = ResourceSystem.extract(entry);
                                if (data != null) 
                                    glPrimitive.setMaterial(glb.createMaterial(materialName, new Resource(data).loadResource(RGfxMaterial.class)));   
                                else glPrimitive.setMaterial(glb.createMaterial(materialName));
                            } catch (Exception e) {
                                glPrimitive.setMaterial(glb.createMaterial(materialName));
                            }
                        }
                        else  {
                            materialName = primitive.getMaterial().toString();
                            glPrimitive.setMaterial(glb.createMaterial(materialName));
                        }
                    }

                    glPrimitive.setMode(4);

                    glMesh.addPrimitives(glPrimitive);
                }
                
                glb.gltf.addMeshes(glMesh);
            }
            
            Node root = new Node();
            root.setName("Armature");
            
            glb.createSkeleton(mesh);
            Skin skin = new Skin();
            for (Bone bone : mesh.getBones()) {
                int index = glb.getNodeIndex(bone.getName());
                skin.addJoints(index);
                if (bone.parent == -1)
                    root.addChildren(index);
            }
            skin.setInverseBindMatrices(glb.createAccessor("MATRIX", 5126, "MAT4", 0, mesh.getBones().length));
            glb.gltf.addSkins(skin);
            
            for (int i = 0; i < subMeshes.length; ++i) {
                Node child = new Node();
                child.setMesh(i);
                child.setSkin(0);
                glb.gltf.addNodes(child);
                root.addChildren(glb.gltf.getNodes().size() - 1);
            }
            
            glb.gltf.addNodes(root);
            
            glb.gltf.setScene(0);
            
            Scene scene = new Scene();
            scene.setName("Scene");
            scene.addNodes(glb.gltf.getNodes().size() - 1);
            glb.gltf.addScenes(scene);

            return glb;
        }
        
        private int addTexture(String name, byte[] buffer) {
            if (getBufferView("TEXTURE_" + name) != -1) return this.textures.get(name);
            Image image = new Image();
            image.setBufferView(createBufferView("TEXTURE_" + name, 0, buffer.length));
            image.setMimeType("image/png");
            image.setName(name);
            this.buffer = Bytes.combine(this.buffer, buffer);
            this.gltf.getBuffers().get(0).setByteLength(this.buffer.length);
            de.javagl.jgltf.impl.v2.Texture texture = new de.javagl.jgltf.impl.v2.Texture();
            this.gltf.addImages(image);
            texture.setSource(this.gltf.getImages().size() - 1);
            texture.setName(name);
            this.gltf.addTextures(texture);
            int index = this.gltf.getTextures().size() - 1;
            this.textures.put(name, index);
            return index;
        }
        
        private void createSkeleton(RMesh mesh) {
            if (mesh.getBones().length == 0) return;
            for (Bone bone : mesh.getBones()) {
                if (bone.parent == -1)
                    createChildren(mesh, bone);
            }
        }
        
        private int createChildren(RMesh mesh, Bone bone) {
            Matrix4f transform = bone.getLocalTransform(mesh.getBones());
            int index = createNode(bone.getName(), transform);
            Node root = this.gltf.getNodes().get(index);
            
            Bone[] children = bone.getChildren(mesh.getBones());
            if (children.length == 0) return index;

            for (Bone child : children) {
                root.addChildren(createChildren(
                        mesh, 
                        child
                ));
            }
            return index;
        }
        
        private int createNode(String name, Matrix4f transform) {
            Node node = new Node();
            node.setName(name);
            
            Vector3f translation = transform.getTranslation(new Vector3f());
            node.setTranslation(new float[] { translation.x, translation.y, translation.z });
            
            Quaternionf rotation = new Quaternionf().setFromUnnormalized(transform);
            node.setRotation(new float[] { rotation.x, rotation.y, rotation.z, rotation.w });
            
            Vector3f scale = transform.getScale(new Vector3f());
            node.setScale(new float[] { scale.x, scale.y, scale.z });
            
            this.gltf.addNodes(node);
            return this.gltf.getNodes().size() - 1;
        }

        private Node getNode(String name) {
            List<Node> nodes = this.gltf.getNodes();
            for (Node node : nodes)
                if (node.getName().equals(name))
                    return node;
            return null;
        }
        
        private int getNodeIndex(String name) {
            List<Node> nodes = this.gltf.getNodes();
            for (int i = 0; i < nodes.size(); ++i)
                if (nodes.get(i).getName().equals(name))
                    return i;
            return -1;
        }

        public byte[] convertTexture(RGfxMaterial gfx, int index) {
            ResourceDescriptor descriptor = gfx.textures[index];
            if (descriptor == null) return null;
            byte[] data = ResourceSystem.extract(descriptor);
            if (data == null) return null;
            RTexture texture = null;
            try { 
                texture = new RTexture(data);
                try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                    ImageIO.write(texture.getImage(), "png", stream);
                    return stream.toByteArray();
                }
            } catch (Exception ex) { return null; }
        }
        
        private int createMaterial(String name, RGfxMaterial gmat) {
            if (this.materials.containsKey(name))
                return this.materials.get(name);
            
            Material material = new Material();
            material.setName(name);
            
            material.setDoubleSided(true);
            
            MaterialPbrMetallicRoughness pbr = new MaterialPbrMetallicRoughness();
            
            boolean foundDiffuse = false;
            boolean foundBump = false;
            int outputBox = gmat.getOutputBox();
            for (int i = 0; i < gmat.boxes.size(); ++i) {
                if (foundBump && foundDiffuse) break;
                MaterialBox box = gmat.boxes.get(i);
                /*
                
                DIFFUSE : 0,
                SPECULAR: 2,
                BUMP: 3,
                GLOW: 4,
                REFLECTION: 6
                */
                
                if (box.type == BoxType.TEXTURE_SAMPLE) {
                    int[] params = box.getParameters();
                    float[] textureScale = new float[] { Float.intBitsToFloat((int) params[0]), Float.intBitsToFloat((int) params[1]) };
                    float[] textureOffset = new float[] { Float.intBitsToFloat((int) params[2]), Float.intBitsToFloat((int) params[3]) };
                    int channel = (int) params[4];
                    int textureIndex = (int) params[5];
                    FileEntry entry = ResourceSystem.get(gmat.textures[textureIndex]);
                    if (entry == null) continue;
                    byte[] texture = this.convertTexture(gmat, textureIndex);
                    if (texture == null) continue;
                    int source = addTexture(Paths.get(entry.getPath()).getFileName().toString().replaceFirst("[.][^.]+$", ""), texture);
                    
                    ByteArrayInputStream png = new ByteArrayInputStream(texture);
                    BufferedImage image = null;
                    try {
                        image = ImageIO.read(png);
                    } catch (IOException ex) {
                        Logger.getLogger(MeshExporter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    HashMap<String, float[]> transforms = new HashMap<String, float[]>();
                    transforms.put("offset", textureOffset);
                    transforms.put("scale", textureScale);
                    
                    MaterialWire wire = gmat.findWireFrom(i);
                    while (wire.boxTo != outputBox)
                        wire = gmat.findWireFrom(wire.boxTo);
                    
                    TextureInfo textureInfo = new TextureInfo();
                    textureInfo.addExtensions("KHR_texture_transform", transforms);
                    textureInfo.setTexCoord(channel);
                    textureInfo.setIndex(source);
                    switch (wire.portTo) {
                        case 0:                     
                            if (entry.getPath().contains("dirt")) {
                                MaterialOcclusionTextureInfo occInfo = new MaterialOcclusionTextureInfo();
                                occInfo.addExtensions("KHR_texture_transform", transforms);
                                occInfo.setIndex(source);
                                occInfo.setTexCoord(channel);
                                material.setOcclusionTexture(occInfo);
                                continue;
                            }
                            if (foundDiffuse) continue;
                            System.out.println(String.format("%s:%d", Paths.get(entry.getPath()).getFileName().toString().replaceFirst("[.][^.]+$", ""), image.getTransparency()));
                            if (name.toLowerCase().contains("decal"))
                                material.setAlphaMode("BLEND");
                            foundDiffuse = true;
                            pbr.setBaseColorTexture(textureInfo);
                            if (material.getExtensions() != null && material.getExtensions().containsKey("KHR_materials_pbrSpecularGlossiness")) {
                                @SuppressWarnings("unchecked")
                                HashMap<String, Object> map = (HashMap<String, Object>) material.getExtensions().get("KHR_materials_pbrSpecularGlossiness");  
                                if (!map.containsKey("diffuseTexture"))
                                    map.put("diffuseTexture", textureInfo);
                            } 
                            continue;
                        case 2:
                            /*
                            HashMap<String, TextureInfo> extension = new HashMap<String, TextureInfo>();
                            extension.put("specularGlossinessTexture", textureInfo);
                            material.addExtensions("KHR_materials_pbrSpecularGlossiness", extension);
                            */
                            continue;
                        case 3:
                            if (foundBump) continue;
                            foundBump = true;
                            
                            if (image != null) {
                                for (int x = 0; x < image.getWidth(); ++x) {
                                    for (int y = 0; y < image.getHeight(); ++y) {
                                        Color c = new Color(image.getRGB(x, y), true);
                                        
                                        Color output = new Color(255 - c.getAlpha(), c.getGreen(), 255, 255);
                                        
                                        image.setRGB(x, y, output.getRGB());
                                    }
                                }
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                try {
                                    ImageIO.write(image, "png", baos);
                                } catch (IOException ex) {
                                    Logger.getLogger(RGfxMaterial.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                
                                source = addTexture(Paths.get(entry.getPath()).getFileName().toString().replaceFirst("[.][^.]+$", "") + "_converted", baos.toByteArray());
                            }
                            
                            MaterialNormalTextureInfo normal = new MaterialNormalTextureInfo();
                            normal.addExtensions("KHR_texture_transform", transforms);
                            normal.setIndex(source);
                            normal.setTexCoord(channel);
                            material.setNormalTexture(normal);
                            continue;

                    }
                } else if (box.type == BoxType.COLOR) {
                    MaterialWire wire = gmat.findWireFrom(i);
                    if (wire.boxTo == outputBox) {
                        int[] params = box.getParameters();
                        float[] color = new float[] { Float.intBitsToFloat((int) params[0]) / 255f, Float.intBitsToFloat((int) params[1]) / 255f, Float.intBitsToFloat((int) params[2]) / 255f, Float.intBitsToFloat((int) params[3]) / 255f};
                        if (wire.portTo == 0) {
                            if (material.getExtensions() != null && material.getExtensions().containsKey("KHR_materials_pbrSpecularGlossiness")) {
                                @SuppressWarnings("unchecked")
                                HashMap<String, Object> map = (HashMap<String, Object>) material.getExtensions().get("KHR_materials_pbrSpecularGlossiness");  
                                if (!map.containsKey("diffuseFactor"))
                                    map.put("diffuseFactor", color);
                            } 
                            foundDiffuse = true;
                            pbr.setBaseColorFactor(color);
                        } else if (wire.portTo == 2) {
                            HashMap<String, Object> extension = new HashMap<String, Object>();
                            extension.put("specularFactor", new float[] { color[0], color[1], color[2] });
                            if (pbr.getBaseColorFactor() != null)
                                extension.put("diffuseFactor", pbr.getBaseColorFactor());
                            if (pbr.getBaseColorTexture() != null)
                                extension.put("diffuseTexture", pbr.getBaseColorTexture());
                            material.addExtensions("KHR_materials_pbrSpecularGlossiness", extension);
                        }
                    }
                }
            }
            
            if (!foundDiffuse)
                pbr.setBaseColorFactor(new float[] { 1.0f, 1.0f, 1.0f, 1.0f });
            
            material.setPbrMetallicRoughness(pbr);
            
            this.gltf.addMaterials(material);
            int index = this.materials.size();
            this.materials.put(name, index);
            return index;
        }
        
        private int createMaterial(String name) {
            if (this.materials.containsKey(name))
                return this.materials.get(name);
            
            Material material = new Material();
            material.setName(name);
            MaterialPbrMetallicRoughness pbr = new MaterialPbrMetallicRoughness();
            pbr.setBaseColorFactor(new float[] { 1.0f, 1.0f, 1.0f, 1.0f });
            material.setPbrMetallicRoughness(pbr);
            
            this.gltf.addMaterials(material);
            int index = this.materials.size();
            this.materials.put(name, index);
            return index; 
        }
        
        private void setAsset(String generator, String version) {
            Asset asset = new Asset();
            asset.setGenerator(generator);
            asset.setVersion(version);
            this.gltf.setAsset(asset);
        }
        
        private int createAccessor(String bufferView, int componentType, String type, int offset, int count) {
            Accessor accessor = new Accessor();
            accessor.setBufferView(getBufferView(bufferView));
            accessor.setByteOffset(offset);
            accessor.setComponentType(componentType);
            accessor.setType(type);
            accessor.setCount(count);
            this.accessorCount++;
            this.gltf.addAccessors(accessor);
            return this.accessorCount - 1;
        }
        
        private int createBufferView(String name, int offset, int length) {
            if (this.buffer != null)
                offset += this.buffer.length;
            BufferView view = new BufferView();
            view.setBuffer(0);
            view.setByteOffset(offset);
            view.setByteLength(length);
            this.gltf.addBufferViews(view);
            int index = this.bufferViews.size();
            this.bufferViews.put(name, index);
            return index;
        }
        
        public int getBufferView(String name) {
            if (this.bufferViews.containsKey(name))
                return this.bufferViews.get(name);
            return -1;
        }
        
        private byte[] getBufferFromAnimation(RAnimation animation) {
            float timestep = 1.0f / ((float) animation.getFPS());
            
            MemoryOutputStream output = new MemoryOutputStream(animation.getNumFrames() * animation.getFPS() * animation.getBoneCount() + (animation.getPosAnimated().length + animation.getRotAnimated().length + animation.getScaleAnimated().length) * 0x50 + 0xFF0);
            output.setLittleEndian(true);

            float step = 0.0f;
            for (int i = 0; i < animation.getNumFrames(); ++i, step += timestep)
                output.f32(step);
            createBufferView("TIME", 0, output.getOffset());
            AnimBone[] bones = animation.getBones();
            for (int i = 0; i < bones.length; ++i) {
                AnimBone bone = bones[i];

                if (animation.isAnimated(bone, AnimationType.POSITION)) {
                    int posStart = output.getOffset();
                    for (Vector4f pos : animation.getPositionFrames(bone)) {
                        output.f32(pos.x);
                        output.f32(pos.y);
                        output.f32(pos.z);
                    }
                    createBufferView("BONE_TRANSLATION_" + String.valueOf(bone.animHash), posStart, output.getOffset() - posStart);
                }
                
                if (animation.isAnimated(bone, AnimationType.ROTATION)) {
                    int rotStart = output.getOffset();
                    for (Vector4f rot : animation.getRotationFrames(bone)) {
                        output.f32(rot.x);
                        output.f32(rot.y);
                        output.f32(rot.z);
                        output.f32(rot.w);
                    }
                    createBufferView("BONE_ROTATION_" + String.valueOf(bone.animHash), rotStart, output.getOffset() - rotStart);
                }
                
                if (animation.isAnimated(bone, AnimationType.SCALE)) {
                    int scaleStart = output.getOffset();
                    for (Vector4f scale : animation.getScaleFrames(bone)) {
                        output.f32(scale.x);
                        output.f32(scale.y);
                        output.f32(scale.z);
                    }
                    createBufferView("BONE_SCALE_" + String.valueOf(bone.animHash), scaleStart, output.getOffset() - scaleStart);
                }
            }

            if (animation.getMorphsAnimated().length != 0) {
                float[][] morphFrames = new float[animation.getMorphCount()][];
                for (int i = 0; i < animation.getMorphCount(); ++i) {
                    if (animation.isAnimated(i)) morphFrames[i] = animation.getMorphFrames(i);
                    else {
                        float[] weights = new float[animation.getNumFrames()];
                        float base = animation.getBaseWeight(i);
                        for (int j = 0; j < weights.length; ++j)
                            weights[j] = base;
                    }
                }

                int morphStart = output.getOffset();
                for (int i = 0; i < animation.getNumFrames(); ++i) {
                    for (int j = 0; j < animation.getMorphCount(); ++j)
                        output.f32(morphFrames[j][i]);
                }
                createBufferView("MORPHS_ANIMATED",  morphStart, output.getOffset() - morphStart);
            }
            
            output.shrink();
            return output.getBuffer();
            
        }
        
        private byte[] getBufferFromMesh(RStaticMesh mesh) {
            MemoryOutputStream output = new MemoryOutputStream(mesh.getNumVerts() * 0x80 + ((mesh.getNumVerts() - 1) * 0x8));
            output.setLittleEndian(true);

            for (Vector3f vertex : mesh.getVertices()) {
                output.f32(vertex.x);
                output.f32(vertex.y);
                output.f32(vertex.z);
            }
            createBufferView("VERTICES", 0, output.getOffset());
            int normalStart = output.getOffset();
            for (Vector3f normal : mesh.getNormals()) {
                output.f32(normal.x);
                output.f32(normal.y);
                output.f32(normal.z);
            }
            createBufferView("NORMALS", normalStart, output.getOffset() - normalStart);
            int uvStart = output.getOffset();
            for (Vector2f uv : mesh.getUV0()) {
                output.f32(uv.x);
                output.f32(uv.y);
            }
            createBufferView("TEXCOORD_0", uvStart, output.getOffset() - uvStart);
            uvStart = output.getOffset();
            for (Vector2f uv : mesh.getUV1()) {
                output.f32(uv.x);
                output.f32(uv.y);
            }
            createBufferView("TEXCOORD_1", uvStart, output.getOffset() - uvStart);       
            StaticPrimitive[] primitives = mesh.getMeshInfo().primitives;
            for (int i = 0; i < primitives.length; ++i) {
                StaticPrimitive primitive = primitives[i];
                int primitiveStart = output.getOffset();
                int[] triangles = mesh.getTriangles(primitive.indexStart, primitive.numIndices, primitive.type);
                primitive.numVerts = getMax(triangles) + 1;
                for (int triangle : triangles)
                    output.u16((short) triangle);
                createBufferView("INDICES_" + String.valueOf(i), primitiveStart, output.getOffset() - primitiveStart);
            }
            output.shrink();
            return output.getBuffer();
        }
        
        private byte[] getBufferFromMesh(RMesh mesh) {
            MemoryOutputStream output = new MemoryOutputStream( (mesh.getNumVerts() * 0x50) + ((mesh.getNumVerts() - 1) * 8) + (mesh.getAttributeCount() * mesh.getNumVerts() * 8) + (mesh.getMorphCount() * mesh.getNumVerts() * 0x18) + (mesh.getBones().length * 0x40));
            output.setLittleEndian(true);

            for (Vector3f vertex : mesh.getVertices()) {
                output.f32(vertex.x);
                output.f32(vertex.y);
                output.f32(vertex.z);
            }
            createBufferView("VERTICES", 0, output.getOffset());
            
            cwlib.structs.mesh.Primitive[][] subMeshes = mesh.getSubmeshes();
            for (int i = 0; i < subMeshes.length; ++i) {
                for (int j = 0; j < subMeshes[i].length; ++j) {
                    int triangleStart = output.getOffset();
                    cwlib.structs.mesh.Primitive primitive
                            = subMeshes[i][j];
                    int[] triangles = mesh.getTriangles(primitive);

                    primitive.setMinMax(
                        getMin(triangles), 
                        getMax(triangles)
                    );

                    for (int triangle : triangles)
                        output.u16((short) (triangle - primitive.getMinVert()));
                    createBufferView("INDICES_" + String.valueOf(i) + "_" + String.valueOf(j), triangleStart, output.getOffset() - triangleStart);
                }
            }
            
            int normalStart = output.getOffset();
            Vector3f[] normals = mesh.getNormals();
            for (Vector3f normal : normals) {
                output.f32(normal.x);
                output.f32(normal.y);
                output.f32(normal.z);
            }
            createBufferView("NORMAL", normalStart, output.getOffset() - normalStart);
            for (int i = 0; i < mesh.getAttributeCount(); ++i) {
                int uvStart = output.getOffset();
                for (Vector2f texCoord : mesh.getUVs(i)) {
                    output.f32(texCoord.x);
                    output.f32(texCoord.y);
                }
                createBufferView("TEXCOORD_" + String.valueOf(i), uvStart, output.getOffset() - uvStart);
            }
            if (mesh.getMorphCount() != 0) {
                Morph[] morphs = mesh.getMorphs();
                for (int i = 0; i < mesh.getMorphCount(); ++i) {
                    int morphStart = output.getOffset();
                    Morph morph = morphs[i];
                    for (Vector3f vertex : morph.getOffsets()) {
                        output.f32(vertex.x);
                        output.f32(vertex.y);
                        output.f32(vertex.z);
                    }
                    createBufferView("MORPH_" + String.valueOf(i), morphStart, output.getOffset() - morphStart);
                }
                
                for (int i = 0; i < mesh.getMorphCount(); ++i) {
                    int morphStart = output.getOffset();
                    Morph morph = morphs[i];
                    for (int j = 0; j < mesh.getNumVerts(); ++j) {
                        Vector3f vertex = morph.getNormals()[j];
                        output.f32(vertex.x - normals[j].x);
                        output.f32(vertex.y - normals[j].y);
                        output.f32(vertex.z - normals[j].z);
                    }
                    
                    createBufferView("MORPH_NORMAL_" + String.valueOf(i), morphStart, output.getOffset() - morphStart);
                }
            }

            if (output.getOffset() % 0x40 != 0)
                output.seek((0x40 - (output.getOffset() % 0x40)));
            int matrixStart = output.getOffset();
            for (int i = 0; i < mesh.getBones().length; ++i) {
                for (float v : mesh.getBones()[i].invSkinPoseMatrix.get(new float[16]))
                    output.f32(v);
            }
            createBufferView("MATRIX", matrixStart, output.getOffset() - matrixStart);

            int jointStart = output.getOffset();
            for (byte[] joints : mesh.getJoints())
                for (int i = 0; i < 4; ++i)
                    output.i8(joints[i]);
            
            createBufferView("JOINTS", jointStart, output.getOffset() - jointStart);

            int weightStart = output.getOffset();
            for (Vector4f weight : mesh.getWeights()) {
                output.f32(weight.x);
                output.f32(weight.y);
                output.f32(weight.z);
                output.f32(weight.w);
            }
            
            createBufferView("WEIGHTS", weightStart, output.getOffset() - weightStart);

            int colorStart = output.getOffset();
            for (float weight : mesh.getSoftbodyWeights(0, mesh.getNumVerts())) {
                short c = (short) Math.round(weight * 0xFFFF);
                output.i16(c);
                output.i16(c);
                output.i16(c);
                output.i16((short) 0xFFFF);
            }
            createBufferView("COLOR", colorStart, output.getOffset() - colorStart);
            
            output.shrink();
            
            return output.getBuffer();
        }
        
        public static int getMin(int[] triangles) {
            int minValue = triangles[0];
            for (int i = 1; i < triangles.length; ++i)
                if (triangles[i] < minValue)
                    minValue = triangles[i];
            return minValue;
        }
        
        public static int getMax(int[] triangles) {
            int maxValue = triangles[0];
            for (int i = 1; i < triangles.length; ++i)
                if (triangles[i] > maxValue)
                    maxValue = triangles[i];
            return maxValue;
        }
        
        public void export(String path) {
            
            ByteBuffer buffer = null;
            if (this.buffer != null)
                buffer = ByteBuffer.wrap(this.buffer);
            
            GltfAssetV2 gltfAssetV2 = new GltfAssetV2(this.gltf, buffer);
            GltfAssetWriterV2 writer = new GltfAssetWriterV2();
            
            try {
                FileOutputStream stream = new FileOutputStream(path);
                writer.writeBinary(gltfAssetV2, stream);
                stream.close();  
            } catch (IOException ex) {
                Logger.getLogger(MeshExporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}