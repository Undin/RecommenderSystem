package com.ifmo.recommendersystem;

import java.io.File;

/**
 * Created by warrior on 19.11.14.
 */
public class Utils {

    public static String createPath(String... str) {
        return String.join(File.separator, str);
    }

    public static String createName(String... str) {
        return String.join("_", str);
    }
}
