package workbench.model;

import cwlib.types.data.GUID;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;

public class AssetPreset {
    public static void main(String[] args) {
        GUID guid = new GUID(2551);

        FileIO.write(GsonUtils.toJSON(guid).getBytes(), "C:/users/aidan/desktop/guid");
    }
}
