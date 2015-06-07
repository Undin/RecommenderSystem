package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Min;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Half;

public class MinHalfPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public MinHalfPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new Half(), new Min());
    }
}