package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Max;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Sqrt;

public class MaxSqrtPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public MaxSqrtPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new Sqrt(), new Max());
    }
}