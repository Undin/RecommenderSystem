package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Max;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.OneTenth;

public class MaxOneTenthPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public MaxOneTenthPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new OneTenth(), new Max());
    }
}