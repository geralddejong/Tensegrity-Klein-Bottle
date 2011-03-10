/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Face;
import eu.beautifulcode.eig.structure.Interval;
import eu.beautifulcode.eig.structure.Joint;
import eu.beautifulcode.eig.structure.Span;

import javax.media.opengl.GL;
import java.util.List;

/**
 * Paint a fabric in various ways
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class FabricPainter {
    private TextureTrianglePainter painter = new TextureTrianglePainter("/TriangleTexture.png");
    private Span.StressRange stressRange;

    public FabricPainter(Span.StressRange stressRange) {
        this.stressRange = stressRange;
    }

    public void paintTexture(Fabric fabric, GL gl) {
        painter.prePaint(gl, 0);
        for (Face face : fabric.getFaces()) {
            paintFace(face, gl);
        }
        painter.postPaint();
    }

    public void paintLines(Fabric fabric, GL gl, Tint tint) {
        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glBegin(GL.GL_LINES);
        for (Interval interval : fabric.getIntervals()) {
            if (tint != null) {
                gl.glColor3fv(tint.getFloatArray(), 0);
            }
            else {
                float redness = (float) interval.getSpan().getStress(stressRange);
                gl.glColor3f(redness, 0.4f, 1 - redness);
            }
            paintVertex(gl, interval.get(false).getLocation());
            paintVertex(gl, interval.get(true).getLocation());
        }
        gl.glEnd();
    }

    private void paintVertex(GL gl, Arrow location) {
        gl.glVertex3d(location.x, location.y, location.z);
    }

    private void paintFace(Face face, GL gl) {
        if (face.getJoints().size() != 3) return;
        if (stressRange != null) {
            setColor(face.getStressInterval(), gl);
        }
        else {
            gl.glColor3d(1, 1, 1);
        }
        List<Joint> joints = face.getJoints();
        switch (face.getOrder()) {
            case LEFT_HANDED:
                painter.paint(joints.get(0).getLocation(), joints.get(1).getLocation(), joints.get(2).getLocation());
                break;
            case RIGHT_HANDED:
                painter.paint(joints.get(2).getLocation(), joints.get(1).getLocation(), joints.get(0).getLocation());
                break;
        }
    }

    private void setColor(Interval stressInterval, GL gl) {
        if (stressInterval != null) {
            double stress = stressInterval.getSpan().getStress(stressRange);
            gl.glColor3d(
                    adjust(1 - stress),
                    0.4,
                    adjust(stress)
            );
        }
        else {
            gl.glColor3d(1, 1, 1);
        }
    }

    private static double adjust(double value) {
        return 0.1 + value * 0.9;
    }
}
