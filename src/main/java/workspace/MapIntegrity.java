package workspace;

import cwlib.types.databases.FileDB;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import toolkit.utilities.ResourceSystem;

public class MapIntegrity {
    public static void main(String[] args) {
        byte[] data = FileIO.read("C:/Users/Rueezus/Downloads/pre_something.map");

        for (int i = 22564; i < 22568; ++i) {
            System.out.println(i);
            byte[] integer = Bytes.toBytesBE(i);
            for (int j = 0; j < 4; ++j)
                data[0x4 + j] = integer[j];
            try {
                FileDB db = new FileDB(data);
            } catch (Exception ex) {
                System.out.println("FAILED! at " + i);
                ex.printStackTrace();
                break;
            }
        }
        


    }
}
