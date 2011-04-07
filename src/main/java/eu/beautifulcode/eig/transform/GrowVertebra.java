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
import eu.beautifulcode.eig.structure.Vertebra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Open up a triangular face like a book creating two new faces and moving the original
 * back cover of the book to be front cover.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class GrowVertebra implements Fabric.Transformation {
    private static final int TICKS_TO_IDEAL = 500;
    private Map<Interval.Role, Physics.Value> spanMap;
    private Vertebra vertebra;
    private boolean rightHanded, zigzag;
    private List<Joint> joints;
    private List<Joint> otherJoints;
    private int ringSize = 10;
    private boolean connecting;

    public GrowVertebra(int barCount) {
        this.ringSize = barCount * 2;
    }

    public GrowVertebra(Vertebra vertebra, boolean alpha, boolean zigzag) {
        this.rightHanded = zigzag ^ vertebra.isRightHanded();
        this.joints = vertebra.getJoints(alpha);
        this.zigzag = zigzag;
    }

    public void setSpanMap(Map<Interval.Role, Physics.Value> spanMap) {
        this.spanMap = spanMap;
    }

    public void transform(Fabric fabric) {
        if (joints == null) {
            createRing(fabric);
            createOtherJoints(fabric, 0.3);
        }
        else {
            removeSprings(joints, fabric);
            if (otherJoints != null) {
                removeSprings(otherJoints, fabric);
                connecting = true;
            }
            else {
                createOtherJoints(fabric, 0.1);
            }
        }
        List<Joint> alpha = rightHanded ? joints : otherJoints;
        List<Joint> omega = rightHanded ? otherJoints : joints;
        Arrow midBar = new Arrow();
        for (int walk = 0; walk < joints.size(); walk++) {
            Interval counterCable = fabric.createInterval(alpha.get(walk), omega.get((walk + 1) % joints.size()), Interval.Role.ACROSS);
            setIdeal(counterCable);
            fabric.getMods().getIntervalMod().add(counterCable);
            if (walk % 2 == 1) {
                Interval bar = fabric.createInterval(alpha.get(walk), omega.get((walk + 2) % joints.size()), Interval.Role.BAR);
                setIdeal(bar);
                fabric.getMods().getIntervalMod().add(bar);
                Interval vertical = fabric.createInterval(alpha.get(walk), omega.get(walk), Interval.Role.VERT);
                setIdeal(vertical);
                fabric.getMods().getIntervalMod().add(vertical);
                // reposition
                bar.getLocation(midBar);
                otherJoints.get((walk + 1) % joints.size()).getLocation().add(midBar);
                otherJoints.get((walk + 1) % joints.size()).getLocation().scale(0.5);
            }
        }
        for (int walk = 0; walk < joints.size(); walk++) {
            boolean even = walk % 2 == 0;
            if (!connecting) {
                if (even) {
                    Interval spring = fabric.createInterval(otherJoints.get(walk), otherJoints.get((walk + 2) % joints.size()), Interval.Role.SCAFFOLD);
                    setIdeal(spring);
                    fabric.getMods().getIntervalMod().add(spring);
                }
                else {
                    Interval safety = fabric.createInterval(otherJoints.get(walk), otherJoints.get((walk + 2) % joints.size()), Interval.Role.HORIZ);
                    setIdeal(safety);
                    fabric.getMods().getIntervalMod().add(safety);
                }
            }
            Interval ringCable = fabric.createInterval(otherJoints.get(walk), otherJoints.get((walk + 1) % joints.size()), even ? Interval.Role.RING : Interval.Role.RINGBAR);
            setIdeal(ringCable);
            fabric.getMods().getIntervalMod().add(ringCable);
        }
        vertebra = new Vertebra(rightHanded);
        vertebra.getJoints().addAll(joints);
        vertebra.getJoints().addAll(otherJoints);
        fabric.getMods().getVertebraMod().add(vertebra);
    }

    public Vertebra getVertebra() {
        return vertebra;
    }

    private void createOtherJoints(Fabric fabric, double displacement) {
        otherJoints = new ArrayList<Joint>();
        Ring ring = new Ring(joints, rightHanded);
        for (Joint joint : joints) {
            Joint newJoint = fabric.createJoint(fabric.who().createAnotherLike(joint.getWho()), joint.getLocation());
            newJoint.getLocation().add(ring.getNormal(), displacement);
            otherJoints.add(newJoint);
            fabric.getMods().getJointMod().add(newJoint);
        }
    }

    private static void removeSprings(List<Joint> jointList, Fabric fabric) {
        for (int walk = 0; walk < jointList.size(); walk++) {
            Interval spring = fabric.getInterval(jointList.get(walk), jointList.get((walk + 2) % jointList.size()));
            if (spring != null) {
                if (spring.getRole() != Interval.Role.SCAFFOLD) {
                    continue;
                }
                fabric.getMods().getIntervalMod().remove(spring);
            }
        }
    }

    private List<Joint> createRing(Fabric fabric) {
        joints = new ArrayList<Joint>();
        double radius = ringSize / 10.0;
        for (int walk = 0; walk < ringSize; walk++) {
            double angle = walk * 2 * Math.PI / ringSize;
            Joint joint = fabric.createJoint(fabric.who().createMiddle(), new Arrow(radius * Math.cos(angle), radius * Math.sin(angle), 0));
            joints.add(joint);
            fabric.getMods().getJointMod().add(joint);
        }
        Ring ring = new Ring(joints, true);
        for (int walk = 0; walk < joints.size(); walk++) {
            boolean even = walk % 2 == 0;
            if (!even) {
                joints.get(walk).getLocation().add(ring.getNormal(), 0.03);
            }
            Interval ringCable = fabric.createInterval(joints.get(walk), joints.get((walk + 1) % ringSize), even ? Interval.Role.RINGBAR : Interval.Role.RING);
            setIdeal(ringCable);
            fabric.getMods().getIntervalMod().add(ringCable);
            if (even) {
                Interval spring = fabric.createInterval(joints.get(walk), joints.get((walk + 2) % ringSize), Interval.Role.SCAFFOLD);
                setIdeal(spring);
                fabric.getMods().getIntervalMod().add(spring);
            }
            else {
                Interval safety = fabric.createInterval(joints.get(walk), joints.get((walk + 2) % ringSize), Interval.Role.HORIZ);
                setIdeal(safety);
                fabric.getMods().getIntervalMod().add(safety);
            }
        }
        return joints;
    }

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
                case RINGBAR:
                    value = new Val(role, 0.6);
                    break;
                case RING:
                    value = new Val(role, 0.6);
                    break;
                case SCAFFOLD:
                    value = new Val(role, 1.3);
                    break;
                case ACROSS:
                    value = new Val(role, 0.4);
                    break;
                case HORIZ:
                    value = new Val(role, 1.3);
                    break;
                case VERT:
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