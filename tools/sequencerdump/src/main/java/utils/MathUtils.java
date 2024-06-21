package utils;

public final class MathUtils {

    private MathUtils() {
    }

    public static float roundToFraction(float value, int fraction) {
        return (float) Math.round(value * fraction) / fraction;
    }
}
