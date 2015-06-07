package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Min;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Sqrt;

public class MinSqrtPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public MinSqrtPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new Sqrt(), new Min());
    }
}