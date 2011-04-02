/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

import org.apache.log4j.Logger;
import eu.beautifulcode.eig.math.Arrow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Physics implements Fabric.PhysicsTransformation {
    private Logger logger = Logger.getLogger(getClass());
    private static final double AMBIENT_JOINT_MASS = 0.1;
    private static final double CABLE_MASS_FACTOR = 0.05;
    private static final int INTERVAL_MERGE_ITERATIONS = 50;
    private Arrow gravity = new Arrow();
    private Constraints constraints;
    private int iterations = 1;

    public interface Value {
        String getName();

        void set(double value);

        double get();
    }

    public interface Constraints {

        List<PhysicsValue> getPhysicsValues();

        PhysicsValue getElasticFactor();

        void exertJointPhysics(Joint joint, Fabric fabric);

        void postIterate(Fabric fabric);
    }

    public Physics(Constraints constraints) {
        this.constraints = constraints;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getIterations() {
        return iterations;
    }

    public void transform(Fabric fabric) {
        for (int walk = 0; walk < iterations; walk++) {
            iterate(fabric);
            constraints.postIterate(fabric);
        }
    }

    private void iterate(Fabric fabric) {
        fabric.age++;
        boolean anySpanActive = false;
        for (Interval interval : fabric.intervals) {
            if (interval.span.experienceTime(fabric.age)) {
                anySpanActive = true;
            }
            if (interval.role != Interval.Role.GONE) {
                elastic(interval);
            }
            if (interval.role == Interval.Role.TEMP) {
                if (!interval.span.isActive()) {
                    eliminate(interval, fabric);
                }
            }
        }
        if (anySpanActive) {
            fabric.spansWereActive();
        }
        for (Interval interval : fabric.intervals) {
            if (interval.role == Interval.Role.GONE) continue;
            smoothVelocity(interval, interval.role.smoothVelocity);
        }
        for (Joint joint : fabric.joints) {
            switch (joint.who.side) {
                case ELIMINATED:
                case TEMPORARY:
                    continue;
            }
            if (joint.intervalMass == 0) {
                throw new RuntimeException("No mass! " + joint);
            }
            constraints.exertJointPhysics(joint, fabric);
            joint.velocity.add(joint.force, 1 / joint.intervalMass);
            joint.force.zero();
            joint.velocity.add(joint.absorbVelocity);
            joint.absorbVelocity.zero();
        }
        for (Interval interval : fabric.intervals) {
            if (interval.role == Interval.Role.GONE) continue;
            double alphaAltitude = interval.alpha.getAltitude();
            double omegaAltitude = interval.omega.getAltitude();
            boolean straddle = alphaAltitude > 0 ^ omegaAltitude > 0;
            if (straddle) {
                double totalAltitude = Math.abs(alphaAltitude) + Math.abs(omegaAltitude);
                if (totalAltitude > 0.001) {
                    gravity.interpolate(interval.alpha.getGravity(), interval.omega.gravity, Math.abs(omegaAltitude) / totalAltitude);
                }
                else {
                    gravity.average(interval.alpha.gravity, interval.omega.gravity);
                }
            }
            else {
                gravity.average(interval.alpha.gravity, interval.omega.gravity);
            }
            interval.alpha.getVelocity().add(gravity);
            interval.omega.getVelocity().add(gravity);
        }
        for (Joint joint : fabric.joints) {
            switch (joint.who.side) {
                case ELIMINATED:
                case TEMPORARY:
                    continue;
            }
            joint.location.add(joint.velocity);
            joint.intervalMass = AMBIENT_JOINT_MASS;
        }
    }

    // === the rest is private

    private void elastic(Interval interval) {
        Arrow unit = interval.getUnit(true);
        Span span = interval.span;
        if (span.isSignificant()) {
            double ideal = span.getCurrentIdeal();
            span.stress = constraints.getElasticFactor().get() * (span.actual - ideal) * (interval.role.canPush ? ideal * ideal : 1);
            if (interval.role.canPush || span.stress > 0) {
                interval.alpha.force.add(unit, span.stress / 2);
                interval.omega.force.sub(unit, span.stress / 2);
            }
            double mass = interval.role.canPush ? ideal * ideal * ideal : span.actual * CABLE_MASS_FACTOR;
            interval.alpha.intervalMass += mass / 2;
            interval.omega.intervalMass += mass / 2;
        }
    }

    private Arrow projection = new Arrow();
    private Arrow alphaProjection = new Arrow();
    private Arrow omegaProjection = new Arrow();

    private void smoothVelocity(Interval interval, double degree) {
        splitArrows(interval.alpha.velocity, interval.getUnit(false), alphaProjection, degree);
        splitArrows(interval.omega.velocity, interval.getUnit(false), omegaProjection, degree);
        projection.add(alphaProjection, omegaProjection);
        projection.scale(0.5);
        interval.alpha.absorbVelocity.sub(alphaProjection);
        interval.omega.absorbVelocity.sub(omegaProjection);
        interval.alpha.absorbVelocity.add(projection);
        interval.omega.absorbVelocity.add(projection);
    }

    private static void splitArrows(Arrow arrow, Arrow basis, Arrow projection, double howMuch) {
        double agreement = arrow.dot(basis);
        projection.set(basis);
        projection.scale(agreement * howMuch);
    }

    private void eliminate(Interval interval, Fabric fabric) {
        logger.info(fabric.age + ": Eliminating " + interval);
        fabric.getMods().getIntervalMod().remove(interval);
        if (!fabric.getRealIntervals(interval.alpha, interval.omega).isEmpty()) { // todo: eliminate later
            throw new RuntimeException("Other intervals!");
        }
        Who alphaWho = interval.alpha.who;
        Who omegaWho = interval.omega.who;
        if (alphaWho.side == omegaWho.side) {
            switch (alphaWho.side) {
                case TEMPORARY:
                case ELIMINATED:
                    replace(interval.alpha, interval.omega, fabric); // doesn't matter, they're the same
                    return;
                case LEFT:
                case RIGHT:
                case MIDDLE:
                    if (alphaWho.id < omegaWho.id) {
                        replace(interval.omega, interval.alpha, fabric); // alpha survives
                    }
                    else if (alphaWho.id > omegaWho.id) {
                        replace(interval.alpha, interval.omega, fabric); // omega survives
                    }
                    else {
                        throw new RuntimeException("Same joint?");
                    }
                    return;
            }
        }
        else {
            switch (alphaWho.side) {
                case TEMPORARY:
                case ELIMINATED:
                    replace(interval.alpha, interval.omega, fabric);
                    return;
            }
            switch (omegaWho.side) {
                case TEMPORARY:
                case ELIMINATED:
                    replace(interval.omega, interval.alpha, fabric);
                    return;
            }
            switch (alphaWho.side) {
                case LEFT:
                    switch (omegaWho.side) {
                        case MIDDLE:
                            replace(interval.alpha, interval.omega, fabric); // middle survives
                            return;
                        case RIGHT:
                            if (alphaWho.id < omegaWho.id) {
                                replace(interval.omega, interval.alpha, fabric); // alpha survives
                            }
                            else if (alphaWho.id > omegaWho.id) {
                                replace(interval.alpha, interval.omega, fabric); // omega survives
                            }
                            else {
                                replaceToMiddle(interval, fabric);
                            }
                            return;
                    }
                    break;
                case MIDDLE:
                    switch (omegaWho.side) {
                        case LEFT:
                        case RIGHT:
                            replace(interval.omega, interval.alpha, fabric); // middle survives
                            return;
                    }
                    break;
                case RIGHT:
                    switch (omegaWho.side) {
                        case LEFT:
                            if (alphaWho.id < omegaWho.id) {
                                replace(interval.omega, interval.alpha, fabric); // alpha survives
                            }
                            else if (alphaWho.id > omegaWho.id) {
                                replace(interval.alpha, interval.omega, fabric); // omega survives
                            }
                            else {
                                replaceToMiddle(interval, fabric);
                            }
                            return;
                        case MIDDLE:
                            replace(interval.alpha, interval.omega, fabric); // middle survives
                            return;
                    }
                    break;
            }
        }
        throw new RuntimeException("Cannot decide which joint to remove! " + interval);
    }

    private void replaceToMiddle(Interval interval, Fabric fabric) {
        Joint joint = new Joint(fabric.who().createMiddle(), interval.createMidpoint());
        joint.intervalMass = AMBIENT_JOINT_MASS;
        fabric.getMods().getJointMod().add(joint);
        fabric.replace(interval.alpha, joint);
        fabric.replace(interval.omega, joint);
        fabric.getMods().getJointMod().remove(interval.alpha);
        fabric.getMods().getJointMod().remove(interval.omega);
        mergeMultipleIntervals(fabric, joint);
        removeRedundantFaces(fabric, joint);
        removeTetrasAndInnerFaces(joint, fabric);
    }

    private void replace(Joint from, Joint to, Fabric fabric) {
        to.location.average(from.location, to.location);
        fabric.replace(from, to);
        fabric.getMods().getJointMod().remove(from);
        mergeMultipleIntervals(fabric, to);
        removeRedundantFaces(fabric, to);
//        removeTetrasAndInnerFaces(to, fabric);
    }

    private void removeTetrasAndInnerFaces(Joint alive, Fabric fabric) {
        List<Tetra> tetras = fabric.getTetras(alive);
        int count = tetras.size();
        for (int walkA = 0; walkA < count; walkA++) {
            Tetra tetraA = tetras.get(walkA);
            for (int walkB = walkA + 1; walkB < count; walkB++) {
                Tetra tetraB = tetras.get(walkB);
                List<Joint> common = tetraA.commonJointsWith(tetraB);
                switch (common.size()) {
                    case 3:
                        List<Face> faces = fabric.getFaces(common.get(0), common.get(1), common.get(2));
                        for (Face face : faces) {
                            fabric.getMods().getFaceMod().remove(face);
                        }
                        break;
                    case 4:
                        fabric.getMods().getTetraMod().remove(tetraB);
                        break;
                }
            }
        }
    }

    private void mergeMultipleIntervals(Fabric fabric, Joint joint) {
        List<Interval> intervals = fabric.getIntervals(joint);
        List<Joint> others = new ArrayList<Joint>();
        for (Interval interval : intervals) {
            if (interval.role == Interval.Role.TEMP || interval.role == Interval.Role.GONE) continue;
            Joint other = interval.getOther(joint);
            if (other.who.side != Who.Side.ELIMINATED) {
                if (others.contains(other)) {
                    mergeMultipleIntervals(joint, other, fabric);
                    others.remove(other);
                }
                else {
                    others.add(other);
                }
            }
        }
    }

    private void mergeMultipleIntervals(Joint jointA, Joint jointB, Fabric fabric) {
        List<Interval> multiples = fabric.getRealIntervals(jointA, jointB);
        double totalSpan = 0;
        Interval.Role role = null;
        for (Interval member : multiples) {
            if (role == null) {
                role = member.role;
            }
            else if (role != member.role) {
                role = Interval.Role.SPRING;
                logger.info("Defaulting to SPRING role");
            }
            totalSpan += member.span.getUltimateIdeal();
            logger.info(fabric.age + ":removing multiple " + member);
            fabric.getMods().getIntervalMod().remove(member);
        }
        if (role == null) {
            role = Interval.Role.SPRING;
        }
        Interval interval = fabric.createInterval(jointA, jointB, role);
        interval.span.setIdeal(totalSpan / multiples.size(), INTERVAL_MERGE_ITERATIONS);
        fabric.getMods().getIntervalMod().add(interval);
        logger.info(fabric.age + ":replacing "+multiples.size()+" with one: " + interval);
    }

    private void removeRedundantFaces(Fabric fabric, Joint joint) {
        List<Face> faces = fabric.getFaces(joint);
        int count = faces.size();
        logger.info(fabric.age + ": remove redundant faces.. now " + count + " around " + joint);
        for (int walkA = 0; walkA < count; walkA++) {
            Face faceA = faces.get(walkA);
            if (fabric.getMods().getFaceMod().isRemoved(faceA)) continue;
            for (int walkB = walkA + 1; walkB < count; walkB++) {
                Face faceB = faces.get(walkB);
                if (fabric.getMods().getFaceMod().isRemoved(faceB)) continue;
                if (faceA.sameJointsAs(faceB)) {
                    Arrow normalA = faceA.getNormal();
                    Arrow normalB = faceB.getNormal();
                    double dot = normalA.dot(normalB);
                    if (dot > 0) {
                        logger.info(fabric.age + ": double agreeing face, one removed: " + faceB);
                        fabric.getMods().getFaceMod().remove(faceB);
                    }
                    else {
                        logger.info(fabric.age + ": opposing faces, both removed: " + faceA + ", " + faceB);
                        fabric.getMods().getFaceMod().remove(faceA);
                        fabric.getMods().getFaceMod().remove(faceB);
                    }
                }
            }
        }
    }

}