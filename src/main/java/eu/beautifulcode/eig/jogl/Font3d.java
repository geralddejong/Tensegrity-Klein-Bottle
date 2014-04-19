// ========= Copyright (C) 2009, 2010 Gerald de Jong =================
// This file is part of the Darwin at Home project, distributed
// under the GNU General Public License, version 3.
// You should have received a copy of this license in "license.txt",
// but if not, see http://www.gnu.org/licenses/gpl-3.0.txt.
// ===================================================================
package eu.beautifulcode.eig.jogl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;
import eu.beautifulcode.eig.math.Arrow;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

/**
 * Use the text renderer to put text in space
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class Font3d {
    private static final float SCALE = 0.002f;
    private TextRenderer textRenderer = new TextRenderer(new Font("Dialog", Font.PLAIN, 15), true, true);
    private double[] matrix = new double[16];
    private int xAnchor, yAnchor;
    private float scale = 1f;

    public Font3d() {
        matrix[0] = matrix[5] = matrix[10] = matrix[15] = 1f;
    }

    public void setLocation(Arrow location) {
        matrix[12] = location.x;
        matrix[13] = location.y;
        matrix[14] = location.z;
    }

    public void setOrientation(Arrow forward, Arrow up) {
        matrix[0] = -forward.z * up.y + forward.y * up.z;
        matrix[1] = -forward.x * up.z + forward.z * up.x;
        matrix[2] = -forward.y * up.x + forward.x * up.y;
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

    public void display(GL2 gl, String text, java.awt.Color color) {
        gl.glPushMatrix();
        gl.glMultMatrixd(matrix, 0);
        gl.glScalef(scale, scale, scale);
        textRenderer.begin3DRendering();
        textRenderer.setColor(color);
        Rectangle2D rect = textRenderer.getBounds(text);
        float xTranslation = 0;
        switch (xAnchor) {
            case -1:
                break;
            case 0:
                xTranslation = (float) (-rect.getWidth() / 2);
                break;
            case 1:
                xTranslation =  (float) -rect.getWidth();
                break;
        }
        textRenderer.draw3D(text, SCALE * xTranslation, -SCALE * (float) (yAnchor * rect.getHeight() / 2), 0, SCALE);
        textRenderer.end3DRendering();
        gl.glPopMatrix();
    }
}
