/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.transform;

import org.apache.log4j.Logger;
import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Interval;
import eu.beautifulcode.eig.structure.Joint;
import eu.beautifulcode.eig.structure.Who;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class JointMerge implements Fabric.Transformation {
    private static final Logger LOG = Logger.getLogger(JointMerge.class);
    private Joint jointA, jointB;
    private Joint middle;

    public JointMerge(Joint jointA, Joint jointB) {
        this.jointA = jointA;
        this.jointB = jointB;
    }

    public void transform(Fabric fabric) {
        List<Interval> intervals = fabric.getRealIntervals(jointA, jointB);
        for (Interval interval : intervals) {
            fabric.getMods().getIntervalMod().remove(interval);
            LOG.warn("Removing unexpected interval" + interval);
        }
        middle = fabric.createJoint(fabric.who().createTemporary(), new Arrow(jointA.getLocation(),jointB.getLocation()));
        fabric.getMods().getJointMod().add(middle);
        Interval cableA = fabric.createInterval(jointA, middle, Interval.Role.TEMP);
        fabric.getMods().getIntervalMod().add(cableA);
        Interval cableB = fabric.createInterval(jointB, middle, Interval.Role.TEMP);
        fabric.getMods().getIntervalMod().add(cableB);
    }

    public Joint getMiddle() {
        return middle;
    }

    public static class Periodic implements Fabric.PeriodicTransformation {
        private double thresholdDot;
        private boolean finished;

        public Periodic(double thresholdDegrees) {
            this.thresholdDot = Math.cos(thresholdDegrees/180*Math.PI);
        }

        public void transform(Fabric fabric) {
            Map<Who, Joint.Sheath> sheathMap = fabric.createSheathMap();
            Set<Who> joints = new TreeSet<Who>();
            nextSheath:
            for (Joint.Sheath sheath : sheathMap.values()) {
                for (int walkA = 0; walkA < sheath.getIntervals().size(); walkA++) {
                    Interval a = sheath.getIntervals().get(walkA);
                    Arrow aUnit = new Arrow(a.getUnit(true));
                    if (sheath.getJoint() == a.get(true)) {
                        aUnit.scale(-1);
                    }
                    for (int walkB = walkA + 1; walkB < sheath.getIntervals().size(); walkB++) {
                        Interval b = sheath.getIntervals().get(walkB);
                        Arrow bUnit = new Arrow(b.getUnit(true));
                        if (sheath.getJoint() == b.get(true)) {
                            bUnit.scale(-1);
                        }
                        double dot = aUnit.dot(bUnit);
                        if (dot > thresholdDot) {
                            Joint jointA = sheath.getOtherSheaths().get(walkA).getJoint();
                            Joint jointB = sheath.getOtherSheaths().get(walkB).getJoint();
                            List<Interval> intervals = fabric.getRealIntervals(jointA, jointB);
                            if (!joints.contains(jointA.getWho()) && !joints.contains(jointB.getWho()) && intervals.isEmpty()) {
                                LOG.info(" Triggered merge of "+jointA+" & "+jointB);
                                fabric.addTransformation(new JointMerge(jointA, jointB));
                                joints.add(jointA.getWho());
                                joints.add(jointB.getWho());
                                continue nextSheath;
                            }
                        }
                    }
                }
            }
            if (joints.isEmpty()) {
                finished = true;
            }
        }

        public boolean isFinished() {
            return finished;
        }
    }
}