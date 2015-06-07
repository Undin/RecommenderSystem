package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.StdDev;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.OneTenth;

public class StdDevOneTenthPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public StdDevOneTenthPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new OneTenth(), new StdDev());
    }
}