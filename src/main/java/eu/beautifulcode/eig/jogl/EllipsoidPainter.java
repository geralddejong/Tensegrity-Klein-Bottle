/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Interval;
import eu.beautifulcode.eig.structure.Span;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import static javax.media.opengl.GL2.*;

/**
 * Something to be seen in a GLViewPlatform will have to implement these functions.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class EllipsoidPainter {
    public static final double DEFAULT_WIDTH = 0.05;
    private static final double MINIMUM_SPAN = 0.01;
    private static final double RADIANS_TO_DEGREES = 180 / Math.PI;
    private static float SHININESS = -1000.0f;
    private Tint AMBIENT_AND_DIFFUSE = new Tint(Tint.BLACK, Tint.WHITE, 0.6f);
    private Tint SPECULAR = new Tint(Tint.BLACK, Tint.WHITE, 0.6f);
    private Arrow intervalLocation = new Arrow();
    private double width = DEFAULT_WIDTH;
    private GLU glu = new GLU();
    private GL2 gl;
    private int glSphere;
    private Span.StressRange range;

    public EllipsoidPainter(Span.StressRange range) {
        this.range = range;
    }

    public Span.StressRange getRange() {
        return range;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void preVisit(GL2 gl) {
        this.gl = gl;
        gl.glMaterialfv(GL.GL_FRONT, GL_SPECULAR, SPECULAR.getFloatArray(), 0);
        gl.glMaterialf(GL.GL_FRONT, GL_SHININESS, SHININESS);
        gl.glEnable(GL_LIGHTING);
        glSphere = gl.glGenLists(1);
        gl.glNewList(glSphere, GL_COMPILE);
        GLUquadric quadric = glu.gluNewQuadric();
        glu.gluSphere(quadric,1,7,7);
        gl.glEndList();
    }

    public void visit(Interval interval) {
        interval.getLocation(intervalLocation);
        Arrow unit = interval.getUnit(false);
        double span = interval.getSpan().getActual();
        if (span < MINIMUM_SPAN) {
            return;
        }
        gl.glMaterialfv(GL.GL_FRONT, GL_AMBIENT_AND_DIFFUSE, AMBIENT_AND_DIFFUSE.getFloatArray(), 0);
        gl.glPushMatrix();
        gl.glTranslated(intervalLocation.x, intervalLocation.y, intervalLocation.z);
        gl.glRotated(RADIANS_TO_DEGREES * Math.acos(unit.z), -unit.y, unit.x, 0);
        gl.glScaled(span* width, span* width, span/2);
        gl.glCallList(glSphere);
        gl.glPopMatrix();
    }
}