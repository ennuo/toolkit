package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;

public class Copyright {
    public static int MAX_SIZE = 0x24;
    
    
    public String PSID;
    public String region;
    public boolean shareable = true;
    
    public Copyright(String PSID, String region) {
        this.PSID = PSID;
        this.region = region;
    }
    
    public Copyright(Data data) {
        PSID = data.str(0x14);
        region = data.str(0x8);
        shareable = data.bool();
        data.forward(7);
    }
    
    public void serialize(Output output) {
        output.str(PSID, 0x14);
        output.str(region, 0x8);
        output.bool(shareable);
        output.pad(7);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Copyright)) return false;
        Copyright d = (Copyright)o;
        return (d.PSID.equals(PSID) && d.region.equals(region));
    }
}
