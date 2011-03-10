/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Face;
import eu.beautifulcode.eig.structure.Span;

import javax.media.opengl.GL;

/**
 * Something to be seen in a GLViewPlatform will have to implement these functions.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FacePainter {
    private static float SHININESS = -1000.0f;
    private Tint BORING = new Tint(Tint.BLACK, Tint.WHITE, 0.4f);
    private Tint AMBIENT_AND_DIFFUSE = new Tint(Tint.BLACK, Tint.WHITE, 0.4f);
    private Tint SPECULAR = new Tint(Tint.BLACK, Tint.WHITE, 0.28f);
    private Arrow normal = new Arrow();
    private Arrow cross = new Arrow();
    private Arrow mid = new Arrow();
    private Arrow a = new Arrow();
    private Arrow b = new Arrow();
    private GL gl;
    private Span.StressRange range;

    public FacePainter(Span.StressRange range) {
        this.range = range;
    }

    public Span.StressRange getRange() {
        return range;
    }

    public void preVisit(GL gl) {
        this.gl = gl;
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, SPECULAR.getFloatArray(), 0);
        gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, SHININESS);
        gl.glEnable(GL.GL_LIGHTING);
    }

    public void visit(Face face) {
        if (face.getJoints().size() != 3) return;
        gl.glBegin(GL.GL_TRIANGLES);
        if (face.getStressInterval() != null) {
            float stress = (float) face.getStressInterval().getSpan().getStress(range);
            if (stress > 0.5) {
                AMBIENT_AND_DIFFUSE.set(BORING, Tint.BLUE, (stress - 0.5f) * 2);
            }
            else {
                AMBIENT_AND_DIFFUSE.set(Tint.RED, BORING, stress * 2);
            }
            gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, AMBIENT_AND_DIFFUSE.getFloatArray(), 0);
        }
        else {
            gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, BORING.getFloatArray(), 0);
        }
        face.getNormal(normal, mid, cross, a, b);
        gl.glNormal3d(normal.x, normal.y, normal.z);
        switch (face.getOrder()) {
            case LEFT_HANDED:
                displayCorner(face.getJoints().get(0).getLocation());
                displayCorner(face.getJoints().get(1).getLocation());
                displayCorner(face.getJoints().get(2).getLocation());
                break;
            case RIGHT_HANDED:
                displayCorner(face.getJoints().get(2).getLocation());
                displayCorner(face.getJoints().get(1).getLocation());
                displayCorner(face.getJoints().get(0).getLocation());
                break;
        }
        gl.glEnd();
    }

    private void displayCorner(Arrow location) {
        gl.glVertex3d(location.x, location.y, location.z);
    }
}