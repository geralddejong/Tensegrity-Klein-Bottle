/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.math;

/**
 * The Space3 class represents a 3x3 matrix for doing rotational transformations
 * 
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Space3 {
    private static final double EPS = 1.0E-8;
    public double m00, m01, m02;
    public double m10, m11, m12;
    public double m20, m21, m22;

    public Space3() {
    }

    public void setRotationX(double theta) { // rotate transformation about the X axis
        double c = Math.cos(theta);
        double s = Math.sin(theta);
        m00 = 1;
        m11 = c;
        m12 = -s;
        m21 = s;
        m22 = c;
    }

    public void setRotationY(double theta) { // rotate transformation about the Y axis
        double c = Math.cos(theta);
        double s = Math.sin(theta);
        m11 = 1;
        m22 = c;
        m20 = -s;
        m02 = s;
        m00 = c;
    }

    public void setRotationZ(double theta) { // rotate transformation about the Z axis
        double c = Math.cos(theta);
        double s = Math.sin(theta);
        m22 = 1;
        m00 = c;
        m01 = -s;
        m10 = s;
        m11 = c;
    }

    /**
     * Sets this Space3 to identity.
     */
    public final void setIdentity() {
        this.m00 = 1.0;
        this.m01 = 0.0;
        this.m02 = 0.0;

        this.m10 = 0.0;
        this.m11 = 1.0;
        this.m12 = 0.0;

        this.m20 = 0.0;
        this.m21 = 0.0;
        this.m22 = 1.0;
    }

    public void set(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }

    /**
     * Set the transformation to the given axis and angle.
     *
     * @param axis  where to rotate around
     * @param angle what angle in radians
     */

    public final void set(Arrow axis, double angle) {
        double mag = axis.span();
        if (mag < EPS) {
            m00 = 1.0;
            m01 = 0.0;
            m02 = 0.0;

            m10 = 0.0;
            m11 = 1.0;
            m12 = 0.0;

            m20 = 0.0;
            m21 = 0.0;
            m22 = 1.0;
        }
        else {
            mag = 1.0 / mag;
            double ax = axis.x * mag;
            double ay = axis.y * mag;
            double az = axis.z * mag;

            double sinTheta = Math.sin(angle);
            double cosTheta = Math.cos(angle);
            double t = 1.0 - cosTheta;

            double xz = ax * az;
            double xy = ax * ay;
            double yz = ay * az;

            m00 = t * ax * ax + cosTheta;
            m01 = t * xy - sinTheta * az;
            m02 = t * xz + sinTheta * ay;

            m10 = t * xy + sinTheta * az;
            m11 = t * ay * ay + cosTheta;
            m12 = t * yz - sinTheta * ax;

            m20 = t * xz - sinTheta * ay;
            m21 = t * yz + sinTheta * ax;
            m22 = t * az * az + cosTheta;
        }

    }

    /**
     * Computes the determinant of this matrix.
     *
     * @return the determinant of this matrix
     */
    public final double determinant() {
        double total;
        total = this.m00 * (this.m11 * this.m22 - this.m12 * this.m21)
                + this.m01 * (this.m12 * this.m20 - this.m10 * this.m22)
                + this.m02 * (this.m10 * this.m21 - this.m11 * this.m20);
        return total;
    }

    /**
     * Sets the value of this matrix to the result of multiplying itself
     * with matrix m1.
     *
     * @param m1 the other matrix
     */
    public final void mul(Space3 m1) {
        double c00, c01, c02, c10, c11, c12, c20, c21, c22;

        c00 = this.m00 * m1.m00 + this.m01 * m1.m10 + this.m02 * m1.m20;
        c01 = this.m00 * m1.m01 + this.m01 * m1.m11 + this.m02 * m1.m21;
        c02 = this.m00 * m1.m02 + this.m01 * m1.m12 + this.m02 * m1.m22;

        c10 = this.m10 * m1.m00 + this.m11 * m1.m10 + this.m12 * m1.m20;
        c11 = this.m10 * m1.m01 + this.m11 * m1.m11 + this.m12 * m1.m21;
        c12 = this.m10 * m1.m02 + this.m11 * m1.m12 + this.m12 * m1.m22;

        c20 = this.m20 * m1.m00 + this.m21 * m1.m10 + this.m22 * m1.m20;
        c21 = this.m20 * m1.m01 + this.m21 * m1.m11 + this.m22 * m1.m21;
        c22 = this.m20 * m1.m02 + this.m21 * m1.m12 + this.m22 * m1.m22;

        this.m00 = c00;
        this.m01 = c01;
        this.m02 = c02;
        this.m10 = c10;
        this.m11 = c11;
        this.m12 = c12;
        this.m20 = c20;
        this.m21 = c21;
        this.m22 = c22;
    }

    /**
     * Multiply this matrix by the tuple t and place the result
     * back into the tuple (t = this*t).
     *
     * @param a the tuple to be multiplied by this matrix and then replaced
     */
    public final void transform(Arrow a) {
        double x, y, z;
        x = m00 * a.x + m01 * a.y + m02 * a.z;
        y = m10 * a.x + m11 * a.y + m12 * a.z;
        z = m20 * a.x + m21 * a.y + m22 * a.z;
        a.set(x, y, z);
    }

    /**
     * Multiplies each element of this matrix by a scalar.
     *
     * @param scalar The scalar multiplier.
     */
    public final void mul(double scalar) {
        m00 *= scalar;
        m01 *= scalar;
        m02 *= scalar;
        m10 *= scalar;
        m11 *= scalar;
        m12 *= scalar;
        m20 *= scalar;
        m21 *= scalar;
        m22 *= scalar;
    }

    /**
     * Sets the value of this matrix to its inverse.
     */
    public final void invert() {
        double s = determinant();
        if (s == 0.0)
            return;
        s = 1 / s;
        // alias-safe way.
        set(m11 * m22 - m12 * m21, m02 * m21 - m01 * m22, m01 * m12 - m02 * m11,
                m12 * m20 - m10 * m22, m00 * m22 - m02 * m20, m02 * m10 - m00 * m12,
                m10 * m21 - m11 * m20, m01 * m20 - m00 * m21, m00 * m11 - m01 * m10);
        mul(s);
    }


    /**
     * Performs SVD on this matrix and gets scale and rotation.
     * Rotation is placed into rot.
     *
     * @param rot the rotation factor.
     * @return scale factor
     */
    private double SVD(Space3 rot) {
        // this is a simple svd.
        // Not complete but fast and reasonable.

        /*
         * SVD scale factors(squared) are the 3 roots of
         *
         *     | xI - M*MT | = 0.
         *
         * This will be expanded as follows
         *
         * x^3 - A x^2 + B x - C = 0
         *
         * where A, B, C can be denoted by 3 roots x0, x1, x2.
         *
         * A = (x0+x1+x2), B = (x0x1+x1x2+x2x0), C = x0x1x2.
         *
         * An avarage of x0,x1,x2 is needed here. C^(1/3) is a cross product
         * normalization factor.
         * So here, I use A/3. Note that x should be sqrt'ed for the
         * actual factor.
         */

        double s = Math.sqrt((
                m00 * m00 + m10 * m10 + m20 * m20 +
                m01 * m01 + m11 * m11 + m21 * m21 +
                m02 * m02 + m12 * m12 + m22 * m22
                ) / 3.0);

        if (rot != null) {
// zero-div may occur.
            float n = 1 / (float) Math.sqrt(m00 * m00 + m10 * m10 + m20 * m20);
            rot.m00 = m00 * n;
            rot.m10 = m10 * n;
            rot.m20 = m20 * n;

            n = 1 / (float) Math.sqrt(m01 * m01 + m11 * m11 + m21 * m21);
            rot.m01 = m01 * n;
            rot.m11 = m11 * n;
            rot.m21 = m21 * n;

            n = 1 / (float) Math.sqrt(m02 * m02 + m12 * m12 + m22 * m22);
            rot.m02 = m02 * n;
            rot.m12 = m12 * n;
            rot.m22 = m22 * n;
        }

        return s;
    }

    /**
     * Sets the scale component of the current matrix by factoring out the
     * current scale (by doing an SVD) from the rotational component and
     * multiplying by the new scale.
     *
     * @param scale the new scale amount
     */
    public final void setScale(double scale) {
        SVD(this);
        mul(scale);
    }
    
}
