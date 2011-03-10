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

public class SimpleFabricFactory {
    private Fabric fabric;

    public SimpleFabricFactory(Thing.Factory thingFactory) {
        this.fabric = new Fabric(thingFactory);
    }

    public Fabric createDoubleFaceTriangle() {
        double radius = Math.sqrt(3.0 / 4.0) * 2 / 3;
        for (int walk = 0; walk < 3; walk++) {
            fabric.joints.add(new Joint(fabric.who().createMiddle()));
            double angle = walk * Math.PI * 2 / 3;
            fabric.joints.get(walk).location.set(radius * Math.cos(angle), 0, radius + radius * Math.sin(angle));
        }
        for (int walk = 0; walk < 3; walk++) {
            fabric.intervals.add(new Interval(fabric.joints.get(walk), fabric.joints.get((walk + 1) % 3), Interval.Role.SPRING));
            fabric.intervals.get(walk).span.setIdeal(1, 0);
        }
        for (Face.Order order : Face.Order.values()) {
            Face face = new Face(order);
            face.joints.addAll(fabric.joints);
            fabric.faces.add(face);
            if (fabric.getThingFactory() != null) {
                face.setThing(fabric.getThingFactory().createFresh(face, order.toString()));
            }
        }
        return fabric;
    }

    public Fabric createOctahedron() {
        double radius = Math.sqrt(2) / 2;
        for (int walk = 0; walk < 4; walk++) {
            fabric.joints.add(new Joint(fabric.who().createMiddle()));
            double angle = walk * Math.PI / 2 + Math.PI / 4;
            fabric.joints.get(walk).location.set(radius * Math.cos(angle), 0, radius + radius * Math.sin(angle));
        }
        Who leftWho;
        fabric.joints.add(new Joint(leftWho = fabric.who().createLeft()));
        fabric.joints.get(4).location.set(0, -radius, radius);
        fabric.joints.add(new Joint(leftWho.createOpposite()));
        fabric.joints.get(5).location.set(0, radius, radius);
        for (int walk = 0; walk < 4; walk++) {
            fabric.intervals.add(new Interval(fabric.joints.get(walk), fabric.joints.get((walk + 1) % 4), Interval.Role.SPRING));
            fabric.intervals.add(new Interval(fabric.joints.get(walk), fabric.joints.get(4), Interval.Role.SPRING));
            fabric.intervals.add(new Interval(fabric.joints.get(walk), fabric.joints.get(5), Interval.Role.SPRING));
        }
        for (int walk = 0; walk < 4; walk++) {
            Face faceClock = new Face(Face.Order.LEFT_HANDED);
            Face faceAntiClock = new Face(Face.Order.RIGHT_HANDED);
            faceClock.joints.add(fabric.joints.get(4));
            faceAntiClock.joints.add(fabric.joints.get(5));
            faceClock.joints.add(fabric.joints.get(walk));
            faceAntiClock.joints.add(fabric.joints.get(walk));
            faceClock.joints.add(fabric.joints.get((walk + 1) % 4));
            faceAntiClock.joints.add(fabric.joints.get((walk + 1) % 4));
            fabric.faces.add(faceClock);
            fabric.faces.add(faceAntiClock);
            if (fabric.getThingFactory() != null) {
                faceClock.setThing(fabric.getThingFactory().createFresh(faceClock, "+" + walk));
                faceAntiClock.setThing(fabric.getThingFactory().createFresh(faceAntiClock, "-" + walk));
            }
        }
        return fabric;
    }

