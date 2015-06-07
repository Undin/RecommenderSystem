package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Mean;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.OneTenth;

public class MeanOneTenthPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public MeanOneTenthPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new OneTenth(), new Mean());
    }
}