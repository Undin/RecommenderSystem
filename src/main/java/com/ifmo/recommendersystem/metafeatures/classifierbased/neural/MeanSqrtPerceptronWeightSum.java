package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Mean;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Sqrt;

public class MeanSqrtPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public MeanSqrtPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new Sqrt(), new Mean());
    }
}