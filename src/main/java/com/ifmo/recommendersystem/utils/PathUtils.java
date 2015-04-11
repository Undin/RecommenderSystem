package com.ifmo.recommendersystem.utils;

import java.io.File;

/**
 * Created by warrior on 19.11.14.
 */
public class PathUtils {

    public static String createPath(String... str) {
        return String.join(File.separator, str);
    }

    public static String createName(String... str) {
        return String.join("_", str);
    }
}
