package com.ifmo.recommendersystem.metafeatures.classifierbased.knn;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.StdDev;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.BestK;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Sqrt;

public class StdDevSqrtBestK extends AbstractClassifierBasedExtractor {

    public StdDevSqrtBestK() {
        super(new BestK(), new Sqrt(), new StdDev());
    }
}