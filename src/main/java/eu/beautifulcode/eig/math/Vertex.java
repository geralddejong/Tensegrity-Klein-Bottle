/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.math;

import java.util.ArrayList;
import java.util.List;

/**
 * One vertext of a sphere
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Vertex<Type> {
    int index;
    Arrow location;
    List<Vertex<Type>> nears;
    Type occupant;

    Vertex(int index, Arrow location, boolean isCorner) {
        this.index = index;
        nears = new ArrayList<Vertex<Type>>(isCorner ? 5 : 6);
        this.location = location;
    }

    public double getMagnification() {
        return location.span();
    }

    public interface Visitor<Type> {
        void visit(Vertex<Type> vertex);
    }

    public Type getOccupant() {
        return occupant;
    }

    public void setOccupant(Type occupant) {
        this.occupant = occupant;
    }

    public int getIndex() {
        return index;
    }

    public Arrow getLocation() {
        return location;
    }

    public void setAltitude(double altitude) {
        location.normalize();
        location.scale(altitude);
    }

    public double getAltitude() {
        return location.span();
    }

    public Vertex getFront(Vertex vertex) {
        if (nears.size() == 5) return (null);
        return nears.get((nears.indexOf(vertex) + nears.size() / 2) % nears.size());
    }

    public Vertex<Type> getLeftFront(Vertex<Type> vertex) {
        return nears.get((nears.indexOf(vertex) + nears.size() / 2 + (nears.size() == 6 ? -1 : 1)) % nears.size());
    }

    public Vertex<Type> getRightFront(Vertex<Type> vertex) {
        return nears.get((nears.indexOf(vertex) + nears.size() / 2 + (nears.size() == 6 ? 1 : -1)) % nears.size());
    }

    public Vertex<Type> getLeftBack(Vertex<Type> vertex) {
        return nears.get((nears.indexOf(vertex) + 1) % nears.size());
    }

    public Vertex<Type> getRightBack(Vertex<Type> vertex) {
        return nears.get((nears.indexOf(vertex) + nears.size() - 1) % nears.size());
    }

    public List<Vertex<Type>> getNearby() {
        return nears;
    }

    public boolean isCorner() {
        return nears.size() == 5;
    }

    void connectTo(Vertex<Type> vertex) {
        nears.add(vertex);
    }

    void connectWith(Vertex<Type> theNearPoint) {
        connectTo(theNearPoint);
        theNearPoint.connectTo(this);
    }

    void sort(Arrow a, Arrow b, boolean clockwise) {
        for (int walk = 0; walk < nears.size() - 1; walk++) {
            a.sub(nears.get(walk).location, location);
            a.normalize();
            double maxDot = 0;
            int best = -1;
            for (int scan = 0; scan < nears.size(); scan++) {
                if (scan == walk) continue;
                b.sub(nears.get(scan).location, location);
                b.normalize();
                double dot = a.dot(b);
                if (dot > maxDot) {
                    if (clockwise) {
                        b.cross(b, a);
                    }
                    else {
                        b.cross(a, b);
                    }
                    if (b.dot(location) < 0) {
                        best = scan;
                        maxDot = dot;
                    }
                }
            }
            if (best < 0) throw new RuntimeException(this.toString());
            if (best != walk + 1) {
                Vertex<Type> temp = nears.get(best);
                nears.set(best, nears.get(walk + 1));
                nears.set(walk + 1, temp);
            }
        }
    }

    public String toString() {
        return (String.valueOf(index));
    }
}
