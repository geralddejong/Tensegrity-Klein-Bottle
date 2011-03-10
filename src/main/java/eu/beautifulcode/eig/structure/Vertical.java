package eu.beautifulcode.eig.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * A physics with vertical gravity
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class Vertical implements Physics.Constraints {
    private static final double JOINT_RADIUS = 0.01;
    private PhysicsValue airDrag = new PhysicsValue("airDrag", 0.002);
    private PhysicsValue airGravity = new PhysicsValue("airGravity", 0.000001);
    private PhysicsValue landDrag = new PhysicsValue("landDrag", 20);
    private PhysicsValue landGravity = new PhysicsValue("landGravity", 60);
    private PhysicsValue elasticFactor = new PhysicsValue("elasticFactor", 0.4);

    public Vertical() {
    }

    public PhysicsValue getAirDrag() {
        return airDrag;
    }

    public PhysicsValue getAirGravity() {
        return airGravity;
    }

    public PhysicsValue getLandDrag() {
        return landDrag;
    }

    public PhysicsValue getLandGravity() {
        return landGravity;
    }

    public List<PhysicsValue> getPhysicsValues() {
        List<PhysicsValue> values = new ArrayList<PhysicsValue>();
        values.add(airGravity);
        values.add(airDrag);
        values.add(elasticFactor);
        values.add(landGravity);
        values.add(landDrag);
        return values;
    }

    public PhysicsValue getElasticFactor() {
        return elasticFactor;
    }

    public void exertJointPhysics(Joint joint, Fabric fabric) {
        double altitude = joint.location.z;
        if (altitude > JOINT_RADIUS) {
            exertGravity(joint, airGravity.get());
            joint.getVelocity().scale(1 - airDrag.get());
        }
        else if (altitude < -JOINT_RADIUS) {
            exertGravity(joint, -airGravity.get() * landGravity.get());
            joint.getVelocity().scale(1 - airDrag.get() * landDrag.get());
        }
        else {
            double degree = (altitude + JOINT_RADIUS) / (JOINT_RADIUS * 2);
            double gravityValue = airGravity.get() * degree + -airGravity.get() * landGravity.get() * (1 - degree);
            exertGravity(joint, gravityValue);
            double drag = airDrag.get() * degree + airDrag.get() * landDrag.get() * (1 - degree);
            joint.getVelocity().scale(1 - drag);
        }
    }

    public void exertGravity(Joint joint, double value) {
        joint.velocity.z -= value;
    }

    public void postIterate(Fabric fabric) {
    }
}
