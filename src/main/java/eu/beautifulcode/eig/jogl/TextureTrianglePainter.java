/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import javax.media.opengl.GL;
import eu.beautifulcode.eig.math.Arrow;

import java.io.IOException;

/**
 * Something to be seen in a GLViewPlatform will have to implement these functions.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TextureTrianglePainter {
    private static final float[][] TEX_COORD = {
            {1.0f, 1.0f},
            {0.5f, 0.0f},
            {0.0f, 1.0f},
    };
    private String [] resource;
    private int [] texture;
    private Arrow normal = new Arrow();
    private Arrow cross = new Arrow();
    private Arrow mid = new Arrow();
    private Arrow v0 = new Arrow();
    private Arrow v1 = new Arrow();
    private GL gl;

    public TextureTrianglePainter(String... resource) {
        this.resource = resource;
    }

    public void prePaint(GL gl, int textureIndex) {
        if (texture == null) {
            try {
                texture = TextureLoader.getTextures(gl, resource);
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to load image", e);
            }
        }
        gl.glDisable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture[textureIndex]);
        gl.glBegin(GL.GL_TRIANGLES);
        this.gl = gl;
    }

    public void paint(Arrow a, Arrow b, Arrow c) {
        mid.zero();
        mid.add(a);
        mid.add(b);
        mid.add(c);
        mid.scale(1/3.0);
        normal.zero();
        crossToNormal(a, b);
        crossToNormal(b, c);
        crossToNormal(c, a);
        double normalSpan = normal.span();
        if (normalSpan > 0.0001) {
            normal.scale(1/normalSpan);
        }
        gl.glNormal3d(normal.x, normal.y, normal.z);
        displayCorner(a, 0, gl);
        displayCorner(b, 1, gl);
        displayCorner(c, 2, gl);
    }

    public void postPaint() {
        gl.glEnd();
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_LIGHTING);
    }

    private void crossToNormal(Arrow a0, Arrow a1) {
        v0.sub(a0, mid);
        v1.sub(a1, mid);
        cross.cross(v1,v0);
        double span = cross.span();
        if (span != 0) {
            normal.add(cross, 1/span);
        }
    }

    private void displayCorner(Arrow location, int corner, GL gl) {
        gl.glTexCoord2f(TEX_COORD[corner][0], 1 - TEX_COORD[corner][1]);
        gl.glVertex3d(location.x, location.y, location.z);
    }

}