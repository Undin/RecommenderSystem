package com.ifmo.recommendersystem.metafeatures.classifierbased.knn;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Mean;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.BestK;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Sqrt;

public class MeanSqrtBestK extends AbstractClassifierBasedExtractor {

    public MeanSqrtBestK() {
        super(new BestK(), new Sqrt(), new Mean());
    }
}