/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package eu.beautifulcode.eig.structure;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class FactoryTest {
//    private static final double ACCURACY = 0.001;
//    private static final Tetra.PullVector PULL_VECTOR = new Tetra.PullVector(2, 3, 4, 3, 1.6);
//    private SimpleFabricFactory factory = new SimpleFabricFactory();

//    @Test
//    public void threeBar() throws Exception {
//        factory.createThreeBarTensegrity(PULL_VECTOR, true);
//        Fabric f = factory.getFabric();
//        assertEquals(6, f.joints.size());
//        assertEquals(15, f.intervals.size());
//        assertEquals(3, f.tetras.size());
//        for (Joint joint : f.joints) {
//            assertTrue(joint.location.z >= 0);
//        }
//        Physics physics = Physics.createVerticalPhysics();
//        for (Interval interval : f.intervals) {
//            if (interval.role == Interval.Role.BAR) {
//                Arrow alphaOmega = new Arrow();
//                interval.getAlphaOmega(alphaOmega);
//                assertEquals(1, alphaOmega.span(), ACCURACY);
//            }
//        }
//        f.addTransformation(physics);
//        f.executeTransformations();
//        assertEquals(1, f.vertebras.size());
//        for (int walk=0; walk<1000; walk++) {
//            f.addTransformation(physics);
//            f.executeTransformations();
//        }
//    }
}