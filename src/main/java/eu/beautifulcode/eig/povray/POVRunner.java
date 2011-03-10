/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.povray;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class POVRunner {
    private static final String POVRAY_PROGRAM = "/opt/local/bin/povray";
    private static final String IMAGEMAGICK_PROGRAM = "/opt/local/bin/convert";

    public static String snapshot2048x1536(File script, File image) throws IOException {
        return snapshot(script, 2048, 1536, image);
    }

    public static String snapshot1024x768(File script, File image) throws IOException {
        return snapshot(script, 1024, 768, image);
    }

    public static String snapshot640x480(File script, File image) throws IOException {
        return snapshot(script, 640, 480, image);
    }

    public static String jitterStereoGif(File script, File image) throws IOException {
        return snapshot(script, 640, 480, image);
    }

    public static String snapshot(File script, int width, int height, File image) throws IOException {
        return executePovRay(image.getParentFile(), "+A0.3", "+W" + width, "+H" + height, "+I" + script.getAbsolutePath(), "+O" + image.getName());
    }

    private static String executePovRay(File directory, String... arguments) throws IOException {
        String[] command = new String[arguments.length + 1];
        int index = 0;
        command[index++] = POVRAY_PROGRAM;
        for (String argument : arguments) {
            command[index++] = argument;
        }
        StringBuilder commandString = new StringBuilder();
        for (String part : command) {
            commandString.append(" ").append(part);
        }
        String[] vars = {"PATH=" + POVRAY_PROGRAM};
        Process process = Runtime.getRuntime().exec(command, vars, directory);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("input:" + line);
        }
        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null) {
            System.out.println("error:" + line);
        }
        try {
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new IOException("Unable to run Povray!");
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return commandString.toString();
    }

    public static String createAnimatedGif(File left, File right, File animated) throws IOException {
        return executeImageMagick(animated.getParentFile(), "-delay 20", "-loop 1", left.getAbsolutePath(), right.getAbsolutePath(), animated.getAbsolutePath());
    }

    private static String executeImageMagick(File directory, String... arguments) throws IOException {
        String[] command = new String[arguments.length + 1];
        int index = 0;
        command[index++] = IMAGEMAGICK_PROGRAM;
        for (String argument : arguments) {
            command[index++] = argument;
        }
        StringBuilder commandString = new StringBuilder();
        for (String part : command) {
            commandString.append(" ").append(part);
        }
        String[] vars = {"PATH=" + POVRAY_PROGRAM};
        Process process = Runtime.getRuntime().exec(command, vars, directory);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("input:" + line);
        }
        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null) {
            System.out.println("error:" + line);
        }
        try {
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new IOException("Unable to run Povray!");
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return commandString.toString();
    }

    public static void main(String[] args) throws IOException {
        File script = new File("/tmp/snapshot.pov");
        File image = new File("/tmp/snapshot.gif");
//        snapshot2048x1536(script, image);
        jitterStereoGif(script, image);
    }
}