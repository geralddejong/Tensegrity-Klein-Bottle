/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

/**
 * Floating point colors for OpenGL
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Tint {
    private static final float LO = 0.0f;
    private static final float HI = 1f;
    public static final Tint BLACK = new Tint(0f, 0f, 0f);
    public static final Tint WHITE = new Tint(1f, 1f, 1f);
    public static final Tint RED = new Tint(HI, LO, LO);
    public static final Tint CYAN = new Tint(LO, HI, HI);
    public static final Tint GREEN = new Tint(LO, HI, LO);
    public static final Tint MAGENTA = new Tint(HI, LO, HI);
    public static final Tint BLUE = new Tint(LO, LO, HI);
    public static final Tint YELLOW = new Tint(HI, HI, LO);
    public float [] c = new float[3];

    public Tint() {
    }

    public Tint(float red, float green, float blue) {
        this.c[0] = red;
        this.c[1] = green;
        this.c[2] = blue;
    }

    public Tint(Tint tintA, Tint tintB, float interpolation) {
        set(tintA, tintB,interpolation);
    }

    public void set(Tint tint) {
        this.c[0] = tint.c[0];
        this.c[1] = tint.c[1];
        this.c[2] = tint.c[2];
    }

    public void set(Tint tintA, Tint tintB, float interpolation) {
        c[0] = tintA.getRed() * (1 - interpolation) + tintB.getRed() * interpolation;
        c[1] = tintA.getGreen() * (1 - interpolation) + tintB.getGreen() * interpolation;
        c[2] = tintA.getBlue() * (1 - interpolation) + tintB.getBlue() * interpolation;
    }

    public float getRed() {
        return c[0];
    }

    public float getGreen() {
        return c[1];
    }

    public float getBlue() {
        return c[2];
    }

    public float[] getFloatArray() {
        return c;
    }

    public void scale(float factor) {
        c[0] *= factor;
        c[1] *= factor;
        c[2] *= factor;
    }

    public static Tint[] buildGradient(Tint fromTint, Tint middleTint, Tint toTint, int size) {
        Tint[] gradient = new Tint[size];
        for (int walk = 0; walk < gradient.length / 2; walk++) {
            float degree = (float) walk / ((float) gradient.length / 2);
            gradient[walk] = new Tint(fromTint, middleTint, degree);
            int antiWalk = gradient.length - walk - 1;
            gradient[antiWalk] = new Tint(toTint, middleTint, degree);
        }
        gradient[gradient.length / 2] = middleTint;
        return gradient;
    }
}
