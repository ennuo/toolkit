package cwlib.structs.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

/**
 * Softbody clusters container.
 */
public class SoftbodyClusterData implements Serializable, Iterable<SoftbodyCluster> {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    private ArrayList<SoftbodyCluster> clusters = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override public SoftbodyClusterData serialize(Serializer serializer, Serializable structure) {
        SoftbodyClusterData softbody = (structure == null) ? new SoftbodyClusterData() : (SoftbodyClusterData) structure;

        // The actual way this is serialized makes it annoying to
        // edit anything in a standard object oriented way, so
        // we'll just be encapsulating the data in separate cluster classes.

        if (serializer.isWriting()) {
            // We need to prepare all the arrays for serialization.
            int clusterCount = softbody.clusters.size();
            Vector4f[] restCOM = new Vector4f[clusterCount];
            Matrix4f[] restDyadicSum = new Matrix4f[clusterCount];
            float[] restQuadraticDyadicSum = new float[clusterCount * SoftbodyCluster.QUAD_DYADIC_SUM_LENGTH];
            String[] names = new String[clusterCount];
            for (int i = 0; i < clusterCount; ++i) {
                SoftbodyCluster cluster = softbody.clusters.get(i);
                restCOM[i] = cluster.getRestCenterOfMass();
                restDyadicSum[i] = cluster.getRestDyadicSum();

                // All the rest dyadic sum values are stored
                // in the same array.
                System.arraycopy(
                    cluster.getRestQuadraticDyadicSum(), 
                    0, 
                    restQuadraticDyadicSum, 
                    i * SoftbodyCluster.QUAD_DYADIC_SUM_LENGTH, 
                    SoftbodyCluster.QUAD_DYADIC_SUM_LENGTH
                );
                
                names[i] = cluster.getName();
            }

            // Begin to actually serialize the data to the array.
            MemoryOutputStream stream = serializer.getOutput();

            stream.i32(clusterCount);

            stream.i32(restCOM.length);
            for (Vector4f COM : restCOM)
                stream.v4(COM);

            stream.i32(restDyadicSum.length);
            for (Matrix4f dyadicSum : restDyadicSum)
                stream.m44(dyadicSum);

            stream.floatarray(restQuadraticDyadicSum);

            stream.i32(names.length);
            for (String name : names)
                stream.str(name, SoftbodyCluster.MAX_CLUSTER_NAME_LENGTH);

            return softbody;
        }

        MemoryInputStream stream = serializer.getInput();

        int clusterCount = stream.i32();
        softbody.clusters = new ArrayList<>(clusterCount);

        // Technically, when reading from the softbody cluster data
        // it's possible for the arrays to not match the cluster counts,
        // but if you're doing that, go fuck yourself.

        Vector4f[] restCOM = new Vector4f[clusterCount];
        Matrix4f[] restDyadicSum = new Matrix4f[clusterCount];
        float[] restQuadraticDyadicSum = new float[clusterCount * SoftbodyCluster.QUAD_DYADIC_SUM_LENGTH];
        String[] names = new String[clusterCount];

        stream.i32(); // restCOM array length
        for (int i = 0; i < clusterCount; ++i)
            restCOM[i] = stream.v4();
        stream.i32(); // restDyadicSum array length
        for (int i = 0; i < clusterCount; ++i)
            restDyadicSum[i] = stream.m44();
        stream.i32(); // restQudraticDyadicSum array length
        for (int i = 0; i < restQuadraticDyadicSum.length; ++i)
            restQuadraticDyadicSum[i] = stream.f32();
        stream.i32(); // name array length
        for (int i = 0; i < clusterCount; ++i)
            names[i] = stream.str(SoftbodyCluster.MAX_CLUSTER_NAME_LENGTH);
        
        for (int i = 0; i < clusterCount; ++i) {
            SoftbodyCluster cluster = new SoftbodyCluster();

            cluster.setRestCenterOfMass(restCOM[i]);
            cluster.setRestDyadicSum(restDyadicSum[i]);
            int offset = i * SoftbodyCluster.QUAD_DYADIC_SUM_LENGTH;
            cluster.setRestQuadraticDyadicSum(
                Arrays.copyOfRange(restQuadraticDyadicSum, offset, offset + SoftbodyCluster.QUAD_DYADIC_SUM_LENGTH)
            );
            cluster.setName(names[i]);

            softbody.clusters.add(cluster);
        }

        return softbody;
    }

    @Override public int getAllocatedSize() { 
        return BASE_ALLOCATION_SIZE + (this.clusters.size() * SoftbodyCluster.BASE_ALLOCATION_SIZE); 
    }

    @Override public Iterator<SoftbodyCluster> iterator() { return this.clusters.iterator(); }

    public ArrayList<SoftbodyCluster> getClusters() { return this.clusters; }
}