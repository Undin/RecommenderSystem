package com.ifmo.recommendersystem.utils;

import weka.core.Utils;

import java.util.Objects;

public class StatisticalUtils {

    private StatisticalUtils() {
    }

    public static double rawMoment(double[] values, int k) {
        double acc = 0;
        double count = 0;
        for (double value : values) {
            if (isCorrectValue(value)) {
                acc += StrictMath.pow(value, k);
                count++;
            }
        }
        return acc / count;
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
            if (isCorrectValue(values[i])) {
                tmp[i] = values[i] - mean;
            }
        }
        return tmp;
    }

    public static double covariance(double values1[], double values2[], double mean1, double mean2) {
        checkSameLength(values1, values2);
        int length = values1.length;
        double cov = 0;
        double count = 0;
        for (int i = 0; i < length; i++) {
            if (isCorrectValue(values1[i]) && isCorrectValue(values2[i])) {
                cov += (values1[i] - mean1) * (values2[i] - mean2);
                count++;
            }
        }
        cov /= count;
        return cov;
    }

    public static double covariance(double values1[], double values2[]) {
        checkSameLength(values1, values2);
        return covariance(values1, values2, mean(values1), mean(values2));
    }

    public static double linearCorrelationCoefficient(double covariance, double variance1, double variance2) {
        return covariance / Math.sqrt(variance1 * variance2);
    }

    public static double linearCorrelationCoefficient(double values1[], double values2[], double mean1, double mean2, double variance1, double variance2) {
        checkSameLength(values1, values2);
        double covariance = covariance(values1, values2, mean1, mean2);
        return linearCorrelationCoefficient(covariance, variance1, variance2);
    }

    public static double linearCorrelationCoefficient(double values1[], double values2[]) {
        checkSameLength(values1, values2);
        double mean1 = mean(values1);
        double mean2 = mean(values2);
        double variance1 = variance(values1, mean1);
        double variance2 = variance(values2, mean2);
        return linearCorrelationCoefficient(values1, values2, mean1, mean2, variance1, variance2);
    }

    public static boolean isCorrectValue(double v) {
        return !Utils.isMissingValue(v);
    }

    private static void checkSameLength(double[] values1, double[] values2) {
        values1 = Objects.requireNonNull(values1);
        values2 = Objects.requireNonNull(values2);
        if (values1.length != values2.length) {
            throw new IllegalArgumentException("value arrays must have same length");
        }
    }
}
