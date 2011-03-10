/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import com.sun.opengl.util.Screenshot;
import com.sun.opengl.util.j2d.TextRenderer;
import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.math.Space4;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;

/**
 * This class plays the role of listening for events from the
 * OpenGL system, at which point we have to initialize or display
 * what is on the screen.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class GLViewPlatform implements GLEventListener, Mouse3d.PickRaySource {
    private static final double EYE_DISPLACEMENT = 0.3;
    private static final Tint LIGHT_MODEL_AMBIENT = new Tint(Tint.BLACK, Tint.WHITE, 1f);
    private Movie movie;
    private TextRenderer textRenderer = new TextRenderer(new Font("Dialog", Font.BOLD, 16));
    private double textHeight;
    private HeadsUpImpl headsUp = new HeadsUpImpl();
    private GLU glu = new GLU();
    private PointOfView pov;
    private double[] elements = new double[16];
    private Space4 modelView = new Space4();
    private double frustumNear, frustumFar;
    private int width, height;
    private double left, right, bottom, top;
    private GLRenderer renderer;
    private Boolean rightEye;
    private Arrow eye = new Arrow();

    public GLViewPlatform(GLRenderer renderer, PointOfView pov, double frustumNear, double frustumFar) {
        this.renderer = renderer;
        this.pov = pov;
        this.frustumNear = frustumNear;
        this.frustumFar = frustumFar;
    }

    public void setRightEye(Boolean rightEye) {
        this.rightEye = rightEye;
    }

    public void recordMovie(File movieDirectory, String movieName, int displaysPerFrame) {
        this.movie = new Movie(movieDirectory, movieName, displaysPerFrame);
    }

    public boolean isRecordingMovie() {
        return movie != null;
    }

    public void stopMovie() {
        this.movie = null;
    }

    public HeadsUp getHeadsUp() {
        return headsUp;
    }

    public final void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, LIGHT_MODEL_AMBIENT.getFloatArray(), 0);
        gl.glLightf(GL.GL_LIGHT0, GL.GL_CONSTANT_ATTENUATION, 0.0f);
        gl.glLightf(GL.GL_LIGHT0, GL.GL_LINEAR_ATTENUATION, 0.1f);
        gl.glLightf(GL.GL_LIGHT0, GL.GL_QUADRATIC_ATTENUATION, 0.6f);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LIGHT0);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glEnable(GL.GL_NORMALIZE);
        renderer.init(gl);
    }

    public final void display(GLAutoDrawable glAutoDrawable) {
        GL gl = glAutoDrawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glPushMatrix();
        setEye();
        Arrow focus = pov.getFocus();
        Arrow up = pov.getUp();
        glu.gluLookAt(eye.x, eye.y, eye.z, focus.x, focus.y, focus.z, up.x, up.y, up.z);
        renderer.display(gl, glAutoDrawable.getWidth(), glAutoDrawable.getHeight());
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslated(0, 0, 1);
        textRenderer.beginRendering(glAutoDrawable.getWidth(), glAutoDrawable.getHeight());
        textHeight = textRenderer.getBounds("Yy").getHeight() * 1.3;
        if (headsUp != null) {
            headsUp.display(glAutoDrawable);
        }
        textRenderer.endRendering();
        gl.glPopMatrix();
        if (movie != null) {
            movie.display();
        }
    }

    private void setEye() {
        if (rightEye == null) {
            eye.set(pov.getEye());
        }
        else if (rightEye) {
            pov.getEye(EYE_DISPLACEMENT, eye);
        }
        else {
            pov.getEye(-EYE_DISPLACEMENT, eye);
        }
    }

    public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        this.width = width;
        this.height = height;
        double factor = (double) height * 4e-4;
        double aspectRatio = (double) width / (double) height;
        left = -aspectRatio * factor;
        right = aspectRatio * factor;
        bottom = -factor;
        top = factor;
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustum(left, right, bottom, top, frustumNear, frustumFar);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public final void displayChanged(GLAutoDrawable drawable, boolean b, boolean b1) {
    }

    public void getPickRay(GL gl, double mouseX, double mouseY, Arrow location, Arrow direction) {
        location.set(pov.getEye());
        double x = left + mouseX / width * (right - left);
        double y = top + mouseY / height * (bottom - top);
        direction.set(x, y, -frustumNear);
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, elements, 0);
        modelView.set(elements);
        modelView.transform(direction);
        direction.normalize();
    }

    private class HeadsUpImpl implements HeadsUp {
        private PositionedLine[][] lines = new PositionedLine[Pos.values().length][];
        private Color color = Color.WHITE;

        private HeadsUpImpl() {
            for (int walk = 0; walk < Pos.values().length; walk++) {
                lines[walk] = new PositionedLine[0];
            }
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void set(Pos pos, Collection<? extends Line> lines) {
            this.lines[pos.ordinal()] = new PositionedLine[lines.size()];
            int index, increment;
            switch (pos) {
                case BOTTOM_LEFT:
                case BOTTOM_RIGHT:
                    index = this.lines[pos.ordinal()].length - 1;
                    increment = -1;
                    break;
                case TOP_LEFT:
                case TOP_RIGHT:
                case MIDDLE:
                    index = 0;
                    increment = 1;
                    break;
                default:
                    return;
            }
            for (Line line : lines) {
                this.lines[pos.ordinal()][index] = new PositionedLine(line);
                index += increment;
            }
        }

        public void set(Pos pos, Line ... lines) {
            this.lines[pos.ordinal()] = new PositionedLine[lines.length];
            int index, increment;
            switch (pos) {
                case BOTTOM_LEFT:
                case BOTTOM_RIGHT:
                    index = this.lines[pos.ordinal()].length - 1;
                    increment = -1;
                    break;
                case TOP_LEFT:
                case TOP_RIGHT:
                case MIDDLE:
                    index = 0;
                    increment = 1;
                    break;
                default:
                    return;
            }
            for (Line line : lines) {
                this.lines[pos.ordinal()][index] = new PositionedLine(line);
                index += increment;
            }
        }

        private void display(GLAutoDrawable drawable) {
            textRenderer.setColor(color);
            for (Pos pos : Pos.values()) {
                for (PositionedLine positionedLine : reposition(pos, drawable)) {
                    positionedLine.display();
                }
            }
        }

        private PositionedLine [] reposition(Pos pos, GLAutoDrawable drawable) {
            PositionedLine[] posLines = lines[pos.ordinal()];
            int walk = 0;
            for (PositionedLine line : posLines) {
                Rectangle2D bounds = line.getBounds();
                switch (pos) {
                    case MIDDLE:
                        line.setPoint(
                                drawable.getWidth() / 2.0 - bounds.getWidth() / 2.0,
                                drawable.getHeight() / 2.0 + textHeight * (posLines.length / 2 - walk)
                        );
                        break;
                    case TOP_LEFT:
                        line.setPoint(
                                MARGIN,
                                drawable.getHeight() - MARGIN - textHeight * walk
                        );
                        break;
                    case TOP_RIGHT:
                        line.setPoint(
                                drawable.getWidth() - MARGIN - bounds.getWidth(),
                                drawable.getHeight() - MARGIN - textHeight * walk
                        );
                        break;
                    case BOTTOM_LEFT:
                        line.setPoint(
                                MARGIN,
                                MARGIN + textHeight * walk
                        );
                        break;
                    case BOTTOM_RIGHT:
                        line.setPoint(
                                drawable.getWidth() - MARGIN - bounds.getWidth(),
                                MARGIN + textHeight * walk
                        );
                        break;
                }
                walk++;
            }
            return posLines;
        }
    }

    private class PositionedLine {
        private HeadsUp.Line line;
        private Rectangle2D bounds;
        private int x, y;

        private PositionedLine(HeadsUp.Line line) {
            this.line = line;
        }

        public void setPoint(double x, double y) {
            this.x = (int) x;
            this.y = (int) y;
        }

        public Rectangle2D getBounds() {
            if (bounds == null || line.hasChanged()) {
                bounds = textRenderer.getBounds(line.getText());
            }
            return bounds;
        }

        public void display() {
            textRenderer.draw(line.getText(), x, y);
        }
    }


    private class Movie {
        private final DecimalFormat FRAME_NUMBER_FORMATTER = new DecimalFormat("00000");
        private File movieDirectory;
        private String movieName;
        private int displaysPerFrame, snapshotCountdown, frameNumber;

        private Movie(File movieDirectory, String movieName, int displaysPerFrame) {
            this.movieDirectory = movieDirectory;
            this.movieName = movieName;
            this.displaysPerFrame = displaysPerFrame;
            this.snapshotCountdown = displaysPerFrame;
        }

        private void display() {
            if (snapshotCountdown-- == 0) {
                try {
                    String frameFileName = movieName + FRAME_NUMBER_FORMATTER.format(frameNumber++) + ".tga";
                    Screenshot.writeToTargaFile(new File(movieDirectory, frameFileName), width, height);
                    snapshotCountdown = displaysPerFrame;
                }
                catch (IOException e) {
                    e.printStackTrace();
                    movieDirectory = null;
                }
            }
        }
    }
}