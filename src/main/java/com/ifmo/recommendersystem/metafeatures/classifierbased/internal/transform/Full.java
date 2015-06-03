package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform;

import com.ifmo.recommendersystem.metafeatures.classifierbased.Transform;
import weka.core.Instances;

/**
 * Created by warrior on 04.06.15.
 */
public class Full implements Transform {
    @Override
    public Instances transform(Instances instances) {
        return instances;
    }
}
