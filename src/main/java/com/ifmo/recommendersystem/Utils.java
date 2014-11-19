package com.ifmo.recommendersystem;

import java.io.File;

/**
 * Created by warrior on 19.11.14.
 */
public class Utils {

    public static double[] calculateEARRCoefs(double alpha, double betta, double[] accuracy, double[] runtime, double[] number) {
        if (alpha < 0 || betta < 0) {
            throw new IllegalArgumentException("alpha must be >= 0 && betta must be >= 0");
        }
        if (accuracy == null || runtime == null || number == null) {
            throw new IllegalArgumentException("arguments must be not null");
        }
        if (accuracy.length != runtime.length || runtime.length != number.length) {
            throw new IllegalArgumentException("arguments must have same length");
        }
        int len = accuracy.length;
        double[] eaarCoefs = new double[len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (i != j) {
                    eaarCoefs[i] += (accuracy[i] / accuracy[j]) /
                            (1 + alpha * Math.log(runtime[i] / runtime[j]) + betta * Math.log(number[i] / number[j]));
                }
            }
            eaarCoefs[i] /= len - 1;
        }
        return eaarCoefs;
    }

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
