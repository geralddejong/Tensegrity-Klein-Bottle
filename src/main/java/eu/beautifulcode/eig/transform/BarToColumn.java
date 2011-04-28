/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.transform;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Interval;
import eu.beautifulcode.eig.structure.Joint;
import eu.beautifulcode.eig.structure.Physics;

import java.util.Map;
import java.util.TreeMap;

/**
 * Open up a triangular face like a book creating two new faces and moving the original
 * back cover of the book to be front cover.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class BarToColumn implements Fabric.Transformation {
    private static final int TICKS_TO_IDEAL = 500;
    private Map<Interval.Role, Physics.Value> spanMap;
    private Interval bar;
    private Interval[][] bars;
    private Arrow mid = new Arrow();
    private Arrow alpha = new Arrow();
    private Arrow omega = new Arrow();
    private int length, circumference;

    public BarToColumn(Interval bar, int length, int circumference) {
        this.bar = bar;
        this.length = length;
        this.circumference = circumference;
        this.bars = new Interval[length][circumference];
    }

    public void setSpanMap(Map<Interval.Role, Physics.Value> spanMap) {
        this.spanMap = spanMap;
    }

    public void transform(Fabric fabric) {
        double ideal = bar.getSpan().getUltimateIdeal();
        double extend = ideal / length / 2;
        Arrow unit = bar.getUnit(true);
        for (int along = 0; along < length; along++) {
            mid.interpolate(bar.get(false).getLocation(), bar.get(true).getLocation(), along / (double) length);
            alpha.set(mid).add(unit, -extend);
            omega.set(mid).add(unit, extend);
            for (int count = 0; count < circumference; count++) {
                fabric.getMods().getIntervalMod().add(
                        bars[along][count] = fabric.createInterval(
                                fabric.getMods().getJointMod().add(
                                        fabric.createJoint(
                                                fabric.who().createAnotherLike(bar.get(false).getWho()),
                                                alpha
                                        )
                                ),
                                fabric.getMods().getJointMod().add(
                                        fabric.createJoint(
                                                fabric.who().createAnotherLike(bar.get(true).getWho()),
                                                omega
                                        )
                                ),
                                Interval.Role.BAR
                        )
                );
            }
        }
        for (int along = 0; along < length-1; along++) {
            for (int count = 0; count < circumference; count++) {
                Interval b = bars[along][count];
                Interval nextB = bars[along][(count + 1) % circumference];
                Interval prevB = bars[along][(count + circumference - 1) % circumference];
                Joint nextP = connect(fabric, b.get(false), b.get(true), nextB.get(false), nextB.get(true));
                Joint prevP = connect(fabric, b.get(true), b.get(false), prevB.get(true), prevB.get(false));
            }
        }
    }

    private Joint connect(Fabric fabric, Joint a0, Joint a1, Joint b0, Joint b1) {
        mid.interpolate(a0.getLocation(), a1.getLocation(), 0.75);
        Joint p = fabric.createJoint(a1.getWho(), mid);
        fabric.getMods().getJointMod().add(p);
        Interval zig = fabric.createInterval(a0, p, Interval.Role.FAR);
        fabric.getMods().getIntervalMod().add(zig);
        Interval ringBar = fabric.createInterval(a1, p, Interval.Role.RING);
        fabric.getMods().getIntervalMod().add(ringBar);
        Interval ring = fabric.createInterval(p, b1, Interval.Role.RING);
        fabric.getMods().getIntervalMod().add(ring);
        Interval zag = fabric.createInterval(a1, b0, Interval.Role.FAR);
        fabric.getMods().getIntervalMod().add(zag);
        return p;
    }

//    private List<Joint> createRing(Fabric fabric) {
//        joints = new ArrayList<Joint>();
//        double radius = ringSize / 10.0;
//        for (int walk = 0; walk < ringSize; walk++) {
//            double angle = walk * 2 * Math.PI / ringSize;
//            Joint joint = fabric.createJoint(fabric.who().createMiddle(), new Arrow(radius * Math.cos(angle), radius * Math.sin(angle), 0));
//            joints.add(joint);
//            fabric.getMods().getJointMod().add(joint);
//        }
//        Ring ring = new Ring(joints, true);
//        for (int walk = 0; walk < joints.size(); walk++) {
//            boolean even = walk % 2 == 0;
//            if (!even) {
//                joints.get(walk).getLocation().add(ring.getNormal(), 0.03);
//            }
//            Interval ringCable = fabric.createInterval(joints.get(walk), joints.get((walk + 1) % ringSize), even ? Interval.Role.RINGBAR : Interval.Role.RING);
//            setIdeal(ringCable);
//            fabric.getMods().getIntervalMod().add(ringCable);
//            if (even) {
//                Interval spring = fabric.createInterval(joints.get(walk), joints.get((walk + 2) % ringSize), Interval.Role.SCAFFOLD);
//                setIdeal(spring);
//                fabric.getMods().getIntervalMod().add(spring);
//            }
//            else {
//                Interval safety = fabric.createInterval(joints.get(walk), joints.get((walk + 2) % ringSize), Interval.Role.HORIZ);
//                setIdeal(safety);
//                fabric.getMods().getIntervalMod().add(safety);
//            }
//        }
//        return joints;
//    }

    private void setIdeal(Interval interval) {
        interval.getSpan().setIdeal(value(interval.getRole()).get(), TICKS_TO_IDEAL);
    }

    private Physics.Value value(Interval.Role role) {
        if (spanMap == null) {
            spanMap = new TreeMap<Interval.Role, Physics.Value>();
        }
        Physics.Value value = spanMap.get(role);
        if (value == null) {
            switch (role) { // defaults
                case RING:
                    value = new Val(role, 0.6);
                    break;
                case SCAFFOLD:
                    value = new Val(role, 1.3);
                    break;
                case FAR:
                    value = new Val(role, 0.4);
                    break;
                case CROSS:
                    value = new Val(role, 1.7);
                    break;
                case BAR:
                    value = new Val(role, 1.7);
                    break;
                default:
                    throw new RuntimeException("Unknown: " + role);
            }
            spanMap.put(role, value);
        }
        return value;
    }

    private class Val implements Physics.Value {
        private Interval.Role role;
        private double value;

        private Val(Interval.Role role, double value) {
            this.role = role;
            this.value = value;
        }

        public String getName() {
            return role.toString();
        }

        public void set(double value) {
            this.value = value;
        }

        public double get() {
            return value;
        }
    }
}