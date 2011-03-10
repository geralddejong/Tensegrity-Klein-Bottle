package eu.beautifulcode.eig.gui;

import javax.swing.*;
import eu.beautifulcode.eig.structure.Physics;

/**
 * Play with a physics value;
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class DoubleRangeModel {
    private DefaultBoundedRangeModel model = new DefaultBoundedRangeModel();
    private Physics.Value physicsValue;
    private double defaultValue, rangeFactor, step;

    public DoubleRangeModel(Physics.Value physicsValue, double rangeFactor) {
        this.physicsValue = physicsValue;
        this.defaultValue = physicsValue.get();
        this.rangeFactor = rangeFactor;
        this.step = Math.pow(rangeFactor, 0.02);
        model.setValue(50);
    }

    public BoundedRangeModel getModel() {
        return model;
    }

    public Physics.Value getPhysicsValue() {
        return physicsValue;
    }

    public void valueToModel() {
        if (physicsValue.get() < defaultValue / rangeFactor || physicsValue.get() > defaultValue * rangeFactor) throw new IllegalArgumentException("Value violates range:"+ physicsValue);
        model.setValue(getIntValue(physicsValue.get()));
    }

    public void modelToValue() {
        physicsValue.set(getDoubleValue());
    }

    private double getDoubleValue() {
        int power = model.getValue() - 50;
        return defaultValue * Math.pow(step, power);
    }

    private int getIntValue(double value) {
        return 50 + (int) Math.round(Math.log(value) / Math.log(step));
    }

    public static void main(String[] args) {
        Physics.Value physicsValue = new Physics.Value() {
            double value = 10;

            public String getName() {
                return "?";
            }

            public void set(double value) {
                this.value = value;
            }

            public double get() {
                return value;
            }
        };
        DoubleRangeModel model = new DoubleRangeModel(physicsValue, 2);
        for (int walk=0; walk<= 100; walk++) {
            model.getModel().setValue(walk);
            System.out.println(walk+": "+model.getDoubleValue() + " :"+model.getIntValue(model.getDoubleValue()));
        }
    }
}
