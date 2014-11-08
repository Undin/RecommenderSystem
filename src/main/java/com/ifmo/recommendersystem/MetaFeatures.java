package com.ifmo.recommendersystem;

import org.json.JSONObject;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.*;

public class MetaFeatures implements JSONConverted {

    public static final int META_FEATURE_NUMBER = 13;
    
    private static final double LOG_2 = Math.log(2);

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
        if (InstancesUtils.hasStringAttribute(dataSet)) {
            dataSet = InstancesUtils.removeStringAttributes(dataSet, true);
        }
        int classIndex = dataSet.classIndex();
        int attributeNumber = dataSet.numAttributes() - 1;
        double[] values = new double[META_FEATURE_NUMBER];
        values[0] = dataSet.numInstances();
        values[1] = attributeNumber;
        values[2] = dataSet.numClasses();
        values[3] = values[0] / values[1];
        double[][] attributeValues = new double[attributeNumber][];
        double[] classValues = null;
        int next = 0;
        for (int i = 0; i < dataSet.numAttributes(); i++) {
            if (i != classIndex) {
                attributeValues[next] = dataSet.attributeToDoubleArray(i);
                next++;
            } else {
                classValues = dataSet.attributeToDoubleArray(i);
            }
        }
        values[4] = meanAttributeCorrelation(attributeValues);
        double[] mean = new double[attributeNumber];
        double[] standardDeviation = new double[attributeNumber];
        for (int i = 0; i < attributeNumber; i++) {
            mean[i] = MathUtils.mean(attributeValues[i]);
            standardDeviation[i] = Math.sqrt(MathUtils.variance(attributeValues[i], mean[i]));
        }
        values[5] = meanSkewness(attributeValues, mean, standardDeviation);
        values[6] = meanKurtosis(attributeValues, mean, standardDeviation);
        double classEntropy = ContingencyTables.entropy(classValues);
        double meanAttributeEntropy = meanAttributesEntropy(attributeValues);
        values[7] = classEntropy / (Math.log(dataSet.numInstances()) / LOG_2);
        values[8] = meanAttributeEntropy / ((Math.log(dataSet.numInstances()) / LOG_2));
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
        values[11] = classEntropy / meanMutualInformation;
        values[12] = (meanAttributeEntropy - meanMutualInformation) / meanMutualInformation;
        return new MetaFeatures(values);
    }

    private static double meanAttributeCorrelation(double[][] attributeValues) {
        double meanCorrelation = 0;
        int attributeNumber = attributeValues.length;
        int valuesNumber = attributeValues[0].length;
        for (double[] attributeValue1 : attributeValues) {
            for (double[] attributeValue2 : attributeValues) {
                meanCorrelation += weka.core.Utils.correlation(attributeValue1,
                                                               attributeValue2,
                                                               valuesNumber);
            }
        }
        meanCorrelation /= attributeNumber * attributeNumber;
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

    private static double meanAttributesEntropy(double[][] attributeValues) {
        double meanNormalizedEntropy = 0;
        for (double[] attributeValue : attributeValues) {
            meanNormalizedEntropy += ContingencyTables.entropy(attributeValue);
        }
        meanNormalizedEntropy /= attributeValues[0].length;
        return meanNormalizedEntropy;
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
