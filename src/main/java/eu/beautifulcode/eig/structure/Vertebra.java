/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

import eu.beautifulcode.eig.math.Arrow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Vertebra {
    List<Joint> joints = new ArrayList<Joint>();
    boolean rightHanded;

    public Vertebra(boolean rightHanded) {
        this.rightHanded = rightHanded;
    }

    public Vertebra() {
    }

    public List<Joint> getJoints() {
        return joints;
    }

    public boolean isRightHanded() {
        return rightHanded;
    }

    public void getLocation(Arrow location) {
        location.zero();
        for (Joint joint : joints) {
            location.add(joint.location);
        }
        location.scale(1.0 / joints.size());
    }

    public void replace(Joint jointFrom, Joint jointTo) {
        int count = joints.size();
        for (int walk = 0; walk < count; walk++) {
            if (joints.get(walk) == jointFrom) {
                joints.set(walk, jointTo);
            }
        }
    }

    public List<Joint> getJoints(boolean alpha) {
        List<Joint> list = new ArrayList<Joint>(joints.size()/2);
        int offset = alpha ? 0 : joints.size()/2;
        for (int walk=0; walk<joints.size()/2; walk++) {
            list.add(joints.get(walk+offset));
        }
        if (rightHanded) {
            list.add(list.remove(0));
        }
        else {
            list.add(0, list.remove(list.size()-1));
        }
        return list;
    }
}