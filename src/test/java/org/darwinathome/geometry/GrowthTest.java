/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package org.darwinathome.geometry;

import org.apache.log4j.Logger;
import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Face;
import eu.beautifulcode.eig.structure.Interval;
import eu.beautifulcode.eig.structure.SimpleFabricFactory;
import eu.beautifulcode.eig.transform.AboveFloor;
import eu.beautifulcode.eig.transform.JointMerge;
import eu.beautifulcode.eig.transform.OpenUp;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class GrowthTest {
    private static final Logger log = Logger.getLogger(GrowthTest.class);
    private static ViewTest viewTest = new ViewTest();
    private static Fabric fabric;
    private Queue<Runnable> jobs = new LinkedList<Runnable>();

    @BeforeClass
    public static void up() throws Exception {
        viewTest.setVisible(true);
        fabric = viewTest.getFabric();
    }

    @AfterClass
    public static void down() throws Exception {
        int count = 1500;
        while (count-- > 0) {
            pause(2);
            viewTest.iterate();
        }
    }

    @Test
    public void pentagonShape() throws Exception {
        SimpleFabricFactory factory = new SimpleFabricFactory(null);
        viewTest.setFabric(fabric = factory.createDoubleFaceTriangle());
        Assert.assertEquals(3, fabric.getIntervals().size());
        fabric.addTransformation(new AboveFloor());
        fabric.executeTransformations(null);
        Assert.assertEquals(2, fabric.getFaces().size());
        Open open = new Open();
        open.setFace(fabric.getFaces().get(0));
        Open open01 = open.setOpen01(new Open());
        Open open0101 = open01.setOpen01(new Open());
        Open open010112 = open0101.setOpen12(new Open());
        open010112.setOpen12(new Open());
        jobs.add(open);
        iterate();
        Assert.assertEquals(18, fabric.getIntervals().size());
        Assert.assertEquals(8, fabric.getJoints().size());
        Assert.assertEquals(12, fabric.getFaces().size());
        fabric.addTransformation(new JointMerge.Periodic(10));
        iterate();
        for (Interval interval : fabric.getIntervals()) {
            log.info(interval);
        }
        Assert.assertEquals(16, fabric.getIntervals().size());
        Assert.assertEquals(7, fabric.getJoints().size());
        Assert.assertEquals(10, fabric.getFaces().size());
    }

    @Test
    public void icosahedron() throws Exception {
        SimpleFabricFactory factory = new SimpleFabricFactory(null);
        viewTest.setFabric(fabric = factory.createDoubleFaceTriangle());
        fabric.addTransformation(new AboveFloor());
        iterate();
        Open open = new Open();
        open.setFace(fabric.getFaces().get(0));
        Open open01 = open.setOpen01(new Open());
        Open open0101 = open01.setOpen01(new Open());
        Open open010101 = open0101.setOpen01(new Open());
        Open open01010112 = open010101.setOpen12(new Open());
        Open open0101011220 = open01010112.setOpen20(new Open());
        Open open010112 = open0101.setOpen12(new Open());
        Open open12 = open.setOpen12(new Open());
        Open open1201 = open12.setOpen01(new Open());
        Open open120101 = open1201.setOpen01(new Open());
        Open open12010112 = open120101.setOpen12(new Open());
        Open open1201011220 = open12010112.setOpen20(new Open());
        Open open120112 = open1201.setOpen12(new Open());
        Open open20 = open.setOpen20(new Open());
        Open open2001 = open20.setOpen01(new Open());
        Open open200101 = open2001.setOpen01(new Open());
        Open open20010112 = open200101.setOpen12(new Open());
        Open open2001011220 = open20010112.setOpen20(new Open());
        Open open200101122012 = open2001011220.setOpen12(new Open()); // todo
        Open open200112 = open2001.setOpen12(new Open());

//        Open open01 = open.setOpen01(new Open());
//        Open open0101 = open01.setOpen01(new Open());
//        Open open010101 = open0101.setOpen01(new Open());
//        Open open12 = open.setOpen12(new Open());
//        Open open1201 = open12.setOpen01(new Open());
//        Open open120120 = open1201.setOpen20(new Open());
//        Open open120101 = open120120.setOpen20(new Open());
//        Open open1212 = open12.setOpen12(new Open());
//        Open open20 = open.setOpen20(new Open());
//        Open open2001 = open20.setOpen01(new Open());
//        Open open2012 = open20.setOpen12(new Open());
//        Open open201212 = open2012.setOpen12(new Open());
//        Open open20121201 = open201212.setOpen01(new Open());
        jobs.add(open);
        iterate();
        Assert.assertEquals(20, fabric.getTetras().size());
        iterate();
        fabric.addTransformation(new JointMerge.Periodic(20));
        iterate();
        fabric.addTransformation(new JointMerge.Periodic(20));
        iterate();
        fabric.addTransformation(new JointMerge.Periodic(20));
        iterate();
        for (Interval interval : fabric.getIntervals()) {
            log.info(interval);
        }
        Assert.assertEquals(42, fabric.getIntervals().size());
        Assert.assertEquals(13, fabric.getJoints().size());
        Assert.assertEquals(20, fabric.getFaces().size());
        Assert.assertEquals(20, fabric.getTetras().size());
    }

    private class Open implements OpenUp.Callback, Runnable {
        private Face face;
        private Open open01, open12, open20;

        private Open() {
        }

        public Open setOpen01(Open open) {
            this.open01 = open;
            return open;
        }

        public Open setOpen12(Open open) {
            this.open12 = open;
            return open;
        }

        public Open setOpen20(Open open) {
            this.open20 = open;
            return open;
        }

        public void setFace(Face face) {
            this.face = face;
        }

        public void faces(Face face01, Face face12, Face face20) {
            fire(open01, face01);
            fire(open12, face12);
            fire(open20, face20);
        }

        private void fire(Open open, Face face) {
            if (open != null) {
                open.setFace(face);
                jobs.add(open);
            }
        }

        public void run() {
            OpenUp openUp = new OpenUp(face, 1, 1800, Interval.Role.SPRING);
            openUp.setCallback(this);
            fabric.addTransformation(openUp);
//            fabric.executeTransformations(null);
        }
    }

    private void iterate() {
        viewTest.iterate();
        while (true) {
            pause(1);
            viewTest.iterate();
            if (!fabric.isAnySpanActive()) {
                if (!jobs.isEmpty()) {
                    jobs.remove().run();
                    continue;
                }
                break;
            }
        }
    }

    private static void pause(long time) {
        try {
            Thread.sleep(time);
        }
        catch (InterruptedException e) {
            // eat it
        }
    }
}