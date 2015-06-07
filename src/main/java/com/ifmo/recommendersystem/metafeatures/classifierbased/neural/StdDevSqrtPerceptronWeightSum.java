package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.StdDev;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Sqrt;

public class StdDevSqrtPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public StdDevSqrtPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new Sqrt(), new StdDev());
    }
}