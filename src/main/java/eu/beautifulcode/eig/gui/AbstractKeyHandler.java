/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
public abstract class AbstractKeyHandler implements KeyListener {
    private static final long MAX_ELAPSED = 100;
    protected KeyEvent up, down, left, right;
    private long [] time;

    public AbstractKeyHandler(int actionCount) {
        time = new long[actionCount];
    }

    public double getModeration(int index) {
        long now = System.currentTimeMillis();
        long elapsed = now - time[index];
        time[index] = now;
        if (elapsed > MAX_ELAPSED) {
            elapsed = 0;
        }
        return ((double)elapsed)/((double)MAX_ELAPSED);
    }

    public void keyTyped(KeyEvent event) {
    }

    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (up == null) {
                    press();
                }
                up = event;
                break;
            case KeyEvent.VK_DOWN:
                if (down == null) {
                    press();
                }
                down = event;
                break;
            case KeyEvent.VK_LEFT:
                if (left == null) {
                    press();
                }
                left = event;
                break;
            case KeyEvent.VK_RIGHT:
                if (right == null) {
                    press();
                }
                right = event;
                break;
        }
    }

    public void keyReleased(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_UP:
                up = null;
                break;
            case KeyEvent.VK_DOWN:
                down = null;
                break;
            case KeyEvent.VK_LEFT:
                left = null;
                break;
            case KeyEvent.VK_RIGHT:
                right = null;
                break;
        }
    }

    private void press() {
        for (int walk = 0; walk<time.length; walk++) {
            time[walk] = System.currentTimeMillis();
        }
    }
}
