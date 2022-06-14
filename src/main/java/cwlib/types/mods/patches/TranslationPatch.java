package cwlib.types.mods.patches;

public class TranslationPatch extends ModPatch {
    public TranslationPatch() { this.type = "translation"; }
    public class TranslationData {
        public String tag;
        public String string;
    }
    public String[] languages;
    public TranslationData[] keys;
}
