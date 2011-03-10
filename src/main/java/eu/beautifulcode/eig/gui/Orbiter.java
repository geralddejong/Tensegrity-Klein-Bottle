/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.gui;

import eu.beautifulcode.eig.jogl.PointOfView;
import eu.beautifulcode.eig.math.Space3;

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
public class Orbiter {
    private static final int UP = 0;
    private static final int LEFT = 1;
    private static final double KEY_FACTOR = 0.01;
    private Queue<Runnable> jobs;
    private PointOfView pointOfView;
    private Space3 rotation = new Space3();
    private double radius, minimumRadius;
    private KeyHandler keyHandler = new KeyHandler();
    private MouseHandler mouseHandler = new MouseHandler();

    public Orbiter(Queue<Runnable> jobs, PointOfView pointOfView, double minimumRadius) {
        this.jobs = jobs;
        this.pointOfView = pointOfView;
        this.radius = pointOfView.getEye().span();
        this.minimumRadius = minimumRadius;
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

        private KeyHandler() {
            super(2);
        }

        public void act() {
            rotation.setIdentity();
            if (up != null && down == null) {
                rotation.set(pointOfView.getRight(), -KEY_FACTOR * getModeration(UP));
            }
            if (down != null && up == null) {
                rotation.set(pointOfView.getRight(), KEY_FACTOR * getModeration(UP));
            }
            if (left != null && right == null) {
                rotation.set(pointOfView.getUp(), KEY_FACTOR * getModeration(LEFT));
            }
            if (right != null && left == null) {
                rotation.set(pointOfView.getUp(), -KEY_FACTOR * getModeration(LEFT));
            }
            rotation.transform(pointOfView.getEye());
            rotation.transform(pointOfView.getUp());
            pointOfView.getEye().setSpan(radius);
            pointOfView.getFocus().zero();
            pointOfView.update();
        }
    }

    private class MouseHandler extends MouseAdapter implements MouseMotionListener, MouseWheelListener {
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
            if (dx != 0) {
                jobs.add(new Runnable() {
                    public void run() {
                        rotation.set(pointOfView.getUp(), -0.001 * dx);
                        rotation.transform(pointOfView.getEye());
                        rotation.transform(pointOfView.getUp());
                        pointOfView.getEye().setSpan(radius);
                        pointOfView.getFocus().zero();
                        pointOfView.update();
                    }
                });
            }
            if (dy != 0) {
                jobs.add(new Runnable() {
                    public void run() {
                        rotation.set(pointOfView.getRight(), -0.001 * dy);
                        rotation.transform(pointOfView.getEye());
                        rotation.transform(pointOfView.getUp());
                        pointOfView.getEye().setSpan(radius);
                        pointOfView.getFocus().zero();
                        pointOfView.update();
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
                    radius -= 0.01 * notches;
                    if (radius < minimumRadius) {
                        radius = minimumRadius;
                    }
                }
            });
        }
    }
}