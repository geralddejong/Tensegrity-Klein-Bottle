package org.darwinathome.geometry;

/**
 * tetrahedron volume based on edge lengths
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */
public class Sublime {

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

	private static final String[] N = {
		"AB", "AC", "AD", "BC", "CD", "DB"
	};

	public static void main(String[] args) {
		double[] q = new double[6]; // quadrance
		double sum = 0;
		double lp = 1;
		if (args.length != 6) {
			System.out.println("Usage: sublime AB AC AD BC CD DB\n");
			System.exit(0);
		}
		for (int walk = 0; walk < args.length; walk++) {
			double d = Double.parseDouble(args[walk]);
			System.out.println(N[walk] + "=" + d);
			lp *= d;
			q[walk] = d * d;
		}
		for (int i = 0; i < 12; i++) { // sum of open triangle quadrance products
			sum += q[Z[i][0]] * q[Z[i][1]] * q[Z[i][2]];
		}
		for (int i = 0; i < 4; i++) { // minus sum of closed triangle QP
			sum -= q[A[i][0]] * q[A[i][1]] * q[A[i][2]];
		}
		for (int i = 0; i < 3; i++) { // minus sum of product times sum of opposite edges
			sum -= q[H[i][0]] * q[H[i][1]] * (q[H[i][0]] + q[H[i][1]]);
		}
		sum /= 2;
		// synergetic volume has tetra with all edge lengths 1 produce volume 1
		System.out.println("Synergetic Volume = sqrt("+sum+") = "+Math.sqrt(sum));
		System.out.println("Regularity = " + (sum/lp) );
	}

}