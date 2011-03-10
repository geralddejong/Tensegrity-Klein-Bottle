/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.gui;

import eu.beautifulcode.eig.jogl.PointOfView;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Queue;

/**
 * Position with respect to focus using keyboard and mouse wheel
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
public class Positioner {
    private static final int VERTICAL = 0;
    private static final int FORWARD = 1;
    private static final int ROTATE = 2;
    private static final int HORIZONTALIZE = 3;
    private static final double MOVE_DISTANCE = 0.2;
    private Queue<Runnable> jobs;
    private PointOfView pointOfView;
    private KeyHandler keyHandler = new KeyHandler();
    private MouseHandler mouseHandler = new MouseHandler();

    public Positioner(Queue<Runnable> jobs, PointOfView pointOfView) {
        this.jobs = jobs;
        this.pointOfView = pointOfView;
    }

    public void setSpin(boolean spin) {
        keyHandler.holdLeft = spin;
    }

    public KeyListener getKeyListener() {
        return keyHandler;
    }

    public MouseListener getMouseListener() {
        return mouseHandler;
    }

    public MouseMotionListener getMouseMotionListener() {
        return mouseHandler;
    }

    public MouseWheelListener getMouseWheelListener() {
        return mouseHandler;
    }

    public void run() {
        keyHandler.act();
    }

    private class KeyHandler extends AbstractKeyHandler {
        private static final double ROTATE_ANGLE = 0.04;
        private boolean holdLeft, holdRight;

        private KeyHandler() {
            super(4);
        }

        public void act() {
            pointOfView.horizontalize(getModeration(HORIZONTALIZE));
            if (up != null && down == null) {
                if (up.isShiftDown()) {
                    pointOfView.goUp(MOVE_DISTANCE * getModeration(VERTICAL));
                }
                else {
                    pointOfView.goToFocus(MOVE_DISTANCE * getModeration(FORWARD));
                }
            }
            if (down != null && up == null) {
                if (down.isShiftDown()) {
                    pointOfView.goUp(-MOVE_DISTANCE * getModeration(VERTICAL));
                }
                else {
                    pointOfView.goToFocus(-MOVE_DISTANCE * getModeration(FORWARD));
                }
            }
            if (left != null && right == null || holdLeft) {
                if (left != null) {
                    holdLeft = left.isShiftDown();
                    holdRight = false;
                }
                pointOfView.focusRotateX(-ROTATE_ANGLE * getModeration(ROTATE));
            }
            if ((right != null && left == null) || holdRight) {
                if (right != null) {
                    holdRight = right.isShiftDown();
                    holdLeft = false;
                }
                pointOfView.focusRotateX(ROTATE_ANGLE * getModeration(ROTATE));
            }
        }
    }

    private class MouseHandler extends MouseAdapter implements MouseMotionListener, MouseWheelListener {
        private static final double ROTATE_ANGLE = 0.005;
        private MouseEvent anchor;

        @Override
        public void mouseExited(MouseEvent event) {
            anchor = null;
        }

        @Override
        public void mousePressed(MouseEvent event) {
            anchor = event;
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            anchor = null;
        }

        public void mouseDragged(final MouseEvent event) {
            if (anchor == null) return;
            final int dx = event.getX() - anchor.getX();
            final int dy = event.getY() - anchor.getY();
            if (event.isShiftDown()) {
                jobs.add(new Runnable() {
                    public void run() {
                        pointOfView.focusRotateX(-ROTATE_ANGLE * dx / 2);
                        pointOfView.goUp(0.04 * dy);
                    }
                });
            }
            else {
                jobs.add(new Runnable() {
                    public void run() {
                        pointOfView.focusRotateX(-ROTATE_ANGLE * dx / 2);
                        pointOfView.goUp(0.004 * dy);
                    }
                });
            }
            anchor = event;
        }

        public void mouseMoved(final MouseEvent event) {
            Component component = (Component) event.getSource();
            if (!component.hasFocus()) {
                component.requestFocus();
            }
        }

        public void mouseWheelMoved(final MouseWheelEvent event) {
            Component component = (Component) event.getSource();
            if (!component.hasFocus()) {
                component.requestFocus();
            }
            jobs.add(new Runnable() {
                public void run() {
                    int notches = event.getWheelRotation();
                    pointOfView.goToFocus(0.05 * notches);
                }
            });
        }
    }
}
