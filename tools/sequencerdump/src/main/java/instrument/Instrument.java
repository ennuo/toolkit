package instrument;

import cwlib.types.data.GUID;

public interface Instrument {
    GUID getGUID();
    String getName();

    static Instrument fromGUID(GUID guid) {
        for (StandardInstrument value : StandardInstrument.values()) {
            if (value.guid.equals(guid)) {
                return value;
            }
        }
        return new UnknownInstrument(guid, guid.getValue() + "_Unknown");
    }
}
