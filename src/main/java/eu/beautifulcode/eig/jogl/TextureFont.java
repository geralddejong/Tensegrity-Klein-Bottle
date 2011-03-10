/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import javax.media.opengl.GL;
import eu.beautifulcode.eig.math.Arrow;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class loads an image containing font characters as a texture and then
 * lets you display arbitrary strings in space with a given orientation.
 * <p/>
 * Based on work by konik (konik0001@msn.com), which was based on work
 * of Nicholas Campbell - campbelln@hartwick.edu.  Stuff was found on the
 * NeHe site (http://nehe.gamedev.net/), and many changes were made to
 * smooth it all out.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TextureFont {
    private static final int CHAR_COUNT = 256;
    private static final int TEXTURE_STEPI = 16;
    private static final float TEXTURE_STEPF = (float) TEXTURE_STEPI;
    private static final float TEXTURE_PART = 1f / TEXTURE_STEPF;
    private static final float VERTEX_STEP = 16f / 500f;
    private int base, texture = -1;
    private float[] rgbComponents = new float[4];
    private double[] matrix = new double[16];
    private int xAnchor, yAnchor;
    private float scale = 1f;

    public TextureFont() {
        matrix[0] = matrix[5] = matrix[10] = matrix[15] = 1f;
    }

    public void ensureInitialized(GL gl) {
        if (texture < 0) {
            init(gl);
        }
    }

    public void init(GL gl) {
        try {
            texture = TextureLoader.getTextures(gl, "/TextureFont.png")[0];
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load font", e);
        }
        // create the list
        base = gl.glGenLists(CHAR_COUNT);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        // build the caracter list
        for (int walk = 0; walk < CHAR_COUNT; walk++) {
            float cx = (walk % TEXTURE_STEPI) / TEXTURE_STEPF;    // X Position Of Current Character
            float cy = 1 - (walk / TEXTURE_STEPI) / TEXTURE_STEPF;    // Y Position Of Current Character
            gl.glNewList(base + walk, GL.GL_COMPILE);
            gl.glBegin(GL.GL_QUADS);
            gl.glNormal3f(0, 0, 1f);
            gl.glTexCoord2f(cx, cy - TEXTURE_PART);   // TextureLoader Coord (Bottom Left)
            gl.glVertex2f(0, 0);    // Vertex Coord (Bottom Left)
            gl.glTexCoord2f(cx + TEXTURE_PART, cy - TEXTURE_PART);   // TextureLoader Coord (Bottom Right)
            gl.glVertex2f(VERTEX_STEP, 0);    // Vertex Coord (Bottom Right)
            gl.glTexCoord2f(cx + TEXTURE_PART, cy);   // TextureLoader Coord (Top Right)
            gl.glVertex2f(VERTEX_STEP, VERTEX_STEP);    // Vertex Coord (Top Right)
            gl.glTexCoord2f(cx, cy);   // TextureLoader Coord (Top Left)
            gl.glVertex2f(0, VERTEX_STEP);    // Vertex Coord (Top Left)
            gl.glEnd(); // Done Building Our Quad (Character)
            gl.glTranslatef(VERTEX_STEP, 0, 0);    // Move To The Right Of The Character
            gl.glEndList();
        }
    }

    public void display(GL gl, String text, Color color) {
        gl.glDisable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_TEXTURE_2D);
        color.getRGBComponents(rgbComponents);
        gl.glColor3fv(rgbComponents, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        gl.glListBase(base - 32);
        gl.glPushMatrix();
        gl.glMultMatrixd(matrix, 0);
        gl.glScalef(scale, scale, scale);
        float xTranslation = 0f;
        switch (xAnchor) {
            case -1:
                break;
            case 0:
                xTranslation = -VERTEX_STEP * text.length() / 2;
                break;
            case 1:
                xTranslation = -VERTEX_STEP * text.length();
                break;
        }
        float yTranslation = -yAnchor * VERTEX_STEP / 2;
        gl.glTranslatef(xTranslation, yTranslation - VERTEX_STEP / 2, 0);
        gl.glCallLists(text.length(), GL.GL_BYTE, ByteBuffer.wrap(text.getBytes()));
        gl.glPopMatrix();
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_LIGHTING);
    }

    public void setLocation(Arrow location) {
        matrix[12] = location.x;
        matrix[13] = location.y;
        matrix[14] = location.z;
    }

    public void setOrientation(Arrow forward, Arrow up) {
        matrix[0] = - forward.z * up.y + forward.y * up.z;
        matrix[1] = - forward.x * up.z + forward.z * up.x;
        matrix[2] = - forward.y * up.x + forward.x * up.y;
        matrix[4] = up.x;
        matrix[5] = up.y;
        matrix[6] = up.z;
        matrix[8] = -forward.x;
        matrix[9] = -forward.y;
        matrix[10] = -forward.z;
    }

    public void setAnchor(int xAnchor, int yAnchor) {
        this.xAnchor = xAnchor;
        this.yAnchor = yAnchor;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

}
