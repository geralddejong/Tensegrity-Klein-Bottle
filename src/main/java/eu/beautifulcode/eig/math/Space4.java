/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.math;

/**
 * This is a four-dimensional space represented as a matrix.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Space4 {
    public double m00, m01, m02, m03;
    public double m10, m11, m12, m13;
    public double m20, m21, m22, m23;
    public double m30, m31, m32, m33;

    /**
     * Sets the values in this matrix equal to the row-major
     * array parameter (ie, the first four elements of the
     * array will be copied into the first row of this matrix, etc.).
     *
     * @param m the single precision array of length 16
     */
    public final void set(double[] m) {
        m00 = m[0];
        m01 = m[1];
        m02 = m[2];
        m03 = m[3];
        m10 = m[4];
        m11 = m[5];
        m12 = m[6];
        m13 = m[7];
        m20 = m[8];
        m21 = m[9];
        m22 = m[10];
        m23 = m[11];
        m30 = m[12];
        m31 = m[13];
        m32 = m[14];
        m33 = m[15];
    }

    /**
     * Transforms the normal parameter by this transform and places the value
     * back into normal.  The fourth element of the normal is assumed to be zero.
     *
     * @param a the input normal to be transformed.
     */
    public final void transform(Arrow a) {
        double x = m00 * a.x + m01 * a.y + m02 * a.z;
        double y = m10 * a.x + m11 * a.y + m12 * a.z;
        double z = m20 * a.x + m21 * a.y + m22 * a.z;
        a.set(x, y, z);
    }

    public double determinant() {
        return m03 * m12 * m21 * m30 - m02 * m13 * m21 * m30 -
                m03 * m11 * m22 * m30 + m01 * m13 * m22 * m30 +
                m02 * m11 * m23 * m30 - m01 * m12 * m23 * m30 -
                m03 * m12 * m20 * m31 + m02 * m13 * m20 * m31 +
                m03 * m10 * m22 * m31 - m00 * m13 * m22 * m31 -
                m02 * m10 * m23 * m31 + m00 * m12 * m23 * m31 +
                m03 * m11 * m20 * m32 - m01 * m13 * m20 * m32 -
                m03 * m10 * m21 * m32 + m00 * m13 * m21 * m32 +
                m01 * m10 * m23 * m32 - m00 * m11 * m23 * m32 -
                m02 * m11 * m20 * m33 + m01 * m12 * m20 * m33 +
                m02 * m10 * m21 * m33 - m00 * m12 * m21 * m33 -
                m01 * m10 * m22 * m33 + m00 * m11 * m22 * m33;
    }

}
