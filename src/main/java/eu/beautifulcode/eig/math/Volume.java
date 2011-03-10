package eu.beautifulcode.eig.math;

/**
 * Calculate volume of a tetrahedron based on edge lengths
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */
public class Volume {
    private static final int[][] Z = {// open triangles
        {5, 0, 1}, {3, 0, 2}, {0, 1, 4}, {2, 1, 3}, {4, 2, 0}, {5, 2, 1},
        {4, 3, 0}, {1, 3, 5}, {1, 4, 5}, {3, 4, 2}, {0, 5, 4}, {3, 5, 2}
    };

    private static final int[][] A = {// closed triangles
        {0, 1, 3}, {3, 4, 5}, {1, 2, 4}, {0, 2, 5}
    };

    private static final int[][] H = {// opposite edges
        {0, 4}, {1, 5}, {2, 3}
    };

    /**
     * calculate the volume of a tetrahedron given the edge lengths.  note that the volume of a regular
     * tetrahedron with edges of length one is calibrated to be one.
     *
     * @param ab edge length
     * @param ac edge length
     * @param ad edge length
     * @param bc edge length
     * @param cd edge length
     * @param db edge length
     * @return volume
     */
    
    public static double tetrahedron(double ab, double ac, double ad, double bc, double cd, double db) {
        double[] q = new double[] {
                ab*ab, ac*ac, ad*ad, bc*bc, cd*cd, db*db
        };
        double sum = 0;
        for (int i = 0; i < 12; i++) { // sum of open triangle quadrance products
            sum += q[Z[i][0]] * q[Z[i][1]] * q[Z[i][2]];
        }
        for (int i = 0; i < 4; i++) { // minus sum of closed triangle QP
            sum -= q[A[i][0]] * q[A[i][1]] * q[A[i][2]];
        }
        for (int i = 0; i < 3; i++) { // minus sum of product times sum of opposite edges
            sum -= q[H[i][0]] * q[H[i][1]] * (q[H[i][0]] + q[H[i][1]]);
        }
        return sum/2;
    }

}
