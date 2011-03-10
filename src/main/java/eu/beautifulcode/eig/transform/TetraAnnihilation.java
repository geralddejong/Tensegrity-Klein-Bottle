/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.transform;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Face;
import eu.beautifulcode.eig.structure.Joint;
import eu.beautifulcode.eig.structure.Tetra;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TetraAnnihilation implements Fabric.Transformation {
    private Logger log = Logger.getLogger(getClass());
    private Tetra tetraA, tetraB, newTetra;

    public TetraAnnihilation(Tetra tetraA, Tetra tetraB) {
        this.tetraA = tetraA;
        this.tetraB = tetraB;
    }

    public void transform(Fabric fabric) {
        log.info("tetra annihilation begins");
        List<Joint> jointsA = tetraA.getJoints();
        List<Joint> jointsB = getJointsB();
        newTetra = new Tetra();
        for (int walk = 0; walk < jointsA.size(); walk++) {
            Joint jointA = jointsA.get(walk);
            Joint jointB = jointsB.get(walk);
            if (jointA != jointB) {
                log.info("merging " + jointA + " and " + jointB);
                JointMerge merge = new JointMerge(jointA, jointB);
                merge.transform(fabric);
                newTetra.getJoints().add(merge.getMiddle());
            }
            else {
                newTetra.getJoints().add(jointA);
            }
        }
        List<Face> copyFaces = new ArrayList<Face>();
        List<Face> facesA = fabric.getFaces(tetraA);
        for (Face faceA : facesA) {
            if (copyFaces.contains(faceA)) continue;
            if (fabric.getMods().getFaceMod().isRemoved(faceA)) continue;
            Arrow normalA = faceA.getNormal();
            List<Face> facesB = fabric.getFaces(tetraB);
            for (Face faceB : facesB) {
                if (faceB == faceA) continue;
                if (copyFaces.contains(faceB)) continue;
                if (fabric.getMods().getFaceMod().isRemoved(faceB)) continue;
                Arrow normalB = faceB.getNormal();
                if (normalA.dot(normalB) > 0.8) {
                    if (copyFaces.contains(faceB)) {
                        throw new RuntimeException();
                    }
                    copyFaces.add(faceB);
                    fabric.getMods().getFaceMod().remove(faceA);
                    fabric.getMods().getFaceMod().remove(faceB);
                }
            }
        }
        for (Face copyFace : copyFaces) {
            Face copy = new Face(copyFace.getOrder(), copyFace.getChirality());
            List<Joint> newTetraJoints = new ArrayList<Joint>(newTetra.getJoints());
            for (Joint oldJoint : copyFace.getJoints()) {
                double closestDistance = Double.POSITIVE_INFINITY;
                Joint closestJoint = null;
                for (Joint newJoint : newTetraJoints) {
                    double distance = oldJoint.getLocation().distanceTo(newJoint.getLocation());
                    if (distance < closestDistance) {
                        closestJoint = newJoint;
                        closestDistance = distance;
                    }
                }
                if (closestJoint == null) {
                    throw new IllegalStateException("Closest joint not found");
                }
                copy.getJoints().add(closestJoint);
                newTetraJoints.remove(closestJoint);
            }
            fabric.getMods().getFaceMod().add(copy);
        }
        fabric.getMods().getTetraMod().add(newTetra);
        fabric.getMods().getTetraMod().remove(tetraA);
        fabric.getMods().getTetraMod().remove(tetraB);
        log.info("tetra annihilation completes");
    }

    private List<Joint> getJointsB() {
        List<Joint> jointsB = new ArrayList<Joint>();
        List<Joint> originalJointsB = new ArrayList<Joint>(tetraB.getJoints());
        for (Joint a : tetraA.getJoints()) {
            double closestDistance = Double.POSITIVE_INFINITY;
            Joint closestJoint = null;
            for (Joint b : originalJointsB) {
                double distance = a.getLocation().distanceTo(b.getLocation());
                if (distance < closestDistance) {
                    closestJoint = b;
                    closestDistance = distance;
                }
            }
            jointsB.add(closestJoint);
            originalJointsB.remove(closestJoint);
        }
        return jointsB;
    }

    public Tetra getNewTetra() {
        return newTetra;
    }

    public static class Periodic implements Fabric.Transformation {
        private Logger log = Logger.getLogger(getClass());
        private double thresholdDistance;
        private Arrow a = new Arrow();
        private Arrow b = new Arrow();

        public Periodic(double thresholdDistance) {
            this.thresholdDistance = thresholdDistance;
        }

        public void transform(Fabric fabric) {
            Tetra closestTetraA = null, closestTetraB = null;
            double closestDistance = Double.POSITIVE_INFINITY;
            for (int walkA = 0; walkA < fabric.getTetras().size(); walkA++) {
                fabric.getTetras().get(walkA).getLocation(a);
                for (int walkB = walkA + 1; walkB < fabric.getTetras().size(); walkB++) {
                    fabric.getTetras().get(walkB).getLocation(b);
                    double distance = a.distanceTo(b);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestTetraA = fabric.getTetras().get(walkA);
                        closestTetraB = fabric.getTetras().get(walkB);
                    }
                }
            }
            log.info("closest distance: " + closestDistance);
            if (closestDistance < thresholdDistance) {
                TetraAnnihilation annihilation = new TetraAnnihilation(closestTetraA, closestTetraB);
                annihilation.transform(fabric);
            }
        }
    }
}