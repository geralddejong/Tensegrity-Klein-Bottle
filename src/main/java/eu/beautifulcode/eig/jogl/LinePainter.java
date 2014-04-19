/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Interval;
import eu.beautifulcode.eig.structure.Span;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

/**
 * Something to be seen in a GLViewPlatform will have to implement these functions.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class LinePainter {
    private GL2 gl;
    private Span.StressRange range;

    public LinePainter(Span.StressRange range) {
        this.range = range;
    }

    public Span.StressRange getRange() {
        return range;
    }

    public void preVisit(GL2 gl) {
        this.gl = gl;
        gl.glDisable(GL_LIGHTING);
        gl.glBegin(GL_LINES);
    }

    public void postVisit(GL2 gl) {
        gl.glEnd();
    }

    public void visit(Interval interval) {
//        gl.glColor3f(0.7f, 0.7f, 0.7f);
        gl.glColor3f(0.2f, 0.2f, 0.2f + 0.8f * (float) interval.getSpan().getStress(range));
        Arrow alpha = interval.get(false).getLocation();
        Arrow omega = interval.get(true).getLocation();
        gl.glVertex3d(alpha.x, alpha.y, alpha.z);
        gl.glVertex3d(omega.x, omega.y, omega.z);
    }
}