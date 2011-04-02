/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.transform;

import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Interval;
import eu.beautifulcode.eig.structure.Joint;
import eu.beautifulcode.eig.structure.Vertebra;

import java.util.Collections;
import java.util.List;

/**
 * Open up a triangular face like a book creating two new faces and moving the original
 * back cover of the book to be front cover.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ConnectVertebra implements Fabric.Transformation {
    private List<Joint> alphaRing;
    private List<Joint> omegaRing;

    /**
     * Connect the omega ring of the alpha vertebra to the alpha ring of the omega vertebra
     *
     * @param alphaVertebra where to start
     * @param omegaVertebra where to end
     * @param reversed flip to make a klein bottle
     */

    public ConnectVertebra(Vertebra alphaVertebra, Vertebra omegaVertebra, boolean reversed) {
        this.alphaRing = alphaVertebra.getJoints(false);
        this.omegaRing = omegaVertebra.getJoints(true);
        if (reversed) {
            Collections.reverse(this.alphaRing);
        }
        else {
            this.alphaRing.add(this.alphaRing.remove(0));
        }
    }

    public void transform(Fabric fabric) {
        for (int walk = 0; walk < alphaRing.size(); walk++) {
            Interval connect = fabric.createInterval(alphaRing.get(walk), omegaRing.get(walk), Interval.Role.TEMP);
            fabric.getMods().getIntervalMod().add(connect);
        }
    }
}