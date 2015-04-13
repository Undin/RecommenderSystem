package com.ifmo.recommendersystem.metafeatures.informationtheoretic;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Instances;

/**
 * Created by warrior on 23.03.15.
 */
public class MeanMutualInformation extends MetaFeatureExtractor {

    public static final String NAME = "Mean mutual information of class and attribute";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double extractValue(Instances instances) {
        InfoGainAttributeEval infoGain = new InfoGainAttributeEval();
        try {
            infoGain.buildEvaluator(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
        double meanMutualInformation = 0;
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (i != instances.classIndex()) {
                try {
                    meanMutualInformation += infoGain.evaluateAttribute(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return meanMutualInformation;
    }
}
