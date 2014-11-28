package com.ifmo.recommendersystem;

import org.json.JSONObject;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;

public class MetaFeatures implements JSONConverted {

    public static final int META_FEATURE_NUMBER = 13;
    
    private static final String[] attributeDescription = new String[META_FEATURE_NUMBER];
    static {
        attributeDescription[0] = "Number of instances";
        attributeDescription[1] = "Number of features";
        attributeDescription[2] = "Number of target concept values";
        attributeDescription[3] = "Data set dimensionality";
        attributeDescription[4] = "Mean absolute linear correlation coefficient of all possible pairs of features";
        attributeDescription[5] = "Mean skewness";
        attributeDescription[6] = "Mean kurtosis";
        attributeDescription[7] = "Normalized class entropy";
        attributeDescription[8] = "Mean normalized feature entropy";
        attributeDescription[9] = "Mean mutual information of class and attribute";
        attributeDescription[10] = "Maximum mutual information of class and attribute";
        attributeDescription[11] = "Equivalent number of features,";
        attributeDescription[12] = "Noise-signal ratio";
    }

    private final double[] values;

    public MetaFeatures(double[] values) {
        if (values == null) {
            throw new IllegalArgumentException("values must be not null");
        }
        if (values.length != META_FEATURE_NUMBER) {
            throw new IllegalArgumentException("values.length must be = " + META_FEATURE_NUMBER);
        }
        this.values = values;
    }

    public String getAttributeDescription(int index) {
        return attributeDescription[index];
    }

    public double value(int i) {
        return values[i];
    }

