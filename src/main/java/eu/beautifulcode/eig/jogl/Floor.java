/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import eu.beautifulcode.eig.math.Arrow;

import java.util.ArrayList;
import java.util.List;

/**
 * Paint a floor around a middle point in all directions on
 * the z=0 plane.  The floor is a beehive pattern of hexagons.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Floor {
    private static final double RADIUS = 20;
    private TextureTrianglePainter painter = new TextureTrianglePainter("/TriangleTexture.png");
    private List<Arrow> corners = new ArrayList<Arrow>();

    public Floor() {
        for (int walk=0; walk<3; walk++) {
            corners.add(new Arrow(RADIUS * Math.sin(-Math.PI*2/3 * walk), RADIUS * Math.cos(-Math.PI*2/3 * walk), 0));
        }
    }

    public void display(GL2 gl) {
        painter.prePaint(gl, 0);
        gl.glColor3f(0,0.6f,0);
        painter.paint(corners.get(0), corners.get(1), corners.get(2));
        painter.postPaint();
    }
}