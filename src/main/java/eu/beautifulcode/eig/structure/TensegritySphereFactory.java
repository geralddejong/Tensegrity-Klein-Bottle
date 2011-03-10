/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.math.Space3;
import eu.beautifulcode.eig.math.Sphere;
import eu.beautifulcode.eig.math.Vertex;

/**
 * Build a fabric in the form of a tensegrity sphere
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TensegritySphereFactory {
    private Thing.Factory thingFactory;
    private double barExtend = 1.2;
    private double barTwist = 0.52;
    private double relax = 1.5;
    private double circles = 0.93;
    private double connector = 0.55;

    public TensegritySphereFactory(Thing.Factory thingFactory) {
        this.thingFactory = thingFactory;
    }

    public TensegritySphereFactory setBarExtend(double barExtendFactor) {
        this.barExtend *= barExtendFactor;
        return this;
    }

    public TensegritySphereFactory setBarTwist(double barTwistFactor) {
        this.barTwist *= barTwistFactor;
        return this;
    }

    public TensegritySphereFactory setRelax(double relaxFactor) {
        this.relax *= relaxFactor;
        return this;
    }

    public TensegritySphereFactory setCircles(double circlesFactor) {
        this.circles *= circlesFactor;
        return this;
    }

    public TensegritySphereFactory setConnector(double connectorFactor) {
        this.connector *= connectorFactor;
        return this;
    }

    public Fabric createSphere(int frequency, double radiusFactor) {
        final Fabric fabric = new Fabric(thingFactory);
        Sphere<Node> sphere = new Sphere<Node>(frequency);
        Vertex<Node> corner = sphere.getCorner(0);
        sphere.setRadius(radiusFactor / corner.getLocation().distanceTo(corner.getNearby().get(0).getLocation()));
        sphere.admitVisitor(new Vertex.Visitor<Node>() {
            public void visit(Vertex<Node> vertex) {
                vertex.setOccupant(new Node(fabric, vertex));
            }
        });
        sphere.admitVisitor(new Vertex.Visitor<Node>() {
            public void visit(Vertex<Node> vertex) {
                vertex.getOccupant().createBars(barExtend, barTwist);
            }
        });
        sphere.admitVisitor(new Vertex.Visitor<Node>() {
            public void visit(Vertex<Node> vertex) {
                vertex.getOccupant().createCircles(relax * circles / vertex.getNearby().size());
            }
        });
        sphere.admitVisitor(new Vertex.Visitor<Node>() {
            public void visit(Vertex<Node> vertex) {
                vertex.getOccupant().createConnectors(relax * connector);
            }
        });
        return fabric;
    }

    private static class Node {
        private Fabric fabric;
        private Vertex<Node> vertex;
        private Joint[] joints;
        private Bow[] bows;

        private Node(Fabric fabric, Vertex<Node> vertex) {
            this.fabric = fabric;
            this.vertex = vertex;
            this.joints = new Joint[size()];
            this.bows = new Bow[size()];
            for (int walk = 0; walk < size(); walk++) {
                Joint joint = new Joint(fabric.who().createMiddle(), vertex.getLocation());
                fabric.joints.add(joint);
                joints[walk] = joint;
            }
        }

        private int size() {
            return vertex.getNearby().size();
        }

        private Node node(int vertexIndex) {
            return vertex.getNearby().get((vertexIndex + size()) % size()).getOccupant();
        }

        private int index(Node node) {
            for (int walk = 0; walk < size(); walk++) {
                if (node == node(walk)) {
                    return walk;
                }
            }
            throw new IllegalArgumentException();
        }

        private Joint joint(int vertexIndex) {
            return joints[vertexIndex % size()];
        }

        private Joint joint(Node node) {
            return joints[index(node)];
        }

        private void setBow(Node node, Bow bow) {
            bows[index(node)] = bow;
        }

        private Bow bow(Node node) {
            return bows[index(node)];
        }

        public void createBars(double factor, double angle) {
            for (int walk = 0; walk < size(); walk++) {
                Node otherNode = node(walk);
                if (bow(otherNode) == null) {
                    Bow bow = bows[walk] = new Bow();
                    fabric.intervals.add(bow.createBar(joints[walk], otherNode.joint(this), factor));
                    otherNode.setBow(this, bow);
                    bow.twist(angle);
                }
            }
        }

        public void createCircles(double factor) {
            for (int walk = 0; walk < size(); walk++) {
                Interval cable = bows[walk].createEndCable(joint(walk), joint(walk + 1));
                cable.getSpan().setIdeal(cable.getSpan().getCurrentIdeal() * factor, 0);
                fabric.intervals.add(cable);
            }
        }

        public void createConnectors(double factor) {
            for (int walk = 0; walk < size(); walk++) {
                Interval cable = bows[walk].createMiddleCable();
                if (cable != null) {
                    cable.getSpan().setIdeal(cable.getSpan().getCurrentIdeal() * factor, 0);
                    fabric.intervals.add(cable);
                }
            }
        }
    }

    private static class Bow {
        Interval bar, alphaCable, middleCable, omegaCable;

        Interval createBar(Joint alpha, Joint omega, double factor) {
            bar = new Interval(alpha, omega, Interval.Role.BAR);
            bar.span.setIdeal(bar.span.getCurrentIdeal() * factor, 0);
            Arrow unit = bar.getUnit(true);
            double difference = bar.span.getCurrentIdeal() - bar.span.getActual();
            bar.omega.location.add(unit, difference / 2);
            bar.alpha.location.sub(unit, difference / 2);
            return bar;
        }

        void twist(double angle) {
            Arrow axis = new Arrow();
            bar.getLocation(axis);
            axis.normalize();
            Space3 space3 = new Space3();
            space3.set(axis, -angle);
            space3.transform(bar.alpha.getLocation());
            space3.transform(bar.omega.getLocation());
            bar.getUnit(true);
        }

        Interval createEndCable(Joint a, Joint b) {
            if (bar.alpha == a) {
                return alphaCable = cable(a, b);
            }
            else if (bar.omega == a) {
                return omegaCable = cable(b, a);
            }
            else if (bar.alpha == b) {
                return alphaCable = cable(b, a);
            }
            else if (bar.omega == b) {
                return omegaCable = cable(a, b);
            }
            else {
                throw new RuntimeException();
            }
        }

        Interval createMiddleCable() {
            if (middleCable == null) {
                return cable(alphaCable.omega, omegaCable.alpha);
            }
            else {
                return null;
            }
        }

        Interval cable(Joint a, Joint b) {
            return new Interval(a, b, Interval.Role.CABLE);
        }

    }
}