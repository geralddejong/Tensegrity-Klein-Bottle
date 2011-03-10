/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

import eu.beautifulcode.eig.math.Arrow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Face {
    Order order;
    Chirality chirality;
    List<Joint> joints = new ArrayList<Joint>();
    Interval stressInterval;
    Thing thing;

    public enum Order {
        RIGHT_HANDED,
        LEFT_HANDED
    }

    public enum Chirality {
        LEFT_HANDED,
        RIGHT_HANDED
    }

    public Face(Order order) {
        this(order, Chirality.LEFT_HANDED);
    }

    public Face(Order order, Chirality chirality) {
        this.order = order;
        this.chirality = chirality;
    }

    public Chirality getChirality() {
        return chirality;
    }

    public void setChirality(Chirality chirality) {
        this.chirality = chirality;
    }

    public Interval getStressInterval() {
        return stressInterval;
    }

    public void setStressInterval(Interval stressInterval) {
        this.stressInterval = stressInterval;
    }

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public List<Joint> getJoints() {
        return joints;
    }

    public List<Interval> getIntervals(Fabric fabric) {
        List<Interval> intervals = new ArrayList<Interval>(joints.size());
        for (int walk=0; walk<joints.size(); walk++) {
            intervals.addAll(fabric.getRealIntervals(joints.get(walk), joints.get((walk + 1) % joints.size())));
        }
        return intervals;
    }

    public void twist(boolean direction) {
        if (direction) {
            joints.add(joints.remove(0));
        }
        else {
            joints.add(0, joints.remove(joints.size()-1));
        }
    }

    public Order getOrder() {
        return order;
    }

    public boolean sameJointsAs(Face face) {
        for (Joint theirJoint : face.joints) {
            if (!joints.contains(theirJoint)) {
                return false;
            }
        }
        return true;
    }

    public void getLocation(Arrow location) {
        location.zero();
        for (Joint joint : joints) {
            location.add(joint.location);
        }
        location.scale(1.0 / joints.size());
    }

    public void getLocation(Arrow location, double... span) {
        if (span.length != 3) {
            throw new IllegalArgumentException("Only works for 3 spans");
        }
        location.zero();
        int index = 0;
        for (Joint joint : joints) {
            location.add(joint.location, span[index++]);
        }
    }

    public Arrow getNormal() {
        Arrow normal = new Arrow();
        Arrow cross = new Arrow();
        Arrow mid = new Arrow();
        Arrow a = new Arrow();
        Arrow b = new Arrow();
        getNormal(normal, mid, cross, a, b);
        return normal;
    }

    public void getNormal(Arrow normal, Arrow mid, Arrow cross, Arrow a, Arrow b) {
        getLocation(mid);
        for (int walk = 0; walk < joints.size(); walk++) {
            Joint jointA = joint(walk);
            Joint jointB = joint(walk+1);
            a.sub(jointA.location, mid);
            b.sub(jointB.location, mid);
            switch (order) {
                case RIGHT_HANDED:
                    cross.cross(b, a);
                    break;
                case LEFT_HANDED:
                    cross.cross(a, b);
                    break;
            }
            double span = cross.span();
            if (span != 0) {
                cross.scale(1 / span);
                normal.add(cross);
            }
        }
        double normalSpan = normal.span();
        if (normalSpan > 0.0001) {
            normal.scale(1/normalSpan);
        }
    }

    public double getRadius() {
        Arrow mid = new Arrow();
        getLocation(mid);
        Arrow ray = new Arrow();
        double raySum = 0;
        for (Joint joint : joints) {
            ray.sub(joint.location, mid);
            raySum += ray.span();
        }
        return raySum / joints.size();
    }

    public Who createApexWho(Who.Factory factory) {
        int [] count = new int[Who.Side.values().length];
        for (Joint joint : joints) {
            count[joint.who.side.ordinal()]++;
        }
        if (count[Who.Side.LEFT.ordinal()] > count[Who.Side.RIGHT.ordinal()]) {
            return factory.createLeft();
        }
        else if (count[Who.Side.LEFT.ordinal()] < count[Who.Side.RIGHT.ordinal()]) {
            return factory.createRight();
        }
        else if (count[Who.Side.MIDDLE.ordinal()] == 3) {
            switch (order) {
                case RIGHT_HANDED:
                    return factory.createRight();
                case LEFT_HANDED:
                    return factory.createLeft();
            }
        }
        return factory.createMiddle();
    }

    Joint joint(int index) {
        index = (index + 3*joints.size()) % joints.size();
        return joints.get(index);
    }

    public boolean replace(Joint from, Joint to) {
        int fromIndex = joints.indexOf(from);
        if (fromIndex < 0) {
            return false;
        }
        joints.set(fromIndex, to);
        return true;
    }

    public String toString() {
        return "Face("+joints.get(0)+":"+joints.get(1)+":"+joints.get(2)+":"+order+")";
    }

    public boolean contains(Joint joint) {
        return joints.contains(joint);
    }

    public boolean contains(Who who) {
        for (Joint joint : joints) {
            if (joint.who.equals(who)) {
                return true;
            }
        }
        return false;
    }

    public static class Pair {
        public final Face face0;
        public final Face face1;

        public Pair(Face face0, Face face1) {
            this.face0 = face0;
            this.face1 = face1;
        }
    }
}