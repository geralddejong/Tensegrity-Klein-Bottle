/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.povray;

import eu.beautifulcode.eig.jogl.PointOfView;
import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Fabric;
import eu.beautifulcode.eig.structure.Interval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class POVScriptGenerator {
    private static final double EYE_DISPLACEMENT = 0.02;
    private POVFiles files;
    private PointOfView pointOfView;
    private ImageRecorder image;
    private MovieRecorder movie;

    public POVScriptGenerator(File root, PointOfView pointOfView) {
        this.files = new POVFiles(root);
        this.pointOfView = pointOfView;
    }

    public void recordImage() {
        this.image = new ImageRecorder();
    }

    public void startMovie() {
        this.movie = new MovieRecorder();
    }

    public void startFrame() {
        if (this.movie != null) {
            this.movie.startFrame();
        }
    }

    public void endFrame() {
        if (this.movie != null) {
            this.movie.endFrame();
        }
        if (this.image != null) {
            this.image.finish();
            this.image = null;
        }
    }

    public void endMovie() {
        if (this.movie != null) {
            this.movie.finish();
        }
    }

    public void visit(Fabric fabric) {
        if (this.image != null) {
            this.image.visit(fabric);
        }
        if (this.movie != null) {
            this.movie.visit(fabric);
        }
    }

    private class ImageRecorder {
        private Map<POVFiles.Script, File> fileMap;
        private PrintWriter body;

        private ImageRecorder() {
            this.fileMap = files.nextImage();
            createHeader(POVFiles.Script.HEADER, 0);
            createHeader(POVFiles.Script.HEADER_LEFT, pointOfView.getDistance() * EYE_DISPLACEMENT);
            createHeader(POVFiles.Script.HEADER_RIGHT, -pointOfView.getDistance() * EYE_DISPLACEMENT);
            this.body = createWriter(POVFiles.Script.BODY);
        }

        private void createHeader(POVFiles.Script script, double eyeDisplacement) {
            writeHeader(eyeDisplacement, createWriter(script));
        }

        public void visit(Fabric fabric) {
            writeFabric(fabric, body);
        }

        public void finish() {
            body.close();
            PrintWriter out = createWriter(POVFiles.Script.JITTER);
            out.println("# this script should render the stereo images and then make a jitter gif");
            out.println("# default size is 640x480 but you can give width and height as parameters");
            writeWidthHeight(out, 640, 480);
            out.println("povray -D +A0.3 +W$W +H$H +HIheader-l.pov +Ibody.pov +Oimage-l.png");
            out.println("povray -D +A0.3 +W$W +H$H +HIheader-r.pov +Ibody.pov +Oimage-r.png");
            out.println("convert -delay 20 -loop 0 image-l.png image-r.png jitter.gif");
            out.close();
            out = createWriter(POVFiles.Script.LARGE);
            out.println("# this script should render a large image");
            out.println("# default size is 1600x1200 but you can give width and height as parameters");
            writeWidthHeight(out, 1600, 1200);
            out.println("povray -D +A0.3 +W$W +H$H +HIheader.pov +Ibody.pov +Olarge.png");
            out.close();
        }

        private PrintWriter createWriter(POVFiles.Script script) {
            return createPrintWriter(fileMap.get(script));
        }
    }

    private class MovieRecorder {
        private Map<POVFiles.Script, File> fileMap;
        private int frameCount = -1;
        private boolean finished;
        private PrintWriter body;

        private MovieRecorder() {
            this.fileMap = files.nextMovie();
            createHeader(POVFiles.Script.HEADER, 0);
            createHeader(POVFiles.Script.HEADER_LEFT, EYE_DISPLACEMENT);
            createHeader(POVFiles.Script.HEADER_RIGHT, -EYE_DISPLACEMENT);
            this.body = createWriter(POVFiles.Script.BODY);
        }

        public void startFrame() {
            frameCount++;
            body.println("#if(frame_number=" + frameCount + ")");
        }

        public void visit(Fabric fabric) {
            writeFabric(fabric, body);
        }

        public void endFrame() {
            body.println("#end");
            if (finished) {
                body.close();
                createScript();
            }
        }

        private void createScript() {
            PrintWriter movie = createWriter(POVFiles.Script.MOVIE);
            movie.println("# this script should render the movie");
            writeWidthHeight(movie, 640, 480);
            movie.println("povray -D +A0.3 +W$W +H$H +KFI0 +KFF" + frameCount + " -HIheader.pov -Ibody.pov");
            movie.println("for f in *png ; do echo $f; convert -quality 100 $f `basename $f png`jpg; done ");
            movie.println("ffmpeg -r 10 -b 1800 -i body%03d.jpg movie.mp4");
            movie.close();
        }

        public void finish() {
            finished = true;
        }

        private void createHeader(POVFiles.Script script, double eyeDisplacement) {
            writeHeader(eyeDisplacement, createWriter(script));
        }

        private PrintWriter createWriter(POVFiles.Script script) {
            return createPrintWriter(fileMap.get(script));
        }
    }

    // the rest is private

    private PrintWriter createPrintWriter(File file) {
        try {
            return new PrintWriter(file);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeHeader(double eyeDisplacement, PrintWriter out) {
        writeTitles(out);
        writeIncludes(out);
        writeDeclarations(out);
        createFog(out);
        createLight(pointOfView, out);
        writeFloor(out);
        createCamera(pointOfView, eyeDisplacement, out);
        out.close();
    }

    private static void writeWidthHeight(PrintWriter writer, int width, int height) {
        writer.println("W=$1");
        writer.println("H=$2");
        writer.println("if test -z $W");
        writer.println("then");
        writer.println("        W=\""+width+"\"");
        writer.println("fi");
        writer.println("if test -z $H");
        writer.println("then");
        writer.println("        H=\""+height+"\"");
        writer.println("fi");
    }

    private static void writeTitles(PrintWriter writer) {
        writer.println("// POV-Ray Script created by Darwin@Home Project (http://www.darwinathome.org)");
        writer.println("// Copyright (C) 2009 Beautiful Code BV (http://www.beautifulcode.eu)");
        writer.println();
    }

    private static void writeIncludes(PrintWriter writer) {
        writeInclude("colors.inc", writer);
        writeInclude("shapes.inc", writer);
        writeInclude("textures.inc", writer);
        writeInclude("metals.inc", writer);
        writeInclude("stones.inc", writer);
        writer.println();
    }

    private static void writeInclude(String fileName, PrintWriter writer) {
        writer.println("#include \"" + fileName + "\"");
    }

    private static void writeFloor(PrintWriter writer) {
        writer.println("object {");
        writer.println("  plane {");
        writer.println("     < 0, 0, 1 >, 0");
        writer.println("     texture { T_Stone24 }");
        writer.println("     rotate z*60");
        writer.println("     scale 0.1");
        writer.println("  }");
        writer.println("}");
        writer.println();
    }

    private static void writeDeclarations(PrintWriter out) {
        writeDeclaration("BarTexture", "T_Chrome_5C", out);
        writeDeclaration("BarRadius", "0.04", out);
        writeDeclaration("CableTexture", "T_Copper_1A", out);
        writeDeclaration("CableRadius", "0.004", out);
        writeDeclaration("TriangleTexture", "pigment{ color Gray25 }", out);
        out.println();
    }

    private static void writeDeclaration(String name, String value, PrintWriter writer) {
        writer.println("#declare " + name + " = " + value + ";");
    }

    private static void createFog(PrintWriter writer) {
        writer.println("fog {");
        writer.println("  distance 100");
        writer.println("  red 0.35 green 0.4 blue 0.65");
        writer.println("  turbulence 0.2");
        writer.println("  turb_depth 0.3");
        writer.println("}");
        writer.println();
    }

    private static void createCamera(PointOfView pov, double eyeDisplacement, PrintWriter writer) {
        Arrow eye = new Arrow();
        pov.getEye(eyeDisplacement, eye);
        writer.println("camera {");
        writer.println("  location  <" + format(eye) + ">");
        writer.println("  sky <" + format(pov.getUp()) + ">");
        writer.println("  right 640/480*x");
        writer.println("  up y");
        writer.println("  look_at <" + format(pov.getFocus()) + ">");
        writer.println("  angle 50");
        writer.println("}");
        writer.println();
    }

    private static void createLight(PointOfView pov, PrintWriter writer) {
        Arrow location = new Arrow();
        location.set(pov.getFocus());
        location.z += 100;
        writer.println("object {");
        writer.println("  light_source {");
        writer.println("    <" + format(location) + ">");
        writer.println("    color White");
        writer.println("  }");
        writer.println("}");
        writer.println();
    }

    private static final double TO_DEGREES = 180.0 / Math.PI;

    private static void writeFabric(Fabric fabric, PrintWriter out) {
        Arrow location = new Arrow();
        Arrow axis = new Arrow();
        for (Interval interval : fabric.getIntervals()) {
            switch (interval.getRole()) {
                case BAR:
                case SCAFFOLD:
                case SPRING:
                case MUSCLE:
                    interval.getLocation(location);
                    Arrow unit = interval.getUnit(false);
                    double actualSpan = interval.getSpan().getActual();
                    axis.set(-unit.y, unit.x, 0f);
                    double yTwist = -Math.asin(unit.z);
                    double zTwist = Math.atan2(unit.y, unit.x);
                    out.println("object {");
                    out.println("  sphere{ <0,0,0> 1.0");
                    out.println("    scale <sqrt(" + format(actualSpan * actualSpan / 4.0) + "+BarRadius*BarRadius),BarRadius,BarRadius>");
                    out.println("    rotate <0," + format(yTwist * TO_DEGREES) + "," + format(zTwist * TO_DEGREES) + ">");
                    out.println("    translate <" + format(location) + ">");
                    out.println("    texture { BarTexture }");
                    out.println("  }");
                    out.println("}");
                    out.println();
                    break;
                case CABLE:
                case HORIZ:
                case RINGBAR:
                case RING:
                case ZIG:
                case ZAG:
                    out.println("cylinder {");
                    out.println("  <" + format(interval.get(false).getLocation()) + ">,");
                    out.println("  <" + format(interval.get(true).getLocation()) + ">,");
                    out.println("  CableRadius");
                    out.println("  texture { CableTexture }");
                    out.println("}");
                    out.println();
                    break;
                case TEMP:
                    break;
            }
        }
//        if (!fabric.getFaces().isEmpty()) { // revive this when faces can be more than triangles
//            out.println("mesh {\n");
//            for (Face face : fabric.getFaces()) {
//                out.println(
//                        "  triangle {"
//                );
//                int count = 0;
//                for (Joint joint : face.getJoints()) {
//                    out.println("    <" + format(joint.getLocation()) + ">");
//                    if (count++ < 2) {
//                        out.println(",");
//                    }
//                    else {
//                        out.println();
//                    }
//                }
//                out.println(
//                        "  }\n"
//                );
//            }
//            out.println(
//                    "  texture { TriangleTexture }\n" +
//                            "}\n"
//            );
//        }
    }

    private static String format(Arrow arrow) {
        return format(arrow.x) + "," + format(arrow.y) + "," + format(arrow.z);
    }

    private static final DecimalFormat DECIMAL = new DecimalFormat("#0.0#####");

    private static String format(double value) {
        return DECIMAL.format(value);
    }
}
