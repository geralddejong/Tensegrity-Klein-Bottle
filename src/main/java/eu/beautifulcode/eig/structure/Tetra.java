/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.math.Space4;
import eu.beautifulcode.eig.math.Volume;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Tetra {
    List<Joint> joints = new ArrayList<Joint>(4);
    boolean clockwise;

    public Tetra(Interval ab, Interval cd, boolean clockwise) {
        this(ab.alpha, ab.omega, cd.alpha, cd.omega, clockwise);
    }

    public Tetra(Joint a, Joint b, Joint c, Joint d, boolean clockwise) {
        joints.add(a);
        joints.add(b);
        joints.add(c);
        joints.add(d);
        this.clockwise = clockwise;
    }

    public Tetra() {
    }

    public List<Joint> getJoints() {
        return joints;
    }

    public boolean isClockwise() {
        return clockwise;
    }

    public List<Interval> getBars(Fabric fabric) {
        List<Interval> intervals = new ArrayList<Interval>();
        intervals.add(getInterval(0, 1, fabric)); // ab
        intervals.add(getInterval(2, 3, fabric)); // cd
        if (intervals.size() != 2) {
            throw new RuntimeException("Expected exactly 2 intervals");
        }
        return intervals;
    }

    public List<Interval> getCables(Fabric fabric) {
        List<Interval> intervals = new ArrayList<Interval>();
        intervals.add(getInterval(1, 2, fabric)); // bc
        intervals.add(getInterval(0, 2, fabric)); // ac
        intervals.add(getInterval(0, 3, fabric)); // ad
        intervals.add(getInterval(1, 3, fabric)); // bd
        if (intervals.size() != 4) {
            throw new RuntimeException("Expected exactly 4 intervals");
        }
        return intervals;
    }

    public void getLocation(Arrow location) {
        location.zero();
        for (Joint joint : joints) {
            location.add(joint.location);
        }
        location.scale(1.0 / joints.size());
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

    public void setSlack(double slackFactor, Fabric fabric, int ticksToIdeal) {
        List<Interval> cables = getCables(fabric);
        double existingTotalSpan = 0;
        for (Interval cable : cables) {
            existingTotalSpan += cable.span.getUltimateIdeal();
        }
        List<Interval> bars = getBars(fabric);
        double idealTotalSpan = (bars.get(0).span.getUltimateIdeal() + bars.get(1).span.getUltimateIdeal()) * slackFactor;
        double scale = idealTotalSpan / existingTotalSpan;
        for (Interval cable : cables) {
            cable.span.setIdeal(cable.span.getUltimateIdeal() * scale, ticksToIdeal);
        }
    }

    Joint joint(int index, boolean next) {
        if (next) {
            if (++index == joints.size()) {
                index = 0;
            }
        }
        return joints.get(index);
    }

    public List<Joint> commonJointsWith(Tetra other) {
        List<Joint> found = new ArrayList<Joint>();
        for (Joint joint : other.joints) {
            if (joints.contains(joint)) {
                found.add(joint);
            }
        }
        return found;
    }

    public void replace(Joint jointFrom, Joint jointTo) {
        int count = joints.size();
        for (int walk = 0; walk < count; walk++) {
            if (joints.get(walk) == jointFrom) {
                joints.set(walk, jointTo);
            }
        }
    }

    private Interval getInterval(int indexAlpha, int indexOmega, Fabric fabric) {
        Joint alpha = joints.get(indexAlpha);
        Joint omega = joints.get(indexOmega);
        List<Interval> intervals = fabric.getRealIntervals(alpha, omega);
        if (intervals.isEmpty()) {
            Interval interval = fabric.createInterval(alpha, omega, Interval.Role.CABLE);
            fabric.intervals.add(interval);
            return interval;
        }
        else if (intervals.size() == 1) {
            return intervals.get(0);
        }
        else {
            throw new RuntimeException("Expected only one interval!");
        }
    }

    public double getIdealVolume(Fabric fabric) {
        double ab = getInterval(0, 1, fabric).span.getUltimateIdeal();
        double cd = getInterval(2, 3, fabric).span.getUltimateIdeal();
        double bc = getInterval(1, 2, fabric).span.getUltimateIdeal();
        double ac = getInterval(0, 2, fabric).span.getUltimateIdeal();
        double ad = getInterval(0, 3, fabric).span.getUltimateIdeal();
        double db = getInterval(1, 3, fabric).span.getUltimateIdeal();
        return Volume.tetrahedron(ab, ac, ad, bc, cd, db);
    }

    public double getCurrentVolume() {
        // math found at http://mathforum.org/library/drmath/view/51837.html
        Space4 s = new Space4();
        s.m00 = joints.get(0).location.x;
        s.m01 = joints.get(0).location.y;
        s.m02 = joints.get(0).location.z;
        s.m03 = 1;
        s.m10 = joints.get(1).location.x;
        s.m11 = joints.get(1).location.y;
        s.m12 = joints.get(1).location.z;
        s.m13 = 1;
        s.m20 = joints.get(2).location.x;
        s.m21 = joints.get(2).location.y;
        s.m22 = joints.get(2).location.z;
        s.m23 = 1;
        s.m30 = joints.get(3).location.x;
        s.m31 = joints.get(3).location.y;
        s.m32 = joints.get(3).location.z;
        s.m33 = 1;
        return (s.determinant() / 6);
    }

    public String toString() {
        if (joints.size() < 4) {
            return "Tetra";
        }
        return "Tetra(" + joints.get(0) + ":" + joints.get(1) + ":" + joints.get(2) + ":" + joints.get(3) + ")";
    }
}