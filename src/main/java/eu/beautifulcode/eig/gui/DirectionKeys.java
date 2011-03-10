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

public class DirectionKeys implements KeyListener {
    private double step = 0.0006;
    private KeyEvent upPressed, downPressed, leftPressed, rightPressed;
    private Dir downUp = new Dir(), leftRight = new Dir(), forwardBackward = new Dir();

    public void setStep(double step) {
        this.step = step;
    }

    public void keyTyped(KeyEvent event) {
    }

    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_UP:
                upPressed = event;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = event;
                break;
            case KeyEvent.VK_LEFT:
                leftPressed = event;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = event;
                break;
        }
    }

    public void keyReleased(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_UP:
                upPressed = null;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = null;
                break;
            case KeyEvent.VK_LEFT:
                leftPressed = null;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = null;
                break;
        }
    }

    public void run() {
        boolean combo = false;
        if (upPressed != null && downPressed != null) {
            forwardBackward = new Dir(forwardBackward, true);
            combo = true;
        }
        else if (upPressed != null) {
            downUp = new Dir(downUp, false);
        }
        else if (downPressed != null) {
            downUp = new Dir(downUp, true);
        }
        else {
            downUp = new Dir(downUp, null);
        }
        if (!(upPressed != null && downPressed != null) && leftPressed != null && rightPressed != null) {
            forwardBackward = new Dir(forwardBackward, false);
            combo = true;
        }
        else if (leftPressed != null) {
            leftRight = new Dir(leftRight, false);
        }
        else if (rightPressed != null) {
            leftRight = new Dir(leftRight, true);
        }
        else {
            leftRight = new Dir(leftRight, null);
        }
        if (combo) {
            downUp = new Dir(downUp, null);
            leftRight = new Dir(leftRight, null);
        }
        else {
            forwardBackward = new Dir(forwardBackward, null);
        }
    }

    public Dir getDownUp() {
        return downUp;
    }

    public Dir getLeftRight() {
        return leftRight;
    }

    public Dir getForwardBackward() {
        return forwardBackward;
    }

    public class Dir {
        private long since;
        private double value, step, destination;

        public Dir() {
            this.since = System.currentTimeMillis();
        }

        public Dir(Dir previous, Boolean higher) {
            this.value = previous.getValue();
            this.since = System.currentTimeMillis();
            if (higher != null) {
                step = higher ? DirectionKeys.this.step : -DirectionKeys.this.step;
                destination = higher ? 1 : -1;
            }
            else {
                if (value < 0) {
                    step = DirectionKeys.this.step * 3;
                }
                else if (value > 0) {
                    step = -DirectionKeys.this.step * 3;
                }
                destination = 0;
            }
        }

        public double getValue() {
            long elapsed = System.currentTimeMillis() - since;
            double valueNow = value + step * elapsed;
            if (step < 0) {
                if (valueNow < destination) {
                    valueNow = destination;
                }
            }
            else if (step > 0) {
                if (valueNow > destination) {
                    valueNow = destination;
                }
            }
            return valueNow;
        }

        public String toString() {
            return value + ":" + step + ":" + destination + ":" + getValue();
        }
    }
}