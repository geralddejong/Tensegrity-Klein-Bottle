package org.darwinathome.geometry;

import eu.beautifulcode.eig.gui.Orbiter;
import eu.beautifulcode.eig.jogl.GLRenderer;
import eu.beautifulcode.eig.jogl.GLViewPlatform;
import eu.beautifulcode.eig.jogl.PointOfView;
import eu.beautifulcode.eig.jogl.Tint;
import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.math.Sphere;
import eu.beautifulcode.eig.math.Vertex;

import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

/**
 * @author Gerald de Jong
 * @version $Revision: 1.2 $
 */

public class ConwayLifeSphere extends Frame {
    private static final double RADIANS_TO_DEGREES = 180 / Math.PI;
    private static final int SPHERE_FREQUENCY = 60;
    private static final double SPHERE_RADIUS = 2;
    private static final double ALTITUDE = 0.4;
    private static final double VERTEX_RADIUS = 3 * SPHERE_RADIUS / SPHERE_FREQUENCY;
    private static final double VISIBLE_VERTEX_DOT_PRODUCT = 0.5;
    private static final Sphere<Occupant> SPHERE = new Sphere<Occupant>(SPHERE_FREQUENCY);
    private int hexagonShape, pentagonShape;
    private PointOfView pov = new PointOfView(SPHERE_RADIUS + ALTITUDE, 0);
    private JoglSphereVertexPainter painter = new JoglSphereVertexPainter(pov.getEye());
    private Queue<Runnable> jobs = new ConcurrentLinkedQueue<Runnable>();
    private Orbiter orbiter = new Orbiter(jobs, pov, SPHERE_RADIUS + ALTITUDE);
    private GLCanvas glCanvas = new GLCanvas();
    private Checkbox[] countBoxes = new Checkbox[7];
    private boolean refresh = false;
    private boolean running = true;
    private Vertex.Visitor<Occupant> preparer = new Vertex.Visitor<Occupant>() {
        public void visit(Vertex<Occupant> vertex) {
            if (refresh) vertex.getOccupant().shakeUp();
            vertex.getOccupant().prepare();
        }
    };
    private Vertex.Visitor<Occupant> liver = new Vertex.Visitor<Occupant>() {
        public void visit(Vertex<Occupant> vertex) {
            vertex.getOccupant().live(vertex.getNearby());
        }
    };

