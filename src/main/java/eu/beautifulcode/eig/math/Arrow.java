/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.math;

import java.text.DecimalFormat;

/**
 * The arrow class represents a three-dimensional vector of doubles
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Arrow {
    public double x, y, z;

    public Arrow() {
    }

    public Arrow(Arrow arrow) {
        this.x = arrow.x;
        this.y = arrow.y;
        this.z = arrow.z;
    }

    public Arrow(Arrow arrow0, Arrow arrow1) {
        average(arrow0, arrow1);
    }

    public Arrow(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Arrow set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Arrow set(Arrow arrow) {
        this.x = arrow.x;
        this.y = arrow.y;
        this.z = arrow.z;
        return this;
    }

    public Arrow set(Arrow arrow, double scale) {
        this.x = arrow.x * scale;
        this.y = arrow.y * scale;
        this.z = arrow.z * scale;
        return this;
    }

    public Arrow add(Arrow arrow) {
        this.x += arrow.x;
        this.y += arrow.y;
        this.z += arrow.z;
        return this;
    }

    public Arrow add(Arrow arrow, double scale) {
        this.x += arrow.x * scale;
        this.y += arrow.y * scale;
        this.z += arrow.z * scale;
        return this;
    }

    public Arrow average(Arrow arrow0, Arrow arrow1) {
        add(arrow0,arrow1);
        scale(0.5);
        return this;
    }

    public Arrow sub(Arrow arrow) {
        this.x -= arrow.x;
        this.y -= arrow.y;
        this.z -= arrow.z;
        return this;
    }

    public Arrow sub(Arrow arrow, double scale) {
        this.x -= arrow.x * scale;
        this.y -= arrow.y * scale;
        this.z -= arrow.z * scale;
        return this;
    }

    public Arrow scale(double scale) {
        if (Double.isNaN(scale)) {
            throw new IllegalStateException("Cannot scale by NaN");
        }
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
        return this;
    }

    public double quadrance() {
        return x*x + y*y + z*z;
    }

    public double quadranceTo(Arrow that) {
        double dx = this.x - that.x;
        double dy = this.y - that.y;
        double dz = this.z - that.z;
        return dx*dx + dy*dy + dz*dz;
    }

    public double span() {
        return Math.sqrt(quadrance());
    }

    public Arrow setSpan(double span) {
        double currentSpan = span();
        if (currentSpan == 0.0) {
            throw new IllegalStateException("Cannot normalize, zero span");
        }
        scale(span/currentSpan);
        return this;        
    }

    public double distanceTo(Arrow that) {
        return Math.sqrt(quadranceTo(that));
    }

    public double dot(Arrow arrow) {
        return this.x*arrow.x + this.y*arrow.y + this.z*arrow.z;
    }

    public Arrow add(Arrow a, Arrow b) {
        this.x = a.x + b.x;
        this.y = a.y + b.y;
        this.z = a.z + b.z;
        return this;
    }

    public Arrow sub(Arrow a, Arrow b) {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
        this.z = a.z - b.z;
        return this;
    }

    public Arrow cross(Arrow a, Arrow b) {
        double xx = a.y * b.z - a.z * b.y;
        double yy = a.z * b.x - a.x * b.z;
        double zz = a.x * b.y - a.y * b.x;
        this.x = xx;
        this.y = yy;
        this.z = zz;
        return this;
    }

    public double normalize() {
        double span = span();
        if (span == 0.0) {
            throw new IllegalStateException("Cannot normalize, zero span");
        }
        scale(1/span);
        return span;
    }

    public final void interpolate(Arrow a, Arrow b, double interpolation) {
        double antiInterpolation = 1 - interpolation;
        x = a.x * antiInterpolation + b.x * interpolation;
        y = a.y * antiInterpolation + b.y * interpolation;
        z = a.z * antiInterpolation + b.z * interpolation;
    }


    public boolean isNaN() {
        return Double.isNaN(x) ||Double.isNaN(y) ||Double.isNaN(z);
    }

    public Arrow zero() {
        x = y = z = 0;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arrow arrow = (Arrow) o;
        return Double.compare(arrow.x, x) == 0 && Double.compare(arrow.y, y) == 0 && Double.compare(arrow.z, z) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = x != +0.0d ? Double.doubleToLongBits(x) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = y != +0.0d ? Double.doubleToLongBits(y) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = z != +0.0d ? Double.doubleToLongBits(z) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public Arrow random() {
        double s;
        do {
            x = rand();
            y = rand();
            z = rand();
            s = span();
        }
        while (s > 1 && s < 0.01); // avoid the corners of the cube
        scale(1/s);
        return this;
    }

    private static double rand() {
        return 2*(-0.5+Math.random());
    }

    private static DecimalFormat DECIMAL = new DecimalFormat("00.000000");

    public String toString() {
        return "("+ DECIMAL.format(x)+","+ DECIMAL.format(y)+","+ DECIMAL.format(z)+")";
    }

}
