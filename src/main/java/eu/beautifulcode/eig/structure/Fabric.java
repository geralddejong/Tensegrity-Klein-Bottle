/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

import eu.beautifulcode.eig.math.Arrow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Fabric {
    long age;
    long lastSpanActive = -1;
    List<Joint> joints = new ArrayList<Joint>();
    List<Interval> intervals = new ArrayList<Interval>();
    List<Face> faces = new ArrayList<Face>();
    List<Tetra> tetras = new ArrayList<Tetra>();
    List<Vertebra> vertebras = new ArrayList<Vertebra>();
    Mods modifications = new Mods();
    WhoFactory whoFactory = new WhoFactory();
    Thing thing;
    Thing.Factory factory;

    public Fabric(Thing.Factory thingFactory) {
        this.factory = thingFactory;
    }

    public Who.Factory who() {
        return whoFactory;
    }

    public Thing getThing() {
        if (thing == null && getThingFactory() != null) {
            this.thing = getThingFactory().createFresh(this, "");
        }
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public Thing.Factory getThingFactory() {
        if (factory != null) {
            factory.setFabric(this);
        }
        return factory;
    }

    public Modifications getMods() {
        return modifications;
    }

    public void getCenter(Arrow center) {
        center.zero();
        for (Joint joint : joints) {
            center.add(joint.location);
        }
        center.scale(1.0 / joints.size());
    }

    public double getMaxDistanceFrom(Arrow center) {
        double maxQuadrance = 0;
        for (Joint joint : joints) {
            double quadrance = center.quadranceTo(joint.location);
            if (quadrance > maxQuadrance) {
                maxQuadrance = quadrance;
            }
        }
        return Math.sqrt(maxQuadrance);
    }

    public double getRadiusFrom(Arrow center) {
        double maxQuadrance = 0;
        for (Joint joint : joints) {
            double quadrance = joint.location.quadranceTo(center);
            if (quadrance > maxQuadrance) {
                maxQuadrance = quadrance;
            }
        }
        return Math.sqrt(maxQuadrance);
    }

    public long getAge() {
        return age;
    }

    public boolean hasTransformations() {
        return !modifications.transformations.isEmpty();
    }

    public void spansWereActive() {
        lastSpanActive = age;
    }

    public boolean isAnySpanActive() {
        return age == lastSpanActive;
    }

    public List<Joint> getJoints() {
        return joints;
    }

    public Joint getJoint(Who who) {
        for (Joint joint : joints) {
            if (joint.who.equals(who)) {
                return joint;
            }
        }
        return null;
    }

    public List<Interval> getIntervals() {
        return intervals;
    }

    public List<Interval> getIntervals(Joint joint) {
        List<Interval> found = new ArrayList<Interval>();
        for (Interval interval : intervals) {
            if (interval.contains(joint) && interval.isReal()) {
                found.add(interval);
            }
        }
        for (Interval interval : modifications.intervalMods.add) {
            if (interval.contains(joint) && interval.isReal()) {
                found.add(interval);
            }
        }
        return found;
    }

    public List<Interval> getRealIntervals(Joint jointA, Joint jointB) {
        List<Interval> found = new ArrayList<Interval>();
        for (Interval interval : intervals) {
            if (interval.connects(jointA, jointB) && interval.isReal()) {
                found.add(interval);
            }
        }
        for (Interval interval : modifications.intervalMods.add) {
            if (interval.connects(jointA, jointB) && interval.isReal()) {
                found.add(interval);
            }
        }
        return found;
    }

    public Interval getInterval(Joint jointA, Joint jointB) {
        Interval found = null;
        for (Interval interval : intervals) {
            if (interval.connects(jointA, jointB) && interval.isReal()) {
                if (found != null) {
                    throw new RuntimeException("Multiple intervals!");
                }
                found = interval;
            }
        }
        for (Interval interval : modifications.intervalMods.add) {
            if (interval.connects(jointA, jointB) && interval.isReal()) {
                if (found != null) {
                    throw new RuntimeException("Multiple intervals!");
                }
                found = interval;
            }
        }
        return found;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public List<Face.Pair> getFacePairs() {
        List<Face> opposites = new ArrayList<Face>();
        List<Face.Pair> pairs = new ArrayList<Face.Pair>();
        for (Face face : faces) {
            if (opposites.contains(face)) {
                continue;
            }
            Face opposite = getOppositeFace(face);
            if (opposite != null) {
                pairs.add(new Face.Pair(face, opposite));
                opposites.add(opposite);
            }
        }
        return pairs;
    }

    public Face getOppositeFace(Face face) {
        Who who0 = face.getJoints().get(0).who.createOpposite();
        Who who1 = face.getJoints().get(1).who.createOpposite();
        Who who2 = face.getJoints().get(2).who.createOpposite();
        if (who0 == null || who1 == null || who2 == null) {
            return null;
        }
        List<Face> foundFaces = getFaces(who0, who1, who2);
        if (foundFaces.size() == 1) {
            Face oppositeFace = foundFaces.get(0);
            if (oppositeFace != face) {
                return oppositeFace;
            }
        }
        else if (foundFaces.size() == 2) {
            if (foundFaces.get(0) == face) {
                return foundFaces.get(1);
            }
            else if (foundFaces.get(1) == face) {
                return foundFaces.get(0);
            }
        }
        return null;
    }

    public List<Face> getFaces(Joint joint) {
        List<Face> found = new ArrayList<Face>();
        for (Face face : faces) {
            if (face.joints.contains(joint)) {
                found.add(face);
            }
        }
        return found;
    }

    public List<Face> getFaces(Joint jointA, Joint jointB, Joint jointC) {
        List<Face> found = new ArrayList<Face>();
        for (Face face : faces) {
            if (face.contains(jointA) && face.contains(jointB) && face.contains(jointC)) {
                found.add(face);
            }
        }
        return found;
    }

    public List<Face> getFaces(Who whoA, Who whoB, Who whoC) {
        List<Face> found = new ArrayList<Face>();
        for (Face face : faces) {
            if (face.contains(whoA) && face.contains(whoB) && face.contains(whoC)) {
                found.add(face);
            }
        }
        return found;
    }

    public List<Face> getFaces(Tetra tetra) {
        List<Face> found = new ArrayList<Face>();
        for (Face face : faces) {
            int match = 0;
            for (Joint joint : tetra.joints) {
                if (face.joints.contains(joint)) {
                    match++;
                }
            }
            if (match == face.joints.size()) {
                found.add(face);
            }
        }
        return found;
    }

    public double getArea(Face face) {
        if (face.joints.size() != 3) {
            throw new IllegalStateException("Only works for triangles");
        }
        double[] spans = new double[3];
        for (int walk = 0; walk < 3; walk++) {
            double span = 0;
            List<Interval> sideIntervals = getRealIntervals(face.joints.get(walk), face.joints.get((walk + 1) % 3));
            for (Interval interval : sideIntervals) {
                span += interval.span.getUltimateIdeal();
//                logger.info(walk+": "+interval.span.getIdeal());
            }
            if (sideIntervals.isEmpty()) {
                throw new IllegalStateException("Side missing");
            }
            spans[walk] = span / sideIntervals.size();
        }
        double all = 0;
        for (double span : spans) {
            all += span;
        }
        double s = all / 2;
        double sq = s;
        for (double span : spans) {
            sq *= s - span;
        }
        if (sq <= 0) {
            throw new IllegalStateException("Degenerate face");
        }
        return Math.sqrt(sq);
    }


    public List<Tetra> getTetras() {
        return tetras;
    }

    public List<Tetra> getTetras(Joint joint) {
        List<Tetra> found = new ArrayList<Tetra>();
        for (Tetra tetra : tetras) {
            if (tetra.joints.contains(joint)) {
                found.add(tetra);
            }
        }
        return found;
    }

    public List<Vertebra> getVertebras() {
        return vertebras;
    }

    public Map<Who, Joint.Sheath> createSheathMap() {
        Map<Who, Joint.Sheath> sheathMap = new HashMap<Who, Joint.Sheath>(joints.size() * 2);
        for (Joint joint : joints) {
            if (joint.who.side != Who.Side.ELIMINATED) {
                sheathMap.put(joint.who, new JointSheath(joint));
            }
        }
        for (Interval interval : intervals) {
            if (!interval.isReal()) {
                continue;
            }
            JointSheath alpha = (JointSheath) sheathMap.get(interval.alpha.getWho());
            if (alpha == null) {
                throw new RuntimeException("No sheath found for "+interval.alpha.getWho()+" "+interval);
            }
            JointSheath omega = (JointSheath) sheathMap.get(interval.omega.getWho());
            if (omega == null) {
                throw new RuntimeException("No sheath found for "+interval.omega.getWho()+" "+interval);
            }
            alpha.intervalList.add(interval);
            omega.intervalList.add(interval);
            alpha.otherJointList.add(omega);
            omega.otherJointList.add(alpha);
        }
        return sheathMap;
    }

    public void executeTransformations(PhysicsTransformation physicsTransformation) {
        modifications.run(physicsTransformation);
    }

    public void addTransformation(Transformation transformation) {
        modifications.getTransformations().add(transformation);
    }

    public Interval createInterval(Joint alpha, Joint omega, Interval.Role role) {
        Interval interval = new Interval(alpha, omega, role);
        if (interval.role == Interval.Role.TEMPORARY) {
            interval.span.setIdeal(0, 30 + (int)(100 * interval.span.getCurrentIdeal()));
        }
        Thing.Factory thingFactory = getThingFactory();
        if (thingFactory != null) {
            interval.thing = thingFactory.createFresh(interval, "interval");
        }
        return interval;
    }

    public Joint createJoint(Who who, Arrow location) {
        Joint joint = new Joint(who, location);
        Thing.Factory thingFactory = getThingFactory();
        if (thingFactory != null) {
            joint.setThing(thingFactory.createFresh(joint, who.toString()));
        }
        return joint;
    }

    public interface Mod<T> {

        boolean isRemoved(T t);

        void add(T t);

        void remove(T t);

        void apply(List<T> list);
    }

    public interface Modifications {

        Mod<Joint> getJointMod();

        Mod<Interval> getIntervalMod();

        Mod<Face> getFaceMod();

        Mod<Tetra> getTetraMod();

        Mod<Vertebra> getVertebraMod();
    }

    public interface Transformation {
        void transform(Fabric fabric);
    }

    public interface PhysicsTransformation extends Transformation {
        void setIterations(int iterations);
    }

    public interface PeriodicTransformation extends Transformation {
        boolean isFinished();
    }

    private class Mods implements Modifications {
        private Queue<Transformation> transformations = new LinkedList<Transformation>();
        private ModCollection<Joint> jointMods = new ModCollection<Joint>() {
            @Override
            public void remove(Joint joint) {
                joint.who = whoFactory.createEliminated();
                super.remove(joint);
            }
        };
        private ModCollection<Interval> intervalMods = new ModCollection<Interval>() {
            @Override
            public void remove(Interval interval) {
                interval.role = Interval.Role.ELIMINATED;
//                if (interval.alpha.who.side == Who.Side.TEMPORARY) {
//                    interval.alpha.who.side = Who.Side.ELIMINATED;
//                }
//                if (interval.omega.who.side == Who.Side.TEMPORARY) {
//                    interval.omega.who.side = Who.Side.ELIMINATED;
//                }
                super.remove(interval);
            }
        };
        private ModCollection<Face> faceMods = new ModCollection<Face>();
        private ModCollection<Tetra> tetraMods = new ModCollection<Tetra>();
        private ModCollection<Vertebra> vertebraMods = new ModCollection<Vertebra>();

        public Queue<Transformation> getTransformations() {
            return transformations;
        }

        public ModCollection<Joint> getJointMod() {
            return jointMods;
        }

        public ModCollection<Interval> getIntervalMod() {
            return intervalMods;
        }

        public ModCollection<Face> getFaceMod() {
            return faceMods;
        }

        public ModCollection<Tetra> getTetraMod() {
            return tetraMods;
        }

        public ModCollection<Vertebra> getVertebraMod() {
            return vertebraMods;
        }

        public void run(PhysicsTransformation physicsTransformation) {
            if (!isAnySpanActive() || physicsTransformation == null) {
                while (!transformations.isEmpty()) {
                    transformAndModify(transformations.remove());
                }
            }
            if (physicsTransformation != null) {
                transformAndModify(physicsTransformation);
            }
        }

        private void transformAndModify(Transformation transformation) {
            transformation.transform(Fabric.this);
            for (Joint removedJoint : jointMods.remove) {
                for (Interval interval : intervals) {
                    if (interval.getRole() != Interval.Role.ELIMINATED) {
                        if (interval.contains(removedJoint)) {
                            intervalMods.remove(interval);
                        }
                    }
                }
                for (Face face : faces) {
                    if (face.joints.contains(removedJoint)) {
                        faceMods.remove(face);
                    }
                }
                for (Tetra tetra : tetras) {
                    if (tetra.joints.contains(removedJoint)) {
                        tetraMods.remove(tetra);
                    }
                }
                for (Vertebra vertebra : vertebras) {
                    if (vertebra.joints.contains(removedJoint)) {
                        vertebraMods.remove(vertebra);
                    }
                }
            }
            vertebraMods.apply(vertebras);
            tetraMods.apply(tetras);
            faceMods.apply(faces);
            intervalMods.apply(intervals);
            jointMods.apply(joints);
        }

        @Override
        public String toString() {
            return "Transformations=" + transformations.size();
        }
    }

    public void replace(Joint jointFrom, Joint jointTo) {
        for (Interval interval : intervals) {
            if (interval.replace(jointFrom, jointTo)) {
                modifications.getIntervalMod().remove(interval);
            }
        }
        for (Interval interval : modifications.intervalMods.add) {
            if (interval.replace(jointFrom, jointTo)) {
                modifications.getIntervalMod().remove(interval);
            }
        }
        for (Face face : faces) {
            face.replace(jointFrom, jointTo);
        }
        for (Face face : modifications.faceMods.add) {
            face.replace(jointFrom, jointTo);
        }
        for (Tetra tetra : tetras) {
            tetra.replace(jointFrom, jointTo);
        }
        for (Tetra tetra : modifications.tetraMods.add) {
            tetra.replace(jointFrom, jointTo);
        }
        for (Vertebra vertebra : vertebras) {
            vertebra.replace(jointFrom, jointTo);
        }
        for (Vertebra vertebra : modifications.vertebraMods.add) {
            vertebra.replace(jointFrom, jointTo);
        }
    }

    private class ModCollection<T> implements Mod<T> {
        private List<T> add = new ArrayList<T>();
        private List<T> remove = new ArrayList<T>();

        public boolean isRemoved(T t) {
            return remove.contains(t);
        }

        public void add(T t) {
            if (add.contains(t) || remove.contains(t)) {
                throw new RuntimeException();
            }
            add.add(t);
        }

        public void remove(T t) {
            if (add.contains(t)) {
                add.remove(t);
            }
            else if (!remove.contains(t)) {
                remove.add(t);
            }
        }

        public void apply(List<T> list) {
            if (!remove.isEmpty()) {
                list.removeAll(remove);
                remove.clear();
            }
            if (!add.isEmpty()) {
                list.addAll(add);
                add.clear();
            }
        }

        public String toString() {
            return "add=" + add.size() + ", remove=" + remove.size();
        }
    }

    private static class JointSheath implements Joint.Sheath {
        Joint joint;
        List<Interval> intervalList = new ArrayList<Interval>();
        List<JointSheath> otherJointList = new ArrayList<JointSheath>();

        private JointSheath(Joint joint) {
            this.joint = joint;
        }

        public List<? extends Joint.Sheath> getOtherSheaths() {
            return otherJointList;
        }

        public Joint getJoint() {
            return joint;
        }

        public List<Interval> getIntervals() {
            return intervalList;
        }
    }

    class WhoFactory implements Who.Factory {
        int[] id = new int[Who.Side.values().length];

        private WhoFactory() {
            for (int walk = 0; walk < id.length; walk++) {
                id[walk] = -1;
            }
        }

        private int nextId(Who.Side side) {
            if (id[side.ordinal()] < 0) {
                for (Joint joint : joints) {
                    if (joint.who.side != side) {
                        continue;
                    }
                    if (id[side.ordinal()] < joint.who.id) {
                        id[side.ordinal()] = joint.who.id;
                    }
                }
            }
            return ++id[side.ordinal()];
        }

        public Who createMiddle() {
            return new Who(Who.Side.MIDDLE, nextId(Who.Side.MIDDLE));
        }

        public Who createLeft() {
            return new Who(Who.Side.LEFT, nextId(Who.Side.LEFT));
        }

        public Who createRight() {
            return new Who(Who.Side.RIGHT, nextId(Who.Side.RIGHT));
        }

        public Who createTemporary() {
            return new Who(Who.Side.TEMPORARY, nextId(Who.Side.TEMPORARY));
        }

        public Who createEliminated() {
            return new Who(Who.Side.ELIMINATED, nextId(Who.Side.ELIMINATED));
        }

        public Who createAnotherLike(Who who) {
            return new Who(who.side, nextId(who.side));
        }
    }
}