package ennuo.craftworld.resources.io;

import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.resources.Mesh;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;

public class MeshIO { 
    public static void exportOBJ (String path, Mesh mesh) { exportOBJ(path, mesh, 0); }
    public static void exportOBJ(String path, Mesh mesh, int channel) {
        StringBuilder builder = new StringBuilder((mesh.streams[0].length * 41) + (mesh.uvCount * 42) + (mesh.faces.length * 40));
        for (int j = 0; j < mesh.streams[0].length; ++j)
             builder.append("v " + mesh.streams[0][j].x + " " + mesh.streams[0][j].y + " " + mesh.streams[0][j].z + '\n');
        for (int i = 0; i < mesh.uvCount; ++i)
            builder.append("vt " + mesh.attributes[i][channel].x + " " + (1.0f - mesh.attributes[i][channel].y) + '\n');
        for (int i = -1, j = 1; i < mesh.faces.length; ++i, ++j) {
            if (i == -1 || mesh.faces[i] == -1) {
                String str = "f ";
                str += (mesh.faces[i + 1] + 1) + "/" + (mesh.faces[i + 1] + 1) + " ";
                str += (mesh.faces[i + 2] + 1) + "/" + (mesh.faces[i + 2] + 1) + " ";
                str += (mesh.faces[i + 3] + 1) + "/" + (mesh.faces[i + 3] + 1) + '\n';
                
                builder.append(str);
                i += 3; j = 0;
            } else {
                if ((j & 1) == 1) {
                    String str = "f ";
                    str += (mesh.faces[i - 2] + 1) + "/" + (mesh.faces[i - 2] + 1) + " ";
                    str += (mesh.faces[i] + 1) + "/" + (mesh.faces[i] + 1) + " ";
                    str += (mesh.faces[i - 1] + 1) + "/" + (mesh.faces[i - 1] + 1) + '\n';
                    builder.append(str);
                }
                else {
                    String str = "f ";
                    str += (mesh.faces[i - 2] + 1) + "/" + (mesh.faces[i - 2] + 1) + " ";
                    str += (mesh.faces[i - 1] + 1) + "/" + (mesh.faces[i - 1] + 1) + " ";
                    str += (mesh.faces[i] + 1) + "/" + (mesh.faces[i] + 1) + '\n';
                    builder.append(str);
                }
            }
        }
        
        FileIO.write(builder.toString().getBytes(), path); 
    }
}
