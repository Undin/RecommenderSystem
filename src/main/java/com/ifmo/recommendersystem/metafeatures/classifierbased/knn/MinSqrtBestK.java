package com.ifmo.recommendersystem.metafeatures.classifierbased.knn;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Min;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.BestK;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Sqrt;

public class MinSqrtBestK extends AbstractClassifierBasedExtractor {

    public MinSqrtBestK() {
        super(new BestK(), new Sqrt(), new Min());
    }
}