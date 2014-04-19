/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 * Something to be seen in a GLViewPlatform will have to implement these functions.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface GLRenderer {

    /**
     * Prepare to do the drawing, creating GL shapes for example.
     *
     * @param gl the graphics context
     */

    void init(GL2 gl);

    /**
     * Display the things you have to show.
     *
     * @param gl the graphics context
     * @param width of canvas
     * @param height canvas
     */

    void display(GL2 gl, int width, int height);

}
