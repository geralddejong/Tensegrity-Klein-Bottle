package eu.beautifulcode.eig.jogl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import eu.beautifulcode.eig.math.Arrow;
import eu.beautifulcode.eig.structure.Joint;

/**
 * Show who is who in joints
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class JointWhoPainter {
    private PointOfView pointOfView;
    private TextureFont textureFont = new TextureFont();
    private GL2 gl;
    private Arrow forward = new Arrow();

    public JointWhoPainter(PointOfView pointOfView) {
        this.pointOfView = pointOfView;
        textureFont.setAnchor(0,-1);
        textureFont.setScale(2f);
    }

    public void preVisit(GL2 gl) {
        this.gl = gl;
        textureFont.ensureInitialized(gl);
    }

    public void visit(Joint joint) {
        textureFont.setLocation(joint.getLocation());
        forward.sub(joint.getLocation(), pointOfView.getEye());
        forward.normalize();
        textureFont.setOrientation(forward, pointOfView.getUp());
        textureFont.display(gl, joint.getWho().toString(), java.awt.Color.WHITE);
    }
}