package com.ifmo.recommendersystem.metafeatures.classifierbased.knn;

import com.ifmo.recommendersystem.metafeatures.classifierbased.AbstractClassifierBasedExtractor;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.aggregate.First;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors.BestK;
import com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform.Full;

public class FullBestK extends AbstractClassifierBasedExtractor {

    public FullBestK() {
        super(new BestK(), new Full(), new First(), 1);
    }
}