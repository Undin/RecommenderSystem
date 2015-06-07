package com.ifmo.recommendersystem.metafeatures.classifierbased.knn;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.StdDev;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.BestK;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.OneTenth;

public class StdDevOneTenthBestK extends AbstractClassifierBasedExtractor {

    public StdDevOneTenthBestK() {
        super(new BestK(), new OneTenth(), new StdDev());
    }
}