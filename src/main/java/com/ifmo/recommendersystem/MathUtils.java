package com.ifmo.recommendersystem;

public class MathUtils {

    public static double rawMoment(double[] values, int k) {
        double acc = 0;
        for (double value : values) {
            acc += StrictMath.pow(value, k);
        }
        return acc / values.length;
    }

    public static double centralMoment(double[] values, int k, double mean) {
        return rawMoment(unshifted(values, mean), k);
    }

    public static double centralMoment(double[] values, int k) {
        return centralMoment(values, k, mean(values));
    }

    public static double mean(double[] values) {
        return rawMoment(values, 1);
    }

    public static double variance(double[] values) {
        return centralMoment(values, 2);
    }

    public static double variance(double[] values, double mean) {
        return centralMoment(values, 2, mean);
    }

    private static double[] unshifted(double[] values, double mean) {
        double[] tmp = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            tmp[i] = values[i] - mean;
        }
        return tmp;
    }
}
