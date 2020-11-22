package ennuo.craftworld.memory;

public class Vector4f {
    public float x, y, z, w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Vector4f)) return false;
        Vector4f d = (Vector4f) o;
        return (d.x == x && d.y == y && d.z == z && d.w == w);
    }
}