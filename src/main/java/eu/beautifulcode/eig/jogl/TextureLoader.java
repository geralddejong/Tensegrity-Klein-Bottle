/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.jogl;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Load a texture into graphics
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TextureLoader {

    public static int[] getTextures(GL gl, String... resource) throws IOException {
        int[] texture = new int[resource.length];
        gl.glGenTextures(resource.length, texture, 0);
        for (int walk=0; walk<resource.length; walk++) {
            URL url = TextureLoader.class.getResource(resource[walk]);
            BufferedImage bufferedIImage = ImageIO.read(url);
            AffineTransform affineTransform = AffineTransform.getScaleInstance(1, -1);
            affineTransform.translate(0, -bufferedIImage.getHeight(null));
            AffineTransformOp transformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            bufferedIImage = transformOp.filter(bufferedIImage, null);
            int width = bufferedIImage.getWidth();
            int height = bufferedIImage.getHeight();
            ByteBuffer byteBuffer;
            switch (bufferedIImage.getType()) {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_CUSTOM:
                    byte[] byteArray = ((DataBufferByte) bufferedIImage.getRaster().getDataBuffer()).getData();
                    byteBuffer = ByteBuffer.allocate(byteArray.length);
                    byteBuffer.put(byteArray);
                    byteBuffer.flip();
                    break;
                case BufferedImage.TYPE_INT_RGB:
                    // this isn't working correctly, but let's leave it here..
                    int[] intArray = ((DataBufferInt) bufferedIImage.getRaster().getDataBuffer()).getData();
                    byteBuffer = ByteBuffer.allocate(intArray.length * 4);
                    byteBuffer.asIntBuffer().put(intArray, 0, intArray.length);
                    break;
                default:
                    throw new RuntimeException("Unsupported image type " + bufferedIImage.getType());
            }
            gl.glBindTexture(GL.GL_TEXTURE_2D, texture[walk]);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexImage2D(GL.GL_TEXTURE_2D,
                    0,
                    3,
                    width,
                    height,
                    0,
                    GL.GL_RGB,
                    GL.GL_UNSIGNED_BYTE,
                    byteBuffer
            );
        }
        return texture;
    }
}