package com.ifmo.recommendersystem.metafeatures.classifierbased.neural;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.First;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.PerceptronWeightSum;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Full;

public class FullPerceptronWeightSum extends AbstractClassifierBasedExtractor {

    public FullPerceptronWeightSum() {
        super(new PerceptronWeightSum(), new Full(), new First(), 1);
    }
}