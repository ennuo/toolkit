package cwlib.types.swing;

import cwlib.types.data.ResourceReference;
import cwlib.types.data.ResourceReference;
import cwlib.types.data.SHA1;
import cwlib.util.Strings;

public class SearchParameters {
   public String path;
   public ResourceReference pointer;
   
   public SearchParameters(String query) {
        this.path = query.toLowerCase().replaceAll("\\s", "");
        if (query.startsWith("res:")) {
            this.pointer = new ResourceReference();
            String res = this.path.substring(4);
            if (res.startsWith("g"))
                pointer.GUID = Strings.getLong(res);
            else if (res.startsWith("h") && res.length() == 41)
                pointer.hash = new SHA1(res.substring(1));
        }
   }  
}