    public int numAttributes() {
        return values.length;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < META_FEATURE_NUMBER; i++) {
            builder.append(attributeDescription[i]).append(" : ").append(values[i]).append("\n");
        }
        return builder.toString();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        for (int i = 0; i < META_FEATURE_NUMBER; i++) {
            object.put(attributeDescription[i], values[i]);
        }
        return object;
    }

    public static MetaFeatures extractMetaFeature(Instances dataSet) {
        if (dataSet.classIndex() < 0) {
            throw new IllegalArgumentException("data set must have class attribute");
        }
        if (dataSet.checkForStringAttributes()) {
            dataSet = new Instances(dataSet);
            dataSet.deleteStringAttributes();
        }

        int classIndex = dataSet.classIndex();
        int attributeNumber = dataSet.numAttributes() - 1;
        double[] values = new double[META_FEATURE_NUMBER];
        values[0] = dataSet.numInstances();
        values[1] = attributeNumber;
        values[2] = dataSet.numClasses();
        values[3] = values[0] / values[1];
        double[][] attributeValues = new double[attributeNumber][];
        int next = 0;
        for (int i = 0; i < dataSet.numAttributes(); i++) {
            if (i != classIndex) {
                attributeValues[next] = dataSet.attributeToDoubleArray(i);
                next++;
            }
        }
        double[] mean = new double[attributeNumber];
        double[] standardDeviation = new double[attributeNumber];
        for (int i = 0; i < attributeNumber; i++) {
            mean[i] = MathUtils.mean(attributeValues[i]);
            standardDeviation[i] = Math.sqrt(MathUtils.variance(attributeValues[i], mean[i]));
        }
        values[4] = meanAttributeCorrelation(attributeValues, mean, standardDeviation);
        values[5] = meanSkewness(attributeValues, mean, standardDeviation);
        values[6] = meanKurtosis(attributeValues, mean, standardDeviation);


        Discretize discretize = new Discretize();
        discretize.setUseBetterEncoding(true);
        try {
            discretize.setInputFormat(dataSet);
            dataSet = Filter.useFilter(dataSet, discretize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        attributeValues = new double[dataSet.numAttributes()][];
        for (int i = 0; i < dataSet.numAttributes(); i++) {
            attributeValues[i] = dataSet.attributeToDoubleArray(i);
        }

        EntropyResult classResult = entropy(attributeValues[dataSet.classIndex()], dataSet.classAttribute().numValues());
        EntropyResult meanAttributeEntropy = meanAttributesEntropy(attributeValues, dataSet);
        values[7] = classResult.normalizedEntropy;
        values[8] = meanAttributeEntropy.normalizedEntropy;
        InfoGainAttributeEval infoGain = new InfoGainAttributeEval();
        try {
            infoGain.buildEvaluator(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        double maxMutualInformation = -Double.MAX_VALUE;
        double meanMutualInformation = 0;
        for (int i = 0; i < dataSet.numAttributes(); i++) {
            if (i != classIndex) {
                double mutualInformation = 0;
                try {
                    mutualInformation += infoGain.evaluateAttribute(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mutualInformation > maxMutualInformation) {
                    maxMutualInformation = mutualInformation;
                }
                meanMutualInformation += mutualInformation;
            }
        }
        meanMutualInformation /= attributeNumber;
        values[9] = meanMutualInformation;
        values[10] = maxMutualInformation;
        values[11] = classResult.entropy / meanMutualInformation;
        values[12] = (meanAttributeEntropy.entropy - meanMutualInformation) / meanMutualInformation;
        return new MetaFeatures(values);
    }

    private static double meanAttributeCorrelation(double[][] attributeValues, double mean[], double[] standardDeviation) {
        double meanCorrelation = 0;
        int attributeNumber = attributeValues.length;
        for (int i = 0; i < attributeNumber; i++) {
            for (int j = i; j < attributeNumber; j++) {
                meanCorrelation +=
                        MathUtils.covariance(attributeValues[i], attributeValues[j], mean[i], mean[j]) /
                        (standardDeviation[i] * standardDeviation[j]);
            }
        }
        meanCorrelation /= (1 + attributeNumber) * attributeNumber / 2;
        return meanCorrelation;
    }

    private static double meanSkewness(double[][] attributeValues, double[] mean, double[] standardDeviation) {
        double meanSkewness = 0;
        int attributeNumber = attributeValues.length;
        for (int i = 0 ; i < attributeNumber; i++) {
            meanSkewness += MathUtils.centralMoment(attributeValues[i], 3, mean[i]) / Math.pow(standardDeviation[i], 3);
        }
        meanSkewness /= attributeNumber;
        return meanSkewness;
    }

    private static double meanKurtosis(double[][] attributeValues, double[] mean, double[] standardDeviation) {
        double meanKurtosis = 0;
        int attributeNumber = attributeValues.length;
        for (int i = 0 ; i < attributeNumber; i++) {
            meanKurtosis += MathUtils.centralMoment(attributeValues[i], 4, mean[i]) / Math.pow(standardDeviation[i], 4) - 3;
        }
        meanKurtosis /= attributeNumber;
        return meanKurtosis;
    }

    private static EntropyResult meanAttributesEntropy(double[][] attributeValues, Instances instances) {
        double meanEntropy = 0;
        double meanNormalizedEntropy = 0;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (i != instances.classIndex()) {
                EntropyResult result = entropy(attributeValues[i], instances.attribute(i).numValues());
                meanEntropy += result.entropy;
                meanNormalizedEntropy += result.normalizedEntropy;
            }
        }
        meanEntropy /= instances.numAttributes() - 1;
        meanNormalizedEntropy /= instances.numAttributes() - 1;
        return new EntropyResult(meanEntropy, meanNormalizedEntropy);
    }

    private static EntropyResult entropy(double[] values, int numValues) {
        double[] distribution = new double[numValues];
        int count = 0;
        for (double v : values) {
            if (MathUtils.isCorrectValue(v)) {
                distribution[(int) v]++;
                count++;
            }
        }
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] /= count;
        }
        return new EntropyResult(ContingencyTables.entropy(distribution), count);
    }

    private static class EntropyResult {

        private static final double LOG_2 = Math.log(2);

        public final double entropy;
        public final double normalizedEntropy;

        public EntropyResult(double entropy, int count) {
            this.entropy = entropy;
            this.normalizedEntropy = entropy / (Math.log(count) / LOG_2);
        }

        public EntropyResult(double entropy, double normalizedEntropy) {
            this.entropy = entropy;
            this.normalizedEntropy = normalizedEntropy;
        }
    }

    public static final AbstractJSONCreator<MetaFeatures> JSON_CREATOR = new AbstractJSONCreator<MetaFeatures>() {
        @Override
        protected MetaFeatures throwableFromJSON(JSONObject jsonObject) {
            double[] values = new double[META_FEATURE_NUMBER];
            for (int i = 0; i < META_FEATURE_NUMBER; i++) {
                values[i] = jsonObject.getDouble(attributeDescription[i]);
            }
            return new MetaFeatures(values);
        }
    };
}
