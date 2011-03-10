package eu.beautifulcode.eig.jogl;

import eu.beautifulcode.eig.math.Arrow;

import javax.media.opengl.GL;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manage picking
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class Mouse3d {
    private Queue<Event> events = new ConcurrentLinkedQueue<Event>();
    private PickRaySource pickRaySource;
    private Adapter adapter = new Adapter();

    public interface PickRaySource {
        void getPickRay(GL gl, double mouseX, double mouseY, Arrow location, Arrow direction);
    }

    public Mouse3d(PickRaySource pickRaySource) {
        this.pickRaySource = pickRaySource;
    }

    public void attachTo(Component component) {
        component.addMouseListener(adapter);
        component.addMouseMotionListener(adapter);
    }

    public Event getEvent(GL gl) {
        if (events.isEmpty()) {
            return null;
        }
        return events.remove().resolve(gl);
    }

    public void clearEvents() {
        events.clear();
    }

    private class Adapter implements MouseListener, MouseMotionListener {

        public void mouseClicked(MouseEvent mouseEvent) {
        }

        public void mousePressed(MouseEvent mouseEvent) {
            fire(true, false, mouseEvent);
        }

        public void mouseReleased(MouseEvent mouseEvent) {
            fire(false, true, mouseEvent);
        }

        public void mouseEntered(MouseEvent mouseEvent) {
        }

        public void mouseExited(MouseEvent mouseEvent) {
        }

        public void mouseDragged(MouseEvent mouseEvent) {
            fire(false, false, mouseEvent);
        }

        public void mouseMoved(MouseEvent mouseEvent) {
        }
    }

    private void fire(boolean press, boolean release, MouseEvent mouseEvent) {
        events.add(new Event(press, release, mouseEvent));
    }

    public class Event {
        private MouseEvent mouseEvent;
        private boolean press;
        private boolean release;
        private Arrow location = new Arrow();
        private Arrow direction = new Arrow();

        public Event(boolean press, boolean release, MouseEvent mouseEvent) {
            this.press = press;
            this.release = release;
            this.mouseEvent = mouseEvent;
        }

        public boolean isPress() {
            return press;
        }

        public boolean isRelease() {
            return release;
        }

        public Arrow getLocation() {
            return location;
        }

        public Arrow getDirection() {
            return direction;
        }

        public Arrow getIntersection(double sphereRadius) {
            double ll = location.dot(location);
            double ld = location.dot(direction);
            double dd = direction.dot(direction);
            double rr = sphereRadius * sphereRadius;
            double det = ld * ld - dd * (ll - rr);
            if (det > 0) {
                double param = (-ld - Math.sqrt(det)) / dd;
                return new Arrow(location).add(direction, param);
            }
            else {
                return null;
            }
        }

        Event resolve(GL gl) {
            pickRaySource.getPickRay(gl, mouseEvent.getX(), mouseEvent.getY(), location, direction);
            return this;
        }
    }
}