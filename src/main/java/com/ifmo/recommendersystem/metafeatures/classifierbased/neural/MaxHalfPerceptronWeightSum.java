package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Max;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Half;

public class MaxHalfPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public MaxHalfPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new Half(), new Max());
    }
}