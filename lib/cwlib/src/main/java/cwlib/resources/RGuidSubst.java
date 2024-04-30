package cwlib.resources;

import java.util.HashMap;

import cwlib.types.data.GUID;
import cwlib.util.FileIO;
import cwlib.util.Strings;

public class RGuidSubst extends HashMap<GUID, GUID>
{
    public static RGuidSubst LBP2_TO_LBP1_PLANS =
        RGuidSubst.fromCSV(FileIO.getResourceFileAsString("/poppet_lbp1.txt"));
    public static RGuidSubst LBP2_TO_LBP1_GMATS =
        RGuidSubst.fromCSV(FileIO.getResourceFileAsString("/gmat_lbp1.txt"));
    
    public static RGuidSubst fromCSV(String data)
    {
        RGuidSubst subst = new RGuidSubst();
        String[] lines = data.split("\n");
        for (String line : lines)
        {
            line = line.replaceAll("\\s", "");
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] row = line.split(",");
            subst.put(Strings.getGUID(row[0]), Strings.getGUID(row[1]));
        }
        return subst;
    }
}