    public ConwayLifeSphere() {
        super("Sphere Agents");
        GLViewPlatform viewPlatform = new GLViewPlatform(new Renderer(), pov, ALTITUDE, 5);
        glCanvas.addGLEventListener(viewPlatform);
        glCanvas.setFocusable(true);
        glCanvas.addMouseListener(orbiter.getMouseListener());
        glCanvas.addMouseMotionListener(orbiter.getMouseMotionListener());
        glCanvas.addKeyListener(orbiter.getKeyListener());
        glCanvas.addMouseWheelListener(orbiter.getMouseWheelListener());
        SPHERE.admitVisitor(new Vertex.Visitor<Occupant>() {
            public void visit(Vertex<Occupant> vertex) {
                vertex.setOccupant(new Occupant());
            }
        });
        Panel p = new Panel(new GridLayout(1, 0));
        for (int walk = 0; walk < countBoxes.length; walk++) {
            countBoxes[walk] = new Checkbox();
            p.add(countBoxes[walk]);
            if (walk == 1) {
                countBoxes[walk].setState(true);
            }
        }
        Button button = new Button("restart");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh = true;
            }
        });
        p.add(button);
        add(glCanvas, BorderLayout.CENTER);
        add(p, BorderLayout.SOUTH);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                Thread.currentThread().interrupt();
                running = false;
            }
        });
    }

    private class Renderer implements GLRenderer {

        public void init(GL2 gl) {
            gl.glShadeModel(GL_SMOOTH);
            hexagonShape = createVertexGLShape(gl, false);
            pentagonShape = createVertexGLShape(gl, true);
        }

        public void display(GL2 gl, int width, int height) {
            gl.glColor3f(1f, 1f, 1f);
            painter.prepareForVisit(gl);
            SPHERE.admitVisitor(painter);
        }
    }

    public void step() {
        SPHERE.admitVisitor(preparer);
        SPHERE.admitVisitor(liver);
//		pov.goLeft(0.006f);
        refresh = false;
    }

    public void refresh() {
        glCanvas.display();
    }

    private void go() {
        setVisible(true);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (running) {
                    while (!jobs.isEmpty()) {
                        jobs.remove().run();
                    }
                    step();
                    refresh();
                    orbiter.run();
                    try {
                        Thread.sleep(20);
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void init(GL2 gl) {
        gl.glShadeModel(GL_SMOOTH);
        hexagonShape = createVertexGLShape(gl, false);
        pentagonShape = createVertexGLShape(gl, true);
    }

    public void render(GL2 gl) {
        gl.glColor3f(1f, 1f, 1f);
        painter.prepareForVisit(gl);
        SPHERE.admitVisitor(painter);
    }

    public class JoglSphereVertexPainter implements Vertex.Visitor<Occupant> {
        private final Tint CURRENT_OR_PREVIOUS = new Tint().set(Tint.WHITE, Tint.RED, 0.75f);
//        private final Tint CURRENT = new Tint().set(Tint.WHITE, Tint.GREEN, 0.75f);
        private final Tint NONE = new Tint().set(Tint.WHITE, Tint.BLUE, 0.75f);
        private Arrow xAxis = new Arrow();
        private Arrow yAxis = new Arrow();
        private Arrow zAxis = new Arrow();
        private Arrow eye;
        private Arrow eyeVector = new Arrow();
        private Arrow vertexVector = new Arrow();
        private double[] matrix = new double[16];
        private double vertexScaleFactor;
        private GL2 gl;

        public JoglSphereVertexPainter(Arrow eye) {
            this.eye = eye;
        }

        public void prepareForVisit(GL2 gl) {
            this.gl = gl;
            this.vertexScaleFactor = 8.0 / SPHERE.getFrequency();
            eyeVector.set(eye);
            eyeVector.normalize();
        }

        public void visit(Vertex<Occupant> vertex) {
            vertexVector.set(vertex.getLocation());
            vertexVector.normalize();
            double dot = vertexVector.dot(eyeVector);
            if (dot < VISIBLE_VERTEX_DOT_PRODUCT) return;
            Tint tint = null;
            if (vertex.getOccupant().getCurrent()) {
                if (vertex.getOccupant().getPrevious()) {
                    tint = CURRENT_OR_PREVIOUS;
                }
                else {
                    tint = NONE;
                }
            }
            else {
                if (vertex.getOccupant().getPrevious()) {
                    tint = CURRENT_OR_PREVIOUS;
                }
                else {
                    tint = NONE;
                }
            }
            gl.glMaterialfv(GL_FRONT, GL_AMBIENT, tint.getFloatArray(), 0);
            gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, tint.getFloatArray(), 0);
            gl.glMaterialfv(GL_FRONT, GL_SPECULAR, Tint.BLACK.getFloatArray(), 0);
            displayVertex(gl, vertex);
        }

        private void changeBasis() {
            matrix[0] = xAxis.x * vertexScaleFactor;
            matrix[1] = xAxis.y * vertexScaleFactor;
            matrix[2] = xAxis.z * vertexScaleFactor;
            matrix[3] = 0f;
            matrix[4] = yAxis.x * vertexScaleFactor;
            matrix[5] = yAxis.y * vertexScaleFactor;
            matrix[6] = yAxis.z * vertexScaleFactor;
            matrix[7] = 0f;
            matrix[8] = zAxis.x;
            matrix[9] = zAxis.y;
            matrix[10] = zAxis.z;
            matrix[11] = 0f;
            matrix[12] = zAxis.x * SPHERE_RADIUS; // translate out to point
            matrix[13] = zAxis.y * SPHERE_RADIUS;
            matrix[14] = zAxis.z * SPHERE_RADIUS;
            matrix[15] = 1f;
        }

        private void displayVertex(GL2 gl, Vertex<Occupant> vertex) {
            gl.glPushMatrix();
            zAxis.set(vertex.getLocation());
            zAxis.normalize();
            yAxis.sub(vertex.getNearby().get(0).getLocation(), vertex.getLocation());
            yAxis.normalize();
            xAxis.cross(yAxis, zAxis);
            yAxis.cross(zAxis, xAxis);
            changeBasis();
            gl.glMultMatrixd(matrix, 0);
            gl.glScaled(vertex.getMagnification(), vertex.getMagnification(), vertex.getMagnification());
            gl.glCallList(vertex.isCorner() ? pentagonShape : hexagonShape);
            gl.glPopMatrix();
        }

    }

    private class Occupant {
        private boolean current = Math.random() > 0.7;
        private boolean previous;

        public void shakeUp() {
            current = Math.random() > 0.7;
        }

        public void prepare() {
            previous = current;
        }

        public void live(List<Vertex<Occupant>> nearby) {
            int sum = 0;
            for (Vertex<Occupant> near : nearby) {
                Occupant other = near.getOccupant();
                if (other.previous) {
                    sum++;
                }
            }
            current = countBoxes[sum].getState();
        }

        public boolean getCurrent() {
            return current;
        }

        public boolean getPrevious() {
            return previous;
        }
    }

    public static int createVertexGLShape(GL2 gl, boolean pentagon) {
        int list = gl.glGenLists(1);
        gl.glNewList(list, GL_COMPILE);
//        gl.glMaterialfv(GL_FRONT, GL_AMBIENT, Tint.WHITE.getFloatArray(), 0);
//        gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, Tint.WHITE.getFloatArray(), 0);
//        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, Tint.BLACK.getFloatArray(), 0);
        gl.glMaterialf(GL_FRONT, GL_SHININESS, 0f);
        gl.glBegin(GL_TRIANGLES);
        gl.glNormal3f(0, 0, 1);
        int stepDegrees = pentagon ? 72 : 60;
        int weirdRotation = pentagon ? -18 : 0;
        for (int walk = 0; walk < 360; walk += stepDegrees) {
            double angle1 = (float) (walk + weirdRotation) / RADIANS_TO_DEGREES;
            double x1 = VERTEX_RADIUS * (float) Math.cos(angle1);
            double y1 = VERTEX_RADIUS * (float) Math.sin(angle1);
            double angle2 = (float) (walk + stepDegrees + weirdRotation) / RADIANS_TO_DEGREES;
            double x2 = VERTEX_RADIUS * (float) Math.cos(angle2);
            double y2 = VERTEX_RADIUS * (float) Math.sin(angle2);
            gl.glVertex3d(x1, y1, 0);
            gl.glVertex3d(x2, y2, 0);
            gl.glVertex3d(0, 0, 0);
        }
        gl.glEnd();
        gl.glEndList();
        return list;
    }

    public static void main(String[] args) throws Exception {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                ConwayLifeSphere life = new ConwayLifeSphere();
                life.go();
            }
        });
    }
}
