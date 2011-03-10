/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

import eu.beautifulcode.eig.math.Arrow;

import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Joint {
    Who who;
    Arrow location = new Arrow();
    Arrow force = new Arrow();
    double intervalMass;
    Arrow velocity = new Arrow();
    Arrow absorbVelocity = new Arrow();
    Arrow gravity = new Arrow();
    double altitude;
    Thing thing;

    Joint(Who who) {
        this.who = who;
    }

    Joint(Who who, Arrow location) {
        this.who = who;
        this.location.set(location);
    }

    public Who getWho() {
        return who;
    }

    public void getLocation(Arrow location) {
        location.set(this.location);
    }

    public Arrow getLocation() {
        return location;
    }

    public Arrow getVelocity() {
        return velocity;
    }

    public Arrow getGravity() {
        return gravity;
    }

    public double getAltitude() {
        return altitude;
    }

    public double setAltitude(double altitude) {
        return this.altitude = altitude;
    }

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public String toString() {
        return who.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Joint joint = (Joint) o;
        return
                !(location != null ? !location.equals(joint.location) : joint.location != null) &&
                !(velocity != null ? !velocity.equals(joint.velocity) : joint.velocity != null) &&
                        !(who != null ? !who.equals(joint.who) : joint.who != null);
    }

    public interface Sheath {
        Joint getJoint();
        List<Interval> getIntervals();
        List<? extends Joint.Sheath> getOtherSheaths();
    }
}
