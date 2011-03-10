/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import java.awt.Color;
import java.util.Collection;

/**
 * The way to put heads-up text on the GL canvas
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface HeadsUp {

    int MARGIN = 30;

    public enum Pos {
        MIDDLE,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public interface Line {
        boolean hasChanged();
        String getText();
    }

    void setColor(Color color);

    void set(Pos pos, Collection<? extends Line> lines);

    void set(Pos pos, Line ... lines);

    public static class Fixed implements Line {
        private String text;

        public Fixed(String text) {
            this.text = text;
        }

        public boolean hasChanged() {
            return false;
        }

        public String getText() {
            return text;
        }
    }
}