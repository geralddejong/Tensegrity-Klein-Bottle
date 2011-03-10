/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.transform;

import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Joint;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class AboveFloor implements Fabric.Transformation {
    private double height = 0;

    public AboveFloor() {
    }

    public AboveFloor(double height) {
        this.height = height;
    }

    public void transform(Fabric fabric) {
        double lowest = 0;
        double centerX = 0;
        double centerY = 0;
        for (Joint joint : fabric.getJoints()) {
            centerX += joint.getLocation().x;
            centerY += joint.getLocation().y;
            if (joint.getLocation().z < lowest) {
                lowest = joint.getLocation().z;
            }
        }
        double averageX = centerX/fabric.getJoints().size();
        double averageY = centerY/fabric.getJoints().size();
        for (Joint joint : fabric.getJoints()) {
            joint.getLocation().x -= averageX;
            joint.getLocation().y -= averageY;
            joint.getLocation().z += height - lowest;
            joint.getVelocity().zero();
        }
    }
}