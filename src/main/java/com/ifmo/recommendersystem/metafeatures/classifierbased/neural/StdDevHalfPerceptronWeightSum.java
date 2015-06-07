package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.StdDev;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Half;

public class StdDevHalfPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public StdDevHalfPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new Half(), new StdDev());
    }
}