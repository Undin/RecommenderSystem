package com.ifmo.recommendersystem.metafeatures.classifierbased;

import weka.core.Instances;

/**
 * Created by warrior on 04.06.15.
 */
public interface Transform {

    public Instances transform(Instances instances);
}
