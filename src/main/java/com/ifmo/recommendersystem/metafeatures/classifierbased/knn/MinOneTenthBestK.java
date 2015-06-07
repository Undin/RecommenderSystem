package com.ifmo.recommendersystem.metafeatures.classifierbased.knn;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Min;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.BestK;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.OneTenth;

public class MinOneTenthBestK extends AbstractClassifierBasedExtractor {

    public MinOneTenthBestK() {
        super(new BestK(), new OneTenth(), new Min());
    }
}