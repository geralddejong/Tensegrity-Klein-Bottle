/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

import eu.beautifulcode.eig.math.Arrow;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Interval {
    private static final double BAR_SMOOTH = 0.6;
    private static final double SPRING_SMOOTH = 0.03;
    private static final double CABLE_SMOOTH = 0.01;

    public enum Role {
        SPRING(true, SPRING_SMOOTH),
        RING_SPRING(true, SPRING_SMOOTH),
        MUSCLE(true, SPRING_SMOOTH),
        BAR(true, BAR_SMOOTH),
        CABLE(false, CABLE_SMOOTH),
        COUNTER_CABLE(false, CABLE_SMOOTH),
        HORIZONTAL_CABLE(false, CABLE_SMOOTH),
        RING_CABLE(false, CABLE_SMOOTH),
        VERTICAL_CABLE(false, CABLE_SMOOTH),
        TEMPORARY(false, CABLE_SMOOTH),
        ELIMINATED(false, CABLE_SMOOTH);

        public final boolean canPush;
        public final double smoothVelocity;

        Role(boolean canPush, double smoothVelocity) {
            this.canPush = canPush;
            this.smoothVelocity = smoothVelocity;
        }
    }

    Role role;
    Joint alpha;
    Joint omega;
    Span span;
    Arrow unit;
    Thing thing;

    Interval() {
    }

    Interval(Joint alpha, Joint omega, Role role) {
        this.alpha = alpha;
        this.omega = omega;
        this.role = role;
        double current = alpha.location.distanceTo(omega.location);
        this.span = new Span(current, current, 0);
    }

    public boolean connects(Joint jointA, Joint jointB) {
        return jointA == alpha && jointB == omega || jointA == omega && jointB == alpha;
    }

    public boolean contains(Joint joint) {
        return joint == alpha || joint == omega;
    }

    public Role getRole() {
        return role;
    }

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public Joint get(boolean omega) {
        return omega ? this.omega : this.alpha;
    }

    public Arrow getUnit(boolean fresh) {
        if (unit == null) {
            unit = new Arrow();
            fresh = true;
        }
        if (fresh) {
            unit.sub(omega.location, alpha.location);
            span.actual = unit.span();
            if (span.actual > 0.001) {
                unit.scale(1 / span.actual);
            }
            else {
                unit.set(0, 0, 1); // no better idea
            }
        }
        return unit;
    }

    public void getLocation(Arrow location) {
        location.average(alpha.location, omega.location);
    }

    public Arrow createMidpoint() {
        return new Arrow(alpha.location, omega.location);
    }

    public Span getSpan() {
        return span;
    }

    public boolean isReal() {
        return
                role != Role.ELIMINATED &&
                role != Role.TEMPORARY &&
                alpha.who.side != Who.Side.ELIMINATED &&
                omega.who.side != Who.Side.ELIMINATED;
    }

    public boolean replace(Joint from, Joint to) {
        if (role != Role.ELIMINATED) {
            if (from == alpha) {
                if (to == omega) {
                    alpha = null;
                    return true;
                }
                else {
                    alpha = to;
                    return false;
                }
            }
            if (from == omega) {
                if (to == alpha) {
                    omega = null;
                    return true;
                }
                else {
                    omega = to;
                    return false;
                }
            }
        }
        return false;
    }

    public Joint getOther(Joint joint) {
        if (joint == alpha) {
            return omega;
        }
        else if (joint == omega) {
            return alpha;
        }
        else {
            throw new IllegalArgumentException("Finding other but the joint doesn't belong to this cable");
        }
    }

    public String toString() {
        return alpha + "[" + role + "]" + omega;
    }
}