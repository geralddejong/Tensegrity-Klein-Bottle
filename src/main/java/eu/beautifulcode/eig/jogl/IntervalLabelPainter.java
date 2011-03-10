package eu.beautifulcode.eig.jogl;

import javax.media.opengl.GL;
import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Interval;

import java.text.DecimalFormat;

/**
 * Show features of an interval using texture font
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class IntervalLabelPainter {
    private static final DecimalFormat FORMAT = new DecimalFormat(" 000 ");
    private PointOfView pointOfView;
    private TextureFont textureFont = new TextureFont();
    private GL gl;
    private Arrow location = new Arrow();
    private Arrow forward = new Arrow();
    private Arrow measure = new Arrow();
    private Feature feature = Feature.NOTHING;

    public IntervalLabelPainter(PointOfView pointOfView) {
        this.pointOfView = pointOfView;
        textureFont.setAnchor(0, -1);
        textureFont.setScale(0.7f);
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public void preVisit(GL gl) {
        this.gl = gl;
        textureFont.ensureInitialized(gl);
    }

    public void visit(Interval interval) {
        interval.getLocation(location);
        measure.sub(pointOfView.getEye(), location);
        double distance = measure.normalize();
        if (distance < 3) {
            measure.scale(0.05);
            location.add(measure);
            showLabel(feature.tagger.getLabel(interval));
        }
    }

    private void showLabel(String label) {
        textureFont.setLocation(location);
        forward.sub(location, pointOfView.getEye());
        forward.normalize();
        textureFont.setOrientation(forward, pointOfView.getUp());
        textureFont.display(gl, label, java.awt.Color.WHITE);
    }

    private interface Tagger {
        String getLabel(Interval interval);
    }

    public enum Feature implements Tagger {

        NOTHING(null),

        ACTUAL(new Tagger() {
            public String getLabel(Interval interval) {
                return FORMAT.format(interval.getSpan().getActual() * 1000);
            }
        }),

        IDEAL(new Tagger() {
            public String getLabel(Interval interval) {
                return FORMAT.format(interval.getSpan().getCurrentIdeal() * 1000);
            }
        }),

        STRESS(new Tagger() {
            public String getLabel(Interval interval) {
                return FORMAT.format(interval.getSpan().getStress() * 100000.0);
            }
        }),

        ROLE(new Tagger() {
            public String getLabel(Interval interval) {
                if (interval.getRole() != null) {
                    return interval.getRole().toString();
                }
                else {
                    return "?";
                }
            }
        });

        private Tagger tagger;

        Feature(Tagger tagger) {
            this.tagger = tagger;
        }

        public String getLabel(Interval interval) {
            return tagger.getLabel(interval);
        }

    }

}