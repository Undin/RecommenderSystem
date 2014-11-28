package com.ifmo.recommendersystem;

import java.io.File;

/**
 * Created by warrior on 19.11.14.
 */
public class Utils {

    public static String createPath(String... str) {
        StringBuilder builder = new StringBuilder(str[0]);
        for (int i = 1; i < str.length; i++) {
            builder.append(File.separatorChar).append(str[i]);
        }
        return builder.toString();
    }

    public static String createName(String... str) {
        StringBuilder builder = new StringBuilder(str[0]);
        for (int i = 1; i < str.length; i++) {
            builder.append('_').append(str[i]);
        }
        return builder.toString();
    }
}
