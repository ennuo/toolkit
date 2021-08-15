package ennuo.craftworld.resources.io;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Image;
import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MaterialPbrMetallicRoughness;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.impl.v2.Skin;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.io.v2.GltfAssetWriterV2;
import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.structs.mesh.Bone;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MeshIO {
    public static class OBJ {
        public static void export(String path, ennuo.craftworld.resources.Mesh mesh) { export(path, mesh, 0); }
        public static void export(String path, ennuo.craftworld.resources.Mesh mesh, int channel) {
            StringBuilder builder = new StringBuilder((mesh.vertices.length * 82) + (mesh.uvCount * 42) + (mesh.faces.length * 40));
            for (int j = 0; j < mesh.vertices.length; ++j)
                builder.append("v " + mesh.vertices[j].x + " " + mesh.vertices[j].y + " " + mesh.vertices[j].z + '\n');
            for (int j = 0; j < mesh.vertices.length; ++j)
                builder.append("vn " + mesh.weights[j].normal.x + " " + mesh.weights[j].normal.y + " " + mesh.weights[j].normal.z + '\n');
            for (int i = 0; i < mesh.uvCount; ++i)
                builder.append("vt " + mesh.attributes[i][channel].x + " " + (1.0f - mesh.attributes[i][channel].y) + '\n');
            for (int i = -1, j = 1; i < mesh.faces.length; ++i, ++j) {
                if (i == -1 || mesh.faces[i] == -1) {
                    String str = "f ";
                    str += (mesh.faces[i + 1] + 1) + "/" + (mesh.faces[i + 1] + 1) + "/" + (mesh.faces[i + 1] + 1) + " ";
                    str += (mesh.faces[i + 2] + 1) + "/" + (mesh.faces[i + 2] + 1) + "/" + (mesh.faces[i + 2] + 1) + " ";
                    str += (mesh.faces[i + 3] + 1) + "/" + (mesh.faces[i + 3] + 1) + "/" + (mesh.faces[i + 3] + 1) + '\n';

                    builder.append(str);
                    i += 3;
                    j = 0;
                } else {
                    if ((j & 1) == 1) {
                        String str = "f ";
                        str += (mesh.faces[i - 2] + 1) + "/" + (mesh.faces[i - 2] + 1) + "/" + (mesh.faces[i - 2] + 1) + " ";
                        str += (mesh.faces[i] + 1) + "/" + (mesh.faces[i] + 1) + "/" + (mesh.faces[i] + 1) + " ";
                        str += (mesh.faces[i - 1] + 1) + "/" + (mesh.faces[i - 1] + 1) + "/" + (mesh.faces[i - 1] + 1) + '\n';
                        builder.append(str);
                    } else {
                        String str = "f ";
                        str += (mesh.faces[i - 2] + 1) + "/" + (mesh.faces[i - 2] + 1) + "/" + (mesh.faces[i - 2] + 1) + " ";
                        str += (mesh.faces[i - 1] + 1) + "/" + (mesh.faces[i - 1] + 1) + "/" + (mesh.faces[i - 1] + 1) + " ";
                        str += (mesh.faces[i] + 1) + "/" + (mesh.faces[i] + 1) + "/" + (mesh.faces[i] + 1) + '\n';
                        builder.append(str);
                    }
                }
            }
            FileIO.write(builder.toString().getBytes(), path);
        }
    }
    
    public static class GLB {
        GlTF gltf = new GlTF();
        byte[] buffer = null;
        
        HashMap<String, Integer> bufferViews = new HashMap<String, Integer>();
        HashMap<String, Integer> materials = new HashMap<String, Integer>();
        int accessorCount = 0;
       
        public static GLB FromMesh(ennuo.craftworld.resources.Mesh mesh) {
            GLB glb = new GLB();
            glb.setBufferFromMesh(mesh);
            glb.setAsset("CRAFTWORLD", "2.0");
           
            ennuo.craftworld.resources.structs.mesh.MeshPrimitive[][] subMeshes = mesh.getSubmeshes();
            for (int m = 0; m < subMeshes.length; ++m) {
                Mesh glMesh = new Mesh();
                if (mesh.morphs != null && mesh.morphs.length != 0) {
                    HashMap<String, Object> extras = new HashMap<String, Object>();
                    String[] morphs = new String[mesh.morphs.length];
                    for (int i = 0; i < morphs.length; ++i)
                        morphs[i] = mesh.morphNames[i];
                    extras.put("targetNames", morphs);
                    glMesh.setExtras(extras);
                }
                for (int i = 0; i < subMeshes[m].length; ++i) {
                    ennuo.craftworld.resources.structs.mesh.MeshPrimitive primitive = subMeshes[m][i];
                    MeshPrimitive glPrimitive = new MeshPrimitive();
                    
                    glPrimitive.addAttributes("POSITION", 
                            glb.createAccessor(
                                    "VERTICES", 
                                    5126, 
                                    "VEC3", 
                                    primitive.minVert * 0xC, 
                                    primitive.maxVert - primitive.minVert + 1)
                    );

                    glPrimitive.addAttributes("NORMAL", 
                            glb.createAccessor(
                                    "NORMAL", 
                                    5126, 
                                    "VEC3", 
                                    primitive.minVert * 0xC, 
                                    primitive.maxVert - primitive.minVert + 1)
                    );

                    for (int j = 0; j < mesh.attributeCount; ++j) {
                        glPrimitive.addAttributes("TEXCOORD_" + j, 
                                glb.createAccessor(
                                        "TEXCOORD_" + j, 
                                        5126, 
                                        "VEC2", 
                                        primitive.minVert * 0x8, 
                                        primitive.maxVert - primitive.minVert + 1)
                        );
                    }

                    if (mesh.morphs != null) {
                        for (int j = 0; j < mesh.morphs.length; ++j) {
                            HashMap<String, Integer> target = new HashMap<String, Integer>();
                            target.put("POSITION", glb.createAccessor(
                                    "MORPH_" + j,
                                    5126,
                                    "VEC3",
                                    primitive.minVert * 0xC, 
                                    primitive.maxVert - primitive.minVert + 1)
                            );
                            target.put("NORMAL", glb.createAccessor(
                                    "MORPH_NORMAL_" + j,
                                    5126,
                                    "VEC3",
                                    primitive.minVert * 0xC, 
                                    primitive.maxVert - primitive.minVert + 1)
                            );
                            glPrimitive.addTargets(target);
                        }
                    }

                    glPrimitive.addAttributes("JOINTS_0", 
                            glb.createAccessor(
                                    "JOINTS", 
                                    5121, 
                                    "VEC4", 
                                    primitive.minVert * 0x4, 
                                    primitive.maxVert - primitive.minVert + 1
                            )
                    );

                   glPrimitive.addAttributes("WEIGHTS_0", 
                            glb.createAccessor(
                                    "WEIGHTS", 
                                    5126, 
                                    "VEC4", 
                                    primitive.minVert * 0x10, 
                                    primitive.maxVert - primitive.minVert + 1
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
                    if (primitive.material != null) {
                        FileEntry entry = Globals.findEntry(primitive.material);
                        if (entry != null) {
                            materialName = Paths.get(entry.path).getFileName().toString().replaceFirst("[.][^.]+$", "");
                            /*
                            byte[] data = Globals.extractFile(entry.hash);
                            if (data != null) {
                                Resource gfxMaterial = new Resource(data);
                                gfxMaterial.getDependencies(entry);
                                for (FileEntry dependency : gfxMaterial.dependencies) {
                                    byte[] texData = Globals.extractFile(dependency.hash);
                                    if (texData == null) continue;
                                    Texture texture = new Texture(texData);
                                    if (texture.parsed) {
                                        BufferedImage image = texture.getImage();
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        try {
                                            ImageIO.write(image, "png", baos);
                                            glb.addImage(Paths.get(dependency.path).getFileName().toString().replaceFirst("[.][^.]+$", ""), baos.toByteArray());  
                                        } catch (IOException ex) {
                                            Logger.getLogger(MeshIO.class.getName()).log(Level.SEVERE, null, ex);
                                        } 
                                    }
                                }
                            }
*/
                        }
                        else 
                            materialName = primitive.material.toString();
                    }

                    glPrimitive.setMaterial(glb.createMaterial(materialName));
                    glPrimitive.setMode(4);

                    glMesh.addPrimitives(glPrimitive);
                }
                
                glb.gltf.addMeshes(glMesh);
            }
            
            Node root = new Node();
            root.setName(mesh.name);
            
            glb.createSkeleton(mesh);
            Skin skin = new Skin();
            for (Bone bone : mesh.bones) {
                int index = glb.getNodeIndex(bone.name);
                skin.addJoints(index);
                if (bone.parent == -1 || mesh.bones[bone.parent] == bone)
                    root.addChildren(index);
            }
            skin.setInverseBindMatrices(glb.createAccessor("MATRIX", 5126, "MAT4", 0, mesh.bones.length));
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
        
        private void addImage(String name, byte[] buffer) {
            if (getBufferView("TEXTURE_" + name) != -1) return;
            Image image = new Image();
            image.setBufferView(createBufferView("TEXTURE_" + name, this.buffer.length, buffer.length));
            image.setMimeType("image/png");
            image.setName(name);
            this.buffer = Bytes.Combine(this.buffer, buffer);
            this.gltf.getBuffers().get(0).setByteLength(this.buffer.length);
            de.javagl.jgltf.impl.v2.Texture texture = new de.javagl.jgltf.impl.v2.Texture();
            this.gltf.addImages(image);
            texture.setSource(this.gltf.getImages().size() - 1);
            texture.setName(name);
            this.gltf.addTextures(texture);
        }
        
        private void createSkeleton(ennuo.craftworld.resources.Mesh mesh) {
            if (mesh.bones.length == 0) return;
            for (Bone bone : mesh.bones) {
                if (bone.parent == -1 || mesh.bones[bone.parent] == bone)
                    createChildren(mesh, bone);
            }
        }
        
        private Matrix4f getMatrix(float[] f) {
            return new Matrix4f(f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], f[8], f[9], f[10], f[11], f[12], f[13], f[14], f[15]);
        }
        
        private int createChildren(ennuo.craftworld.resources.Mesh mesh, Bone bone) {
            Matrix4f transform;
            if (bone.parent == -1 || mesh.bones[bone.parent] == bone)
                transform = getMatrix(bone.skinPoseMatrix);
            else {
                transform = getMatrix(mesh.bones[bone.parent].skinPoseMatrix)
                        .invert()
                        .mul(getMatrix(bone.skinPoseMatrix));
            }
            
            int index = createNode(bone.name, transform);
            Node root = this.gltf.getNodes().get(index);
            Bone[] children = mesh.getBoneChildren(bone);
            if (children.length == 0) return index;
            for (Bone child : children)
                root.addChildren(createChildren(
                        mesh, 
                        child
                ));
            return index;
        }
        
        private int createNode(String name, Matrix4f transform) {
            Node node = new Node();
            node.setName(name);
            
            Vector3f translation = new Vector3f();
            transform.getTranslation(translation);
            node.setTranslation(new float[] { translation.x, translation.y, translation.z });
            
            Quaternionf rotation = new Quaternionf().setFromUnnormalized(transform);
            
            node.setRotation(new float[] { rotation.x, rotation.y, rotation.z, rotation.w });
            
            Vector3f scale = new Vector3f();
            transform.getScale(scale);
            node.setScale(new float[] { scale.x, scale.y, scale.z });
            
            this.gltf.addNodes(node);
            return this.gltf.getNodes().size() - 1;
        }
        
        private Node getNode(int index) {
            List<Node> nodes = this.gltf.getNodes();
            if (nodes.size() >= index) return null;
            return nodes.get(index);
        }
        
        private int getNodeIndex(String name) {
            List<Node> nodes = this.gltf.getNodes();
            for (int i = 0; i < nodes.size(); ++i)
                if (nodes.get(i).getName().equals(name))
                    return i;
            return -1;
        }
        
        private Node getNode(String name) {
            List<Node> nodes = this.gltf.getNodes();
            for (Node node : nodes)
                if (node.getName().equals(name))
                    return node;
            return null;
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
        
        private void setBufferFromMesh(ennuo.craftworld.resources.Mesh mesh) {
            int morphSize = (mesh.morphs == null) ? 0 : mesh.morphs.length;
            Output output = new Output( (mesh.vertices.length * 0x40) + (mesh.triangulate().length * 8) + (mesh.attributeCount * mesh.uvCount * 8) + (morphSize * mesh.vertices.length * 0x18) + (mesh.bones.length * 0x40));
            for (Vector3f vertex : mesh.vertices) {
                output.float32le(vertex.x);
                output.float32le(vertex.y);
                output.float32le(vertex.z);
            }
            createBufferView("VERTICES", 0, output.offset);
            
            ennuo.craftworld.resources.structs.mesh.MeshPrimitive[][] subMeshes = mesh.getSubmeshes();
            for (int i = 0; i < subMeshes.length; ++i) {
                for (int j = 0; j < subMeshes[i].length; ++j) {
                    int triangleStart = output.offset;
                    ennuo.craftworld.resources.structs.mesh.MeshPrimitive primitive
                            = subMeshes[i][j];
                    short[] triangles = mesh.triangulate(primitive);
                    primitive.minVert = getMin(triangles);
                    primitive.maxVert = getMax(triangles);
                    for (short triangle : triangles)
                        output.int16le((short) (triangle - primitive.minVert));
                    createBufferView("INDICES_" + String.valueOf(i) + "_" + String.valueOf(j), triangleStart, output.offset - triangleStart);
                }
            }
            
            int normalStart = output.offset;
            for (int i = 0; i < mesh.weights.length; ++i) {
                output.float32le(mesh.weights[i].normal.x);
                output.float32le(mesh.weights[i].normal.y);
                output.float32le(mesh.weights[i].normal.z);
            }
            createBufferView("NORMAL", normalStart, output.offset - normalStart);
            for (int i = 0; i < mesh.attributeCount; ++i) {
                int uvStart = output.offset;
                for (int j = 0; j < mesh.uvCount; ++j) {
                    output.float32le(mesh.attributes[j][i].x);
                    output.float32le(mesh.attributes[j][i].y);
                }
                createBufferView("TEXCOORD_" + String.valueOf(i), uvStart, output.offset - uvStart);
            }
            if (mesh.morphs != null) {
                for (int i = 0; i < mesh.morphs.length; ++i) {
                    int morphStart = output.offset;
                    for (int j = 0; j < mesh.vertices.length; ++j) {
                        output.float32le(mesh.morphs[i].vertices[j].x);
                        output.float32le(mesh.morphs[i].vertices[j].y);
                        output.float32le(mesh.morphs[i].vertices[j].z);
                    }
                    createBufferView("MORPH_" + String.valueOf(i), morphStart, output.offset - morphStart);
                }
                
                for (int i = 0; i < mesh.morphs.length; ++i) {
                    int morphStart = output.offset;
                    for (int j = 0; j < mesh.vertices.length; ++j) {
                        output.float32le(mesh.morphs[i].normals[j].x  - mesh.weights[j].normal.x);
                        output.float32le(mesh.morphs[i].normals[j].y  - mesh.weights[j].normal.y);
                        output.float32le(mesh.morphs[i].normals[j].z - mesh.weights[j].normal.z);
                    }
                    createBufferView("MORPH_NORMAL_" + String.valueOf(i), morphStart, output.offset - morphStart);
                }
            }

            int matrixStart = output.offset;
            for (int i = 0; i < mesh.bones.length; ++i)
                for (int j = 0; j < 16; ++j)
                    output.float32le(mesh.bones[i].invSkinPoseMatrix[j]);
            createBufferView("MATRIX", matrixStart, output.offset - matrixStart);

            int jointStart = output.offset;
            for (int i = 0; i < mesh.weights.length; ++i)
                for (int j = 0; j < 4; ++j)
                    output.int8(mesh.weights[i].boneIndex[j]);
            
            createBufferView("JOINTS", jointStart, output.offset - jointStart);

            int weightStart = output.offset;
            for (int i = 0; i < mesh.weights.length; ++i) {
                output.float32le(mesh.weights[i].weights.x);
                output.float32le(mesh.weights[i].weights.y);
                output.float32le(mesh.weights[i].weights.z);
                output.float32le(mesh.weights[i].weights.w);
            }
            
            createBufferView("WEIGHTS", weightStart, output.offset - weightStart);
   
            output.shrinkToFit();
            
            Buffer buffer = new Buffer();
            buffer.setByteLength(output.buffer.length);
            this.gltf.addBuffers(buffer);
            
            this.buffer = output.buffer;
        }
        
        public static short getMin(short[] triangles) {
            short minValue = triangles[0];
            for (int i = 1; i < triangles.length; ++i)
                if (triangles[i] < minValue)
                    minValue = triangles[i];
            return minValue;
        }
        
        public static short getMax(short[] triangles) {
            short maxValue = triangles[0];
            for (int i = 1; i < triangles.length; ++i)
                if (triangles[i] > maxValue)
                    maxValue = triangles[i];
            return maxValue;
        }
        
        public void export(String path) {
            GltfAssetV2 gltfAssetV2 = new GltfAssetV2(this.gltf, ByteBuffer.wrap(this.buffer));
            GltfAssetWriterV2 writer = new GltfAssetWriterV2();
            
            try {
                FileOutputStream stream = new FileOutputStream(path);
                writer.writeBinary(gltfAssetV2, stream);
                stream.close();  
            } catch (IOException ex) {
                Logger.getLogger(MeshIO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}