/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.transform;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Face;
import eu.beautifulcode.eig.structure.Interval;
import eu.beautifulcode.eig.structure.Joint;
import eu.beautifulcode.eig.structure.Tetra;

/**
 * Open up a triangular face like a book creating two new faces and moving the original
 * back cover of the book to be front cover.
 *
 * The 0th joint is the one that gets extended to make the apex.
 * Resulting Faces:  A-0-1, A-1-2, A-2-0
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class OpenUp implements Fabric.Transformation {
    private static final double INITIAL_SPAN = 0.07;
    private Face face;
    private double spanFactor;
    private int ticksToIdeal;
    private Interval.Role intervalRole;
    private Callback callback;
    private boolean useChirality = false;

    public OpenUp(Face face, double spanFactor, int ticksToIdeal, Interval.Role intervalRole) {
        this.face = face;
        this.spanFactor = spanFactor;
        this.ticksToIdeal = ticksToIdeal;
        this.intervalRole = intervalRole;
    }

    public void setUseChirality(boolean useChirality) {
        this.useChirality = useChirality;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void faces(Face face01, Face face12, Face face20);
    }

    public void transform(Fabric fabric) {
        validate(fabric);
        Joint apex = fabric.createJoint(face.createApexWho(fabric.who()), joint(0).getLocation());
        fabric.getMods().getJointMod().add(apex);
        Arrow normal = face.getNormal();
        normal.scale(INITIAL_SPAN);
        apex.getLocation().add(normal);
        double span1 = copyInterval(fabric, apex, joint(1));
        double span2 = copyInterval(fabric, apex, joint(2));
        Interval expanding = createExpandingInterval(fabric, apex, (span1 + span2) / 2);
        Face face0 = new Face(face.getOrder(), face.getChirality());
        face0.getJoints().add(apex);
        face0.getJoints().add(joint(0));
        face0.getJoints().add(joint(1));
        if (intervalRole == Interval.Role.MUSCLE) {
            face0.setStressInterval(expanding);
        }
        fabric.getMods().getFaceMod().add(face0);
        Face face1 = new Face(face.getOrder(), face.getChirality());
        face1.getJoints().add(apex);
        face1.getJoints().add(joint(2));
        face1.getJoints().add(joint(0));
        if (intervalRole == Interval.Role.MUSCLE) {
            face1.setStressInterval(expanding);
        }
        fabric.getMods().getFaceMod().add(face1);
        Tetra newTetra = new Tetra(joint(0), apex, joint(1), joint(2), face.getOrder() == Face.Order.LEFT_HANDED);
        face.getJoints().set(0, apex);
        fabric.getMods().getTetraMod().add(newTetra);
        if (useChirality) {
            face.twist(face.getChirality() == Face.Chirality.RIGHT_HANDED);
        }
        if (callback != null) {
            callback.faces(face0, face, face1);
        }
    }

    private void validate(Fabric fabric) {
        if (!fabric.getFaces().contains(face)) {
            throw new RuntimeException("Face is gone!");
        }
        for (Joint joint : face.getJoints()) {
            if (!fabric.getJoints().contains(joint)) {
                throw new RuntimeException("Missing joint "+joint);
            }
        }
        if (fabric.getInterval(face.getJoints().get(1),face.getJoints().get(2)) == null) {
            throw new RuntimeException("Missing "+face.getJoints().get(1)+" "+face.getJoints().get(2));
        }
        if (fabric.getInterval(face.getJoints().get(2),face.getJoints().get(0)) == null) {
            throw new RuntimeException("Missing "+face.getJoints().get(2)+" "+face.getJoints().get(0));
        }
        if (fabric.getInterval(face.getJoints().get(0),face.getJoints().get(1)) == null) {
            throw new RuntimeException("Missing "+face.getJoints().get(0)+" "+face.getJoints().get(1));
        }
    }

    private Joint joint(int index) {
        return face.getJoints().get(index);
    }

    private Interval createExpandingInterval(Fabric fabric, Joint apex, double ideal) {
        Interval interval = fabric.createInterval(face.getJoints().get(0), apex, intervalRole);
        interval.getSpan().setIdeal(ideal, ticksToIdeal);
        fabric.getMods().getIntervalMod().add(interval);
        return interval;
    }

    private double copyInterval(Fabric fabric, Joint apex, Joint joint) {
        Interval oldInterval = fabric.getInterval(face.getJoints().get(0), joint);
        if (oldInterval == null) {
            throw new RuntimeException("old Interval not found");
        }
        Interval interval = fabric.createInterval(joint, apex, Interval.Role.SPRING);
        interval.getSpan().setIdeal(oldInterval.getSpan().getUltimateIdeal() * spanFactor, ticksToIdeal);
        fabric.getMods().getIntervalMod().add(interval);
        return interval.getSpan().getUltimateIdeal();
    }
}