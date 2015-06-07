package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Mean;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Half;

public class MeanHalfPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public MeanHalfPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new Half(), new Mean());
    }
}