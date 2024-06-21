package instrument;

import cwlib.types.data.GUID;

import java.util.Objects;

public record UnknownInstrument(GUID guid, String name) implements Instrument {
    @Override
    public GUID getGUID() {
        return guid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnknownInstrument that)) return false;
        return Objects.equals(guid, that.guid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(guid);
    }
}
