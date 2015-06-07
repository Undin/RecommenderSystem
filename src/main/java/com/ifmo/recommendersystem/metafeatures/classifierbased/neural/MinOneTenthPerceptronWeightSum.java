package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Min;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.OneTenth;

public class MinOneTenthPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public MinOneTenthPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new OneTenth(), new Min());
    }
}