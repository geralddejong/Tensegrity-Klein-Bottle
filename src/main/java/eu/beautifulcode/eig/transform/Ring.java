package eu.beautifulcode.eig.transform;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Joint;

import java.util.List;

/**
 * Given a ring of joints, figure out the midpoint and the normal
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class Ring {
    private List<Joint> joints;
    private boolean forward;
    private boolean calculated;
    private Arrow normal = new Arrow();
    private Arrow cross = new Arrow();
    private Arrow midpoint = new Arrow();
    private Arrow a = new Arrow();
    private Arrow b = new Arrow();

    public Ring(List<Joint> joints, boolean forward) {
        this.joints = joints;
        this.forward = forward;
    }

    public Arrow getMidpoint() {
        calculate();
        return midpoint;
    }

    public Arrow getNormal() {
        calculate();
        return normal;
    }

    private void calculate() {
        if (calculated) {
            return;
        }
        midpoint.zero();
        for (Joint joint : joints) {
            midpoint.add(joint.getLocation());
        }
        midpoint.scale(1.0 / joints.size());
        for (int walk = 0; walk < joints.size(); walk++) {
            Joint jointA = joint(walk);
            Joint jointB = joint(walk + 1);
            a.sub(jointA.getLocation(), midpoint);
            b.sub(jointB.getLocation(), midpoint);
            if (forward) {
                cross.cross(b, a);
            }
            else {
                cross.cross(a, b);
            }
            double span = cross.span();
            if (span != 0) {
                cross.scale(1 / span);
                normal.add(cross);
            }
        }
        double normalSpan = normal.span();
        if (normalSpan > 0.0001) {
            normal.scale(1 / normalSpan);
        }
        calculated = true;
    }

    private Joint joint(int index) {
        if (index == joints.size()) {
            index = 0;
        }
        return joints.get(index);
    }
}
