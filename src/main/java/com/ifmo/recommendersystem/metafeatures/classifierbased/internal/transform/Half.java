package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform;

import weka.core.Instances;

/**
 * Created by warrior on 04.06.15.
 */
public class Half extends Part {

    @Override
    protected int resultInstanceNumber(Instances instances) {
        return instances.numInstances() / 2;
    }
}
