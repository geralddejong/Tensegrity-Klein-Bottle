/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */

package org.darwinathome.geometry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;

import eu.beautifulcode.eig.gui.Positioner;
import eu.beautifulcode.eig.jogl.EllipsoidPainter;
import eu.beautifulcode.eig.jogl.FacePainter;
import eu.beautifulcode.eig.jogl.Floor;
import eu.beautifulcode.eig.jogl.GLRenderer;
import eu.beautifulcode.eig.jogl.GLViewPlatform;
import eu.beautifulcode.eig.jogl.JointWhoPainter;
import eu.beautifulcode.eig.jogl.PointOfView;
import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Face;
import eu.beautifulcode.eig.structure.Interval;
import eu.beautifulcode.eig.structure.Joint;
import eu.beautifulcode.eig.structure.Physics;
import eu.beautifulcode.eig.structure.SimpleFabricFactory;
import eu.beautifulcode.eig.structure.Span;
import eu.beautifulcode.eig.structure.VerticalPhysicsConstraints;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static javax.media.opengl.GL2.*;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */


public class ViewTest extends Frame {
    private static final float LIGHT_POSITION[] = {1f, 0.1f, 2f, 0.5f};
    private Physics physics = new Physics(new VerticalPhysicsConstraints());
    private GLCanvas canvas = new GLCanvas();
    private Floor floor = new Floor();
    private PointOfView pointOfView = new PointOfView(7);
    private Queue<Runnable> jobs = new ConcurrentLinkedQueue<Runnable>();
    private Positioner positioner = new Positioner(jobs, pointOfView);
    private Fabric fabric;
    private boolean running = true;

    public ViewTest() {
        physics.setIterations(9);
        GLViewPlatform viewPlatform = new GLViewPlatform(new Renderer(), pointOfView, 1, 180);
//        floor.setMiddle(pointOfView.getFocus());
        canvas.setFocusable(true);
        canvas.addGLEventListener(viewPlatform);
        canvas.requestFocus();
        canvas.addKeyListener(positioner.getKeyListener());
        canvas.addMouseListener(positioner.getMouseListener());
        canvas.addMouseMotionListener(positioner.getMouseMotionListener());
        canvas.addMouseWheelListener(positioner.getMouseWheelListener());
        add(canvas, BorderLayout.CENTER);
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        size.height -= 200;
        setSize(size);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                running = false;
            }
        });
    }

    public Physics getPhysics() {
        return physics;
    }

    public void kill() {
        running = false;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Thread.currentThread().interrupt();
            }
        });
    }

    public PointOfView getPointOfView() {
        return pointOfView;
    }

    private class StressRange implements Span.StressRange {
        public double minimum() {
            return -0.03;
        }

        public double maximum() {
            return 0.03;
        }
    }

    private class Renderer implements GLRenderer {
        private Arrow center = new Arrow();
        private EllipsoidPainter ellipsoidPainter = new EllipsoidPainter(new StressRange());
//        private TetraPainter tetraPainter = new TetraPainter();
        private JointWhoPainter jointWhoPainter = new JointWhoPainter(pointOfView);
        private FacePainter facePainter = new FacePainter(new StressRange());

        public void init(GL2 gl) {
            ellipsoidPainter.setWidth(0.02);
        }

        public void display(GL2 gl, int width, int height) {
            gl.glLightfv(GL_LIGHT0, GL_POSITION, LIGHT_POSITION, 0);
            Fabric fabric = ViewTest.this.fabric;
            if (fabric != null) {
                ellipsoidPainter.preVisit(gl);
                for (Interval interval : fabric.getIntervals()) {
                    if (interval.getRole() != Interval.Role.GONE) {
                        ellipsoidPainter.visit(interval);
                    }
                }
                facePainter.preVisit(gl);
                for (Face face : fabric.getFaces()) {
                    facePainter.visit(face);
                }
//                tetraPainter.preVisit(gl);
//                for (Tetra tetra : fabric.getTetras()) {
//                    tetraPainter.visit(tetra);
//                }
//                tetraPainter.postVisit();
                while (!jobs.isEmpty()) {
                    jobs.remove().run();
                }
                jointWhoPainter.preVisit(gl);
                for (Joint joint : fabric.getJoints()) {
                    jointWhoPainter.visit(joint);
                }
                fabric.executeTransformations(physics);
                fabric.getCenter(center);
                pointOfView.moveFocusTowardsIdeal(center, 0.02);
            }
            positioner.run();
            floor.display(gl);
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

    public void iterate() {
        if (!isVisible()) return;
        if (!canvas.hasFocus()) {
            canvas.requestFocus();
        }
        canvas.display();
    }

    public boolean isRunning() {
        return running;
    }

    public void submit(Fabric.Transformation transformation) {
        fabric.addTransformation(transformation);
    }

    public void setFabric(Fabric fabric) {
        this.fabric = fabric;
    }

    public Fabric getFabric() {
        return fabric;
    }

    public static void main(String[] args) {
        ViewTest viewTest = new ViewTest();
        pause(100);
        SimpleFabricFactory factory = new SimpleFabricFactory(null);
        Fabric fabric = factory.createOctahedron();
        viewTest.setFabric(fabric);
        viewTest.setVisible(true);
        while (viewTest.running) {
            pause(10);
            viewTest.iterate();
        }
        System.exit(0);
    }
}