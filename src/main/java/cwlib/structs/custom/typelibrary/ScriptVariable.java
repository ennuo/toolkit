package cwlib.structs.custom.typelibrary;

import cwlib.enums.ScriptVariableType;
import cwlib.io.Serializable;

public interface ScriptVariable extends Serializable {
    ScriptVariableType getVariableType();
}
