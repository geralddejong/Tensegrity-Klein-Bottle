package eu.beautifulcode.eig.structure;

import org.apache.log4j.Logger;

/**
 * Hold a double value for the physics engine
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class PhysicsValue implements Physics.Value {
    private Logger log = Logger.getLogger(getClass());
    private String name;
    private Double nextValue;
    private double value;

    public PhysicsValue(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void set(double value) {
        this.nextValue = value;
    }

    public double get() {
        if (nextValue != null) {
            log.info(String.format("%s : %f -> %f", name, value, nextValue));
            value = nextValue;
            nextValue = null;
        }
        return value;
    }
}
