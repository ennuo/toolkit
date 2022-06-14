package cwlib.structs.animation;

public class AnimatedMorph {
    public float value;
    public float[] values;
    
    public boolean isAnimated = false;
    
    public AnimatedMorph(float value, int count) {
        this.value = value;
        this.values = new float[count];
    }
    
    public float getValueAtFrame(int frame) {
        if (values == null || !isAnimated) return value;
        if (frame < values.length) return values[frame];
        return value;
    }
}
