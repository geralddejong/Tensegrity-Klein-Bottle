/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.math;

import java.util.ArrayList;
import java.util.List;

/**
 * This generic class creates a spherical data structure with vertexes of a given type.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Sphere<Type> {
    private int frequency, beads;
    private List<Vertex<Type>> vertexes;

    public Sphere(int frequency) {
        this.frequency = frequency;
        beads = frequency - 1;
        vertexes = new ArrayList<Vertex<Type>>(VERTEX.length + EDGE.length * beads + FACE.length * (beads * (beads - 1)) / 2);
        buildIcosa();
        if (beads == 0) {
            build30Edges();
        }
        else if (beads == 1) {
            List<Vertex<Type>> edges = build60Edges();
            buildSmallFaces(edges);
        }
        else {
            List<List<Vertex<Type>>> edges = buildEdges();
            buildFaces(edges);
        }
        Arrow a = new Arrow();
        Arrow b = new Arrow();
        for (Vertex<Type> vertex : vertexes) {
            vertex.sort(a, b, false);
        }
        setRadius(1);
        Space3 rot = new Space3();
        rot.setRotationY(Math.acos(PHI));
        for (Vertex<Type> vertex : vertexes) {
            rot.transform(vertex.location);
        }
    }

    public void setRadius(double radius) {
        for (Vertex<Type> vertex : vertexes) {
            vertex.setAltitude(radius);
        }
    }

    public int getVertexCount() {
        return vertexes.size();
    }

    public int getEdgeCount() {
        return (getVertexCount() * 6 - 12) / 2;
    }

    public int getFaceCount() {
        return getEdgeCount() + 2 - getVertexCount();
    }

    public Vertex<Type> getVertex(int index) {
        return vertexes.get(index);
    }

    public Vertex<Type> getVertexNearest(Arrow arrow) {
        double nearestDistance = Double.POSITIVE_INFINITY;
        Vertex<Type> nearest = null;
        for (Vertex<Type> vertex : vertexes) {
            double distance = vertex.getLocation().distanceTo(arrow);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = vertex;
            }
        }
        return nearest;
    }

    public int getConnectionCount() {
        return ((getVertexCount() * 6 - 12) / 2);
    }

    public void admitVisitor(Vertex.Visitor<Type> visitor) {
        for (Vertex<Type> vertex : vertexes) {
            visitor.visit(vertex);
        }
    }

    public Vertex<Type> getCorner(int index) {
        if (index >= 12 || index < 0) throw new RuntimeException("12 corners: 0-11");
        return vertexes.get(index);
    }

    public int getFrequency() {
        return frequency;
    }

    private void buildIcosa() {
        for (double[] loc : VERTEX) {
            vertexes.add(new Vertex<Type>(vertexes.size(), new Arrow(loc[0], loc[1], loc[2]), true));
        }
    }

    private void build30Edges() {
        for (int[] edge : EDGE) {
            vertexes.get(edge[0]).connectWith(vertexes.get(edge[1]));
        }
    }

    private List<Vertex<Type>> build60Edges() {
        List<Vertex<Type>> edges = new ArrayList<Vertex<Type>>();
        for (int[] edge : EDGE) {
            Arrow spot = new Arrow();
            spot.interpolate(vertexes.get(edge[0]).location, vertexes.get(edge[1]).location, 0.5f);
            Vertex<Type> vertex = new Vertex<Type>(vertexes.size(), spot, false);
            vertexes.add(vertex);
            edges.add(vertex);
            vertexes.get(edge[0]).connectWith(vertex);
            vertex.connectWith(vertexes.get(edge[1]));
        }
        return edges;
    }

    private List<List<Vertex<Type>>> buildEdges() {
        List<List<Vertex<Type>>> edgePoints = new ArrayList<List<Vertex<Type>>>();
        for (int[] edge : EDGE) {
            Vertex<Type> vertex = null, prevPoint;
            List<Vertex<Type>> edgeVertexRows = new ArrayList<Vertex<Type>>();
            edgePoints.add(edgeVertexRows);
            for (int walkBeads = 0; walkBeads < beads; walkBeads++) {
                prevPoint = vertex;
                Arrow spot = new Arrow();
                spot.interpolate(vertexes.get(edge[0]).location, vertexes.get(edge[1]).location, ((double) walkBeads + 1) / ((double) (beads + 1)));
                vertex = new Vertex<Type>(vertexes.size(), spot, false);
                vertexes.add(vertex);
                edgeVertexRows.add(vertex);
                if (prevPoint == null) {
                    vertex.connectWith(vertexes.get(edge[0]));
                }
                else {
                    vertex.connectWith(prevPoint);
                    if (walkBeads == beads - 1) {
                        vertex.connectWith(vertexes.get(edge[1]));
                    }
                }
            }
        }
        for (int[][] penta : PENTA) {
            for (int walk = 0; walk < penta.length; walk++) {
                int next = (walk + 1) % penta.length;
                int walkBead = (penta[walk][1] == 1) ? 0 : (beads - 1);
                int nextBead = (penta[next][1] == 1) ? 0 : (beads - 1);
                Vertex<Type> currPoint = edgePoints.get(penta[walk][0]).get(walkBead);
                Vertex<Type> nextVertex = edgePoints.get(penta[next][0]).get(nextBead);
                currPoint.connectWith(nextVertex);
            }
        }
        return edgePoints;
    }

    private void buildSmallFaces(List<Vertex<Type>> edges) {
        for (int[] faceEdge : FACE_EDGE) { // just connect the triangle in the middle of the face
            Vertex<Type> side0 = edges.get(Math.abs(faceEdge[0]));
            Vertex<Type> side1 = edges.get(Math.abs(faceEdge[1]));
            Vertex<Type> side2 = edges.get(Math.abs(faceEdge[2]));
            side0.connectWith(side1);
            side1.connectWith(side2);
            side2.connectWith(side0);
        }
    }

    private void buildFaces(List<List<Vertex<Type>>> edges) {
        List<List<Vertex<Type>>> v = new ArrayList<List<Vertex<Type>>>(beads - 1);
        for (int walk = 0; walk < beads - 1; walk++) {
            v.add(new ArrayList<Vertex<Type>>(beads - 1 - walk));
        }
        Arrow vectorA = new Arrow();
        Arrow vectorB = new Arrow();
        for (int walkF = 0; walkF < FACE.length; walkF++) {
            Arrow origin = vertexes.get(FACE[walkF][0]).location;
            for (int walkA = 1; walkA < beads; walkA++) {
                vectorA.interpolate(origin, vertexes.get(FACE[walkF][1]).location, ((double) walkA) / (beads + 1));
                vectorA.sub(origin);
                List<Vertex<Type>> va = v.get(walkA - 1);
                va.clear();
                for (int walkB = 1; walkB < beads - walkA + 1; walkB++) {
                    vectorB.interpolate(origin, vertexes.get(FACE[walkF][2]).location, ((double) walkB) / (beads + 1));
                    vectorB.sub(origin);
                    Arrow spot = new Arrow(origin);
                    spot.add(vectorA);
                    spot.add(vectorB);
                    Vertex<Type> vertex = new Vertex<Type>(vertexes.size(), spot, false);
                    vertexes.add(vertex);
                    va.add(vertex);
                }
            }
            for (int walkRow = 0; walkRow < v.size(); walkRow++) {
                for (int walk = 0; walk < v.get(walkRow).size(); walk++) {
                    if (walk < v.get(walkRow).size() - 1) {
                        v.get(walkRow).get(walk).connectWith(v.get(walkRow).get(walk + 1));
                    }
                    if (walkRow > 0) {
                        v.get(walkRow).get(walk).connectWith(v.get(walkRow - 1).get(walk));
                        v.get(walkRow).get(walk).connectWith(v.get(walkRow - 1).get(walk + 1));
                    }
                }
            }
            List<Vertex<Type>> vv0 = new ArrayList<Vertex<Type>>(v.size());
            List<Vertex<Type>> vv1 = new ArrayList<Vertex<Type>>(v.size());
            List<Vertex<Type>> vv2 = new ArrayList<Vertex<Type>>(v.size());
            for (int walk = 0; walk < beads - 1; walk++) {
                int antiWalk = v.size() - walk - 1;
                vv0.add(v.get((FACE_EDGE[walkF][0] >= 0) ? walk : antiWalk).get(0));
                List<Vertex<Type>> ee = v.get((FACE_EDGE[walkF][1] < 0) ? walk : antiWalk);
                vv1.add(ee.get(ee.size() - 1));
                vv2.add(v.get(0).get((FACE_EDGE[walkF][2] < 0) ? walk : antiWalk));
            }
            List<List<Vertex<Type>>> vs = new ArrayList<List<Vertex<Type>>>();
            vs.add(vv0);
            vs.add(vv1);
            vs.add(vv2);
            for (int walkSide = 0; walkSide < vs.size(); walkSide++) {
                List<Vertex<Type>> edge = edges.get(Math.abs(FACE_EDGE[walkF][walkSide]));
                for (int walk = 0; walk < v.size(); walk++) {
                    vs.get(walkSide).get(walk).connectWith(edge.get(walk));
                    vs.get(walkSide).get(walk).connectWith(edge.get(walk + 1));
                }
            }
        }
    }

    static void check(Object a, Object b) {
        if (a != b) {
            throw new RuntimeException();
        }
    }

    private static final double NUL = 0.0f;
    private static final double ONE = 0.5257311121191336f;
    private static final double PHI = 0.8506508083520400f;
    private static final double[][] VERTEX = {
            {+ONE, NUL, +PHI}, {+ONE, NUL, -PHI},
            {+PHI, +ONE, NUL}, {-PHI, +ONE, NUL},
            {NUL, +PHI, +ONE}, {NUL, -PHI, +ONE},
            {-ONE, NUL, -PHI}, {-ONE, NUL, +PHI},
            {-PHI, -ONE, NUL}, {+PHI, -ONE, NUL},
            {NUL, -PHI, -ONE}, {NUL, +PHI, -ONE},
    }; // 0-
    private static final int[][] EDGE = {
            {0, 2}, {0, 4}, {0, 5}, {0, 7}, {0, 9},
            {1, 10}, {1, 11}, {1, 2}, {1, 6}, {1, 9},
            {2, 11}, {2, 4}, {2, 9}, {3, 11}, {3, 4},
            {3, 6}, {3, 7}, {3, 8}, {4, 11}, {4, 7},
            {5, 10}, {5, 7}, {5, 8}, {5, 9}, {6, 10},
            {6, 11}, {6, 8}, {7, 8}, {8, 10}, {9, 10},
    };
    private static final int[][] FACE = {
            {0, 2, 4}, {0, 2, 9}, {0, 4, 7}, {0, 5, 7}, {0, 5, 9},
            {1, 2, 11}, {1, 2, 9}, {1, 6, 10}, {1, 6, 11}, {1, 9, 10},
            {2, 4, 11}, {3, 4, 11}, {3, 4, 7}, {3, 6, 11}, {3, 6, 8},
            {3, 7, 8}, {5, 7, 8}, {5, 8, 10}, {5, 9, 10}, {6, 8, 10},
    };
    private static final int[][] FACE_EDGE = {
            {0, 11, -1}, {0, 12, -4}, {1, 19, -3}, {2, 21, -3}, {2, 23, -4},
            {7, 10, -6}, {7, 12, -9}, {8, 24, -5}, {8, 25, -6}, {9, 29, -5},
            {11, 18, -10}, {14, 18, -13}, {14, 19, -16}, {15, 25, -13}, {15, 26, -17},
            {16, 27, -17}, {21, 27, -22}, {22, 28, -20}, {23, 29, -20}, {26, 28, -24},
    };
    private static final int[][][] PENTA = {
            {{0, 1}, {1, 1}, {3, 1}, {2, 1}, {4, 1}},
            {{7, 1}, {6, 1}, {8, 1}, {5, 1}, {9, 1}},
            {{10, 1}, {11, 1}, {0, -1}, {12, 1}, {7, -1}},
            {{14, 1}, {13, 1}, {15, 1}, {17, 1}, {16, 1}},
            {{18, 1}, {11, -1}, {1, -1}, {19, 1}, {14, -1}},
            {{21, 1}, {22, 1}, {20, 1}, {23, 1}, {2, -1}},
            {{26, 1}, {24, 1}, {8, -1}, {25, 1}, {15, -1}},
            {{27, 1}, {16, -1}, {19, -1}, {3, -1}, {21, -1}},
            {{28, 1}, {22, -1}, {27, -1}, {17, -1}, {26, -1}},
            {{4, -1}, {23, -1}, {29, 1}, {9, -1}, {12, -1}},
            {{28, -1}, {20, -1}, {29, -1}, {5, -1}, {24, -1}},
            {{6, -1}, {10, -1}, {18, -1}, {13, -1}, {25, -1}}
    };

}

