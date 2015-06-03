package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform;

import weka.core.Instances;

/**
 * Created by warrior on 04.06.15.
 */
public class OneTenth extends Part {
    @Override
    protected int resultAttributeNumber(Instances instances) {
        return (instances.numAttributes() + 9) / 10;
    }
}
