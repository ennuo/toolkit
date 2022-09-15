package cwlib.util;

import org.joml.Vector4f;

/**
 * Utilities for handling color data.
 */
public final class Colors {
    /**
     * Nested utilities for RGBA colors
     */
    public static final class RGBA32 {
        /**
         * Converts a normalized vector containing RGBA colors
         * to an integer.
         * @param rgba Normalized color vector
         * @return 32-bit integer in RGBA order
         */
        public static int fromVector(Vector4f rgba) {
            if (rgba == null) 
                throw new NullPointerException("Can't convert null color!");
            return  (Math.round(rgba.x * 255) << 24) |
                    (Math.round(rgba.y * 255) << 16) |
                    (Math.round(rgba.z * 255) << 8) |
                    (Math.round(rgba.w * 255) << 0);
        }

        public static int getARGB(Vector4f rgba) {
            if (rgba == null) 
                throw new NullPointerException("Can't convert null color!");
            if (rgba.w != 1.0f) rgba.w = 1.0f;
            return  (Math.round(rgba.w * 255.0f) << 24) |
                (Math.round(rgba.x * 255.0f) << 16) |
                (Math.round(rgba.y * 255.0f) << 8) |
                (Math.round(rgba.z * 255.0f) << 0);
        }

        public static Vector4f fromARGB(int color) {
            return new Vector4f(
                ((color >>> 16) & 0xFF) / 255.0f,
                ((color >>> 8) & 0xFF) / 255.0f,
                ((color >>> 0) & 0xFF) / 255.0f,
                ((color >>> 24) & 0xFF) / 255.0f
            );
        }

        public static Vector4f toVector(int color) {
            return new Vector4f(
                ((color >> 24) & 0xFF) / 255.0f,
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                ((color >> 0) & 0xFF) / 255.0f
            );
        }
    }
}
