/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.gui;

import eu.beautifulcode.eig.jogl.PointOfView;

import java.awt.event.KeyListener;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
public class Vehicle implements Runnable {
    private static final int MOVE = 0;
    private static final int FORWARD = 1;
    private static final int RIGHT = 2;
    private static final int UP = 3;
    private static final int AROUND = 4;
    private static final double DECELERATION = 0.1;
    private static final double ROTATE_ANGLE = 0.05;
    private static final double MIN_ALTITUDE = 0.1;
    private static final double MAX_EYE_ALTITUDE = 5;
    private static final double [] ACCELERATION = { 0, 0.1, 0.01, 0.02 };
    private PointOfView pov;
    private KeyHandler keyHandler = new KeyHandler();
    private double forward, right, up;

    public Vehicle(PointOfView pov) {
        this.pov = pov;
    }

    public KeyListener getKeyListener() {
        return keyHandler;
    }

    public void accelerateForward(boolean direction) {
        if (direction) {
            forward += getAcceleration(FORWARD);
        }
        else {
            forward -= getAcceleration(FORWARD);
        }
    }

    public void accelerateRight(boolean direction) {
        if (direction) {
            right += getAcceleration(RIGHT);
        }
        else {
            right -= getAcceleration(RIGHT);
        }
    }

    public void accelerateUp(boolean direction) {
        if (direction) {
            up += getAcceleration(UP);
        }
        else {
            up -= getAcceleration(UP);
        }
    }

    public void turnRight(boolean direction) {
        pov.rotateX(ROTATE_ANGLE * (direction?-1:1) * keyHandler.getModeration(AROUND));
    }

    public void run() {
        double moderation = keyHandler.getModeration(MOVE);
        pov.goForward(forward * moderation);
        pov.goRight(right * moderation);
        if (pov.getEye().z > MAX_EYE_ALTITUDE) {
            pov.getEye().z = MAX_EYE_ALTITUDE;
            if (pov.getGaze().z < 0.5) {
                pov.rotateY(ROTATE_ANGLE/2 * moderation);
            }
            up = 0;
        }
        else if (pov.getEye().z < MIN_ALTITUDE) {
            pov.getEye().z = MIN_ALTITUDE;
            up = 0;
        }
        pov.goUp(up * moderation);
        keyHandler.act();
        // deceleration
        forward *= (1-DECELERATION*moderation);
        right *= (1-DECELERATION*moderation);
        up *= (1-DECELERATION*moderation);
        pov.horizontalize(moderation);
    }

    private double getAcceleration(int index) {
        return ACCELERATION[index] * keyHandler.getModeration(index);
    }

    private class KeyHandler extends AbstractKeyHandler {

        private KeyHandler() {
            super(5);
        }

        public void act() {
            if (up != null) {
                if (up.isShiftDown()) {
                    accelerateUp(true);
                }
                else {
                    accelerateForward(true);
                }
            }
            if (down != null) {
                if (down.isShiftDown()) {
                    accelerateUp(false);
                }
                else {
                    accelerateForward(false);
                }
            }
            if (left != null) {
                if (left.isShiftDown()) {
                    accelerateRight(false);
                }
                else {
                    turnRight(false);
                }
            }
            if (right != null) {
                if (right.isShiftDown()) {
                    accelerateRight(true);
                }
                else {
                    turnRight(true);
                }
            }
        }
    }

}
