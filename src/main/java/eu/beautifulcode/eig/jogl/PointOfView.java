/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.jogl;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.math.Space3;


/**
 * This class takes a few arrows and makes a point of view.
 * The eye is where we are, up points upwards and focus is the
 * point in space that we are looking at.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class PointOfView {
    private static final double MIN_DISTANCE = 0.1;
    private Arrow eye = new Arrow(1, 0, 1);
    private Arrow focus = new Arrow(0, 0, 1);
    private Arrow up = new Arrow(0, 0, 1);

    private Space3 rotation = new Space3();
    private Arrow gaze = new Arrow();
    private Arrow right = new Arrow();
    private Arrow move = new Arrow();

    public PointOfView(double distanceFromOrigin) {
        this(distanceFromOrigin, 1);
    }

    public PointOfView(double distanceFromOrigin, double altitude) {
        eye.x = distanceFromOrigin;
        eye.z = focus.z = altitude;
        update();
    }

    public Arrow getEye() {
        return eye;
    }

    public void getEye(double rightDistance, Arrow eye) {
        eye.set(right);
        eye.scale(rightDistance);
        eye.add(this.eye);
    }

    public Arrow getFocus() {
        return focus;
    }

    public void moveFocusTowardsIdeal(Arrow idealFocus, double travelDistance) {
        double distance = update();
        if (distance - travelDistance < MIN_DISTANCE) {
            travelDistance = distance - MIN_DISTANCE;
        }
        move.set(idealFocus);
        move.sub(this.focus);
        double jump = move.span();
        if (jump > 0.0001) {
            if (travelDistance > jump) {
                travelDistance = jump;
            }
            move.setSpan(travelDistance);
            this.focus.add(move);
            move.sub(this.eye, this.focus);
            double newDistance = move.span();
            double distanceToMove = distance - newDistance;
            move.setSpan(distanceToMove);
            eye.add(move);
            update();
        }
    }

    public Arrow getUp() {
        return up;
    }

    public Arrow getGaze() {
        return gaze;
    }

    public Arrow getRight() {
        return right;
    }

    public double getDistance() {
        return update();
    }

    public void goForward(double travelDistance) {
        update();
        move.set(gaze);
        move.scale(travelDistance);
        eye.add(move);
        focus.add(move);
        update();
    }

    public void setDistanceFromFocus(double distance) {
        double actualDistance = update();
        eye.sub(gaze, distance - actualDistance);
        update();
    }

    public void setDistanceFromEye(double distance) {
        double actualDistance = update();
        focus.add(gaze, distance - actualDistance);
        update();
    }

    public void goToFocus(double travelDistance) {
        double distance = update();
        if (distance - travelDistance < MIN_DISTANCE) {
            travelDistance = distance - MIN_DISTANCE;
        }
        move.set(gaze);
        move.scale(travelDistance);
        eye.add(move);
        update();
    }

    public void goUp(double dist) {
        update();
        move.set(up);
        move.scale(dist);
        eye.add(move);
        focus.add(move);
        update();
    }

    public void goRight(double dist) {
        update();
        move.set(right);
        move.scale(dist);
        eye.add(move);
        focus.add(move);
    }

    public void rotateX(double angle) {
        update();
        rotation.set(up, angle);
        focus.sub(eye);
        rotation.transform(focus);
        focus.add(eye);
        update();
    }

    public void rotateY(double angle) {
        update();
        rotation.set(right, angle);
        focus.sub(eye);
        rotation.transform(focus);
        rotation.transform(up);
        focus.add(eye);
        update();
    }

    public void focusRotateX(double angle) {
        update();
        rotation.set(up, angle);
        eye.sub(focus);
        rotation.transform(eye);
        eye.add(focus);
        update();
    }

    public void focusRotateY(double angle) {
        update();
        rotation.set(right, angle);
        eye.sub(focus);
        rotation.transform(eye);
        rotation.transform(up);
        eye.add(focus);
        update();
    }

    public void adjustUp(Arrow idealUp, double moderation) {
        double distance = update();
        move.set(idealUp);
        move.scale(moderation);
        up.add(move);
        up.normalize();
        perpendicular(up, right);
        perpendicular(up, gaze);
        focus.set(eye).add(gaze,distance);
        update();
    }

    public void horizontalize(double moderation) {
        double distance = update();
        up.z += 0.3 * moderation;
        up.normalize();
        perpendicular(up, right);
        perpendicular(up, gaze);
        perpendicular(right, gaze);
        move.set(gaze);
        move.scale(distance);
        move.add(eye);
        focus.set(move);
    }

    public double update() {
        gaze.sub(focus, eye);
        double distance = gaze.normalize();
        perpendicular(gaze, up);
        right.cross(gaze, up);
        perpendicular(up, right);
        return distance;
    }

    public void perpendicular(Arrow base, Arrow change) {
        change.sub(base, base.dot(change));
        change.normalize();
    }

    public String toString() {
        return eye.toString();
    }

}
