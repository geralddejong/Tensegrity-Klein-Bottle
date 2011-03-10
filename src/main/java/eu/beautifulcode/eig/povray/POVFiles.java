/*
 * Copyright (C)2008 Gerald de Jong - GNU General Public License
 * please see the LICENSE.TXT in this distribution for more details.
 */
package eu.beautifulcode.eig.povray;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class POVFiles {
    private static final String IMAGE_PREFIX = "image-";
    private static final String MOVIE_PREFIX = "movie-";
    private static final NumberFormat DIRECTORY_NUMBER_FORMAT = new DecimalFormat("0000");
    private File root;
    private int imageCount = -1;
    private int movieCount = -1;

    public enum Script {
        HEADER("header.pov"),
        HEADER_LEFT("header-l.pov"),
        HEADER_RIGHT("header-r.pov"),
        BODY("body.pov"),
        MOVIE("movie.sh"),
        JITTER("jitter.sh"),
        LARGE("large.sh");

        private String fileName;

        Script(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public POVFiles(File root) {
        this.root = root;
        this.imageCount = highestNumberWith(IMAGE_PREFIX);
        this.movieCount = highestNumberWith(MOVIE_PREFIX);
    }

    public Map<Script, File> nextImage() {
        return createFiles(++imageCount, IMAGE_PREFIX);
    }

    public Map<Script, File> nextMovie() {
        return createFiles(++movieCount, MOVIE_PREFIX);
    }

    private Map<Script, File> createFiles(int count, String prefix) {
        File directory = new File(root, prefix+DIRECTORY_NUMBER_FORMAT.format(count));
        if (!directory.mkdirs()) {
            throw new RuntimeException();
        }
        Map<Script, File> map = new HashMap<Script, File>(13);
        for (Script script : Script.values()) {
            File file = new File(directory, script.getFileName());
            map.put(script, file);
        }
        return map;
    }

    private int highestNumberWith(String prefix) {
        int highest = 0;
        File [] files = root.listFiles(new DirectoryFilter(prefix));
        if (files != null) {
            for (File file : files) {
                String numberString = file.getName().substring(prefix.length());
                int number = Integer.parseInt(numberString);
                if (number > highest) {
                    highest = number;
                }
            }
        }
        return highest;
    }

    private static class DirectoryFilter implements FileFilter {
        private String prefix;

        private DirectoryFilter(String prefix) {
            this.prefix = prefix;
        }

        public boolean accept(File file) {
            return file.isDirectory() && file.getName().startsWith(prefix);
        }
    }
}