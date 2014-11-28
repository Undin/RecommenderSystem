package com.ifmo.recommendersystem;

import com.google.common.base.Joiner;

import java.io.File;

/**
 * Created by warrior on 19.11.14.
 */
public class Utils {

    public static String createPath(String... str) {
        return Joiner.on(File.separatorChar).join(str);
    }

    public static String createName(String... str) {
        return Joiner.on('_').join(str);
    }
}
