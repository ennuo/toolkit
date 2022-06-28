package cwlib.types.mods.patches;

import java.util.ArrayList;

import cwlib.enums.PatchType;

public class TranslationPatch extends ModPatch {
    public static class TranslationData {
        private String tag, string;

        public TranslationData() {};
        public TranslationData(String tag, String string) {
            this.tag = tag;
            this.string = string;
        }

        public String getTag() { return this.tag; }
        public String getString() { return this.string; }

        public void setTag(String tag) { this.tag = tag; }
        public void setString(String string) { this.string = string; }
    }


    public TranslationPatch() { super(PatchType.TRANSLATION); }
    public TranslationPatch(String name) {
        this();
        this.setName(name);
    }

    private ArrayList<String> languages = new ArrayList<>();
    private ArrayList<TranslationData> keys = new ArrayList<>();

    public ArrayList<String> getLanguages() { return this.languages; }
    public ArrayList<TranslationData> getKeys() { return this.keys; }
}
