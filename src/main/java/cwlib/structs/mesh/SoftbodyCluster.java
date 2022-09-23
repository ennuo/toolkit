package cwlib.structs.mesh;

import org.joml.Matrix4f;
import org.joml.Vector4f;
/**
 * Data used for softbody calculations.
 * I'm going to be honest, if anybody can tell me what
 * any of these words mean, that'd be great.
 */
public class SoftbodyCluster {
    public static final int MAX_CLUSTER_NAME_LENGTH = 0x20;
    public static final int QUAD_DYADIC_SUM_LENGTH = 81;

    public static final int BASE_ALLOCATION_SIZE = MAX_CLUSTER_NAME_LENGTH + (QUAD_DYADIC_SUM_LENGTH * 0x4) + 0x60;

    private String name;
    private Vector4f restCenterOfMass;
    private Matrix4f restDyadicSum;
    private float[] restQuadraticDyadicSum = new float[QUAD_DYADIC_SUM_LENGTH];

    public SoftbodyCluster() {
        // float9x9
        for (int i = 0; i < QUAD_DYADIC_SUM_LENGTH; ++i)
            if (i % 10 == 0)
                this.restQuadraticDyadicSum[i] = 1.0f;
    }

    public String getName() { return this.name; }
    public Vector4f getRestCenterOfMass() { return this.restCenterOfMass; }
    public Matrix4f getRestDyadicSum() { return this.restDyadicSum; }
    public float[] getRestQuadraticDyadicSum() { return this.restQuadraticDyadicSum; }

    public void setName(String name) {
        if (name != null && name.length() > MAX_CLUSTER_NAME_LENGTH)
            throw new IllegalArgumentException("Cluster name cannot be longer than 32 characters.");
        this.name = name;
    }
    public void setRestCenterOfMass(Vector4f COM) { this.restCenterOfMass = COM; }
    public void setRestDyadicSum(Matrix4f sum) { this.restDyadicSum = sum; }
    public void setRestQuadraticDyadicSum(float[] sum) {
        if (sum == null || sum.length != QUAD_DYADIC_SUM_LENGTH)
            throw new IllegalArgumentException("Rest quadratic dyadic sum array length must be 81!");
        this.restQuadraticDyadicSum = sum;
    }
}
