package com.ifmo.recommendersystem.metafeatures.classifierbased.knn;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.Max;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.BestK;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Half;

public class MaxHalfBestK extends AbstractClassifierBasedExtractor {

    public MaxHalfBestK() {
        super(new BestK(), new Half(), new Max());
    }
}