    public Fabric createTruss() {
        Joint tz = middleJoint(0, 0, 0.5);
        Joint tp = middleJoint(1, 0, 0.5);
        Joint bp = middleJoint(1, 0, -0.5);
        Joint bz = middleJoint(0, 0, -0.5);
        Joint bn = middleJoint(-1, 0, -0.5);
        Joint tn = middleJoint(-1, 0, 0.5);
        Interval tztp = spring(tz, tp);
        Interval tpbp = spring(tp, bp);
        Interval bpbz = spring(bp, bz);
        Interval bzbn = spring(bz, bn);
        Interval bntn = spring(bn, tn);
        Interval tntz = spring(tn, tz);
        spring(tz, bz);
        double rad = Math.sqrt(2) / 2;
        Joint lp = leftJoint(0.5, rad, 0);
        Joint rp = rightJoint(0.5, -rad, 0);
        Joint ln = leftJoint(-0.5, rad, 0);
        Joint rn = rightJoint(-0.5, -rad, 0);
        spring(tz, lp);
        spring(tp, lp);
        spring(bp, lp);
        spring(bz, lp);
        spring(tz, rp);
        spring(tp, rp);
        spring(bp, rp);
        spring(bz, rp);
        spring(bz, ln);
        spring(bn, ln);
        spring(tn, ln);
        spring(tz, ln);
        spring(bz, rn);
        spring(bn, rn);
        spring(tn, rn);
        spring(tz, rn);
        Interval lpln = spring(lp, ln);
        Interval rprn = spring(rp, rn);
        face(Face.Order.RIGHT_HANDED, tz, tp, rp).setStressInterval(tztp);
        face(Face.Order.RIGHT_HANDED, tp, bp, rp).setStressInterval(tpbp);
        face(Face.Order.RIGHT_HANDED, bp, bz, rp).setStressInterval(bpbz);
        face(Face.Order.LEFT_HANDED, tz, tp, lp).setStressInterval(tztp);
        face(Face.Order.LEFT_HANDED, tp, bp, lp).setStressInterval(tpbp);
        face(Face.Order.LEFT_HANDED, bp, bz, lp).setStressInterval(bpbz);
        face(Face.Order.LEFT_HANDED, tz, tn, rn).setStressInterval(tntz);
        face(Face.Order.LEFT_HANDED, tn, bn, rn).setStressInterval(bntn);
        face(Face.Order.LEFT_HANDED, bn, bz, rn).setStressInterval(bzbn);
        face(Face.Order.RIGHT_HANDED, tz, tn, ln).setStressInterval(tntz);
        face(Face.Order.RIGHT_HANDED, tn, bn, ln).setStressInterval(bntn);
        face(Face.Order.RIGHT_HANDED, bn, bz, ln).setStressInterval(bzbn);
        face(Face.Order.LEFT_HANDED, tz, rn, rp).setStressInterval(rprn);
        face(Face.Order.LEFT_HANDED, bz, ln, lp).setStressInterval(lpln);
        face(Face.Order.RIGHT_HANDED, tz, ln, lp).setStressInterval(lpln);
        face(Face.Order.RIGHT_HANDED, bz, rn, rp).setStressInterval(rprn);
        int countA = 0, countB = 0;
        for (Face face : fabric.faces) {
            switch (face.order) {
                case LEFT_HANDED:
                    face.thing = fabric.getThingFactory().createFresh(face, "+" + countA++);
                    break;
                case RIGHT_HANDED:
                    face.thing = fabric.getThingFactory().createFresh(face, "-" + countB++);
                    break;
            }
        }
        return fabric;
    }

    public Fabric createVertebra(Face.Chirality chirality) {
        double height = 1;
        double radius = 1;
        List<Joint> joints;
        for (int hex = 0; hex < 2; hex++) {
            joints = new ArrayList<Joint>();
            for (int walk = 0; walk < 6; walk++) {
                double angle = walk * Math.PI / 3;
                joints.add(middleJoint(radius * Math.sin(angle), radius * Math.cos(angle), height * hex));
            }
            for (int walk = 0; walk < 6; walk++) {
                Interval cable = cable(joints.get(walk), joints.get((walk + 1) % joints.size()));
                cable.getSpan().setIdeal(cable.getSpan().getActual() * 0.5, 100);
            }
            Face face = faceSprings(Face.Order.values()[hex], joints.get(5 - hex), joints.get(3 - hex), joints.get(1 - hex));
            for (Joint joint : face.joints) {
                joint.location.z = joint.location.z + (hex == 0 ? 0.25 : -0.25);
            }
        }
        joints = fabric.joints;
        for (int walk = 0; walk < 6; walk++) {
            Interval cable = cable(joints.get(walk), joints.get(walk + 6));
            cable.getSpan().setIdeal(cable.getSpan().getActual() * 0.9, 100);
            if (walk % 2 == 0) {
                bar(joints.get(walk), joints.get(6 + (walk + 1 + chirality.ordinal() * 4) % 6));
            }
        }
        return fabric;
    }

    private Face face(Face.Order order, Joint joint0, Joint joint1, Joint joint2) {
        Face face = new Face(order);
        face.joints.add(joint0);
        face.joints.add(joint1);
        face.joints.add(joint2);
        fabric.faces.add(face);
        return face;
    }

    private Face faceSprings(Face.Order order, Joint joint0, Joint joint1, Joint joint2) {
        spring(joint0, joint1);
        spring(joint1, joint2);
        spring(joint2, joint0);
        return face(order, joint0, joint1, joint2);
    }

    private Interval spring(Joint joint0, Joint joint1) {
        Interval spring = new Interval(joint0, joint1, Interval.Role.SPRING);
        fabric.intervals.add(spring);
        return spring;
    }

    private Interval cable(Joint joint0, Joint joint1) {
        Interval cable = new Interval(joint0, joint1, Interval.Role.CABLE);
        fabric.intervals.add(cable);
        return cable;
    }

    private Interval bar(Joint joint0, Joint joint1) {
        Interval bar = new Interval(joint0, joint1, Interval.Role.BAR);
        fabric.intervals.add(bar);
        return bar;
    }

    private Joint middleJoint(double x, double y, double z) {
        Joint joint = new Joint(fabric.who().createMiddle(), new Arrow(x, y, z));
        fabric.joints.add(joint);
        return joint;
    }

    private Joint leftJoint(double x, double y, double z) {
        Joint joint = new Joint(fabric.who().createLeft(), new Arrow(x, y, z));
        fabric.joints.add(joint);
        return joint;
    }

    private Joint rightJoint(double x, double y, double z) {
        Joint joint = new Joint(fabric.who().createRight(), new Arrow(x, y, z));
        fabric.joints.add(joint);
        return joint;
    }

}