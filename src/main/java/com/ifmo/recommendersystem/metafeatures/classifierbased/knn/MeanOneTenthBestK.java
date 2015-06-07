package com.ifmo.recommendersystem.metafeatures.classifierbased.knn;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Mean;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.BestK;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.OneTenth;

public class MeanOneTenthBestK extends AbstractClassifierBasedExtractor {

    public MeanOneTenthBestK() {
        super(new BestK(), new OneTenth(), new Mean());
    }
}