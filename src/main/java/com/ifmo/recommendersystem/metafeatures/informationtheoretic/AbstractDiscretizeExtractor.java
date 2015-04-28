package com.ifmo.recommendersystem.metafeatures.informationtheoretic;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import com.ifmo.recommendersystem.utils.InstancesUtils;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * Created by warrior on 23.03.15.
 */
public abstract class AbstractDiscretizeExtractor extends MetaFeatureExtractor {

    @Override
    public double extractValue(Instances instances) {
        Instances discretizeInstances = InstancesUtils.discretize(instances);
        if (discretizeInstances != null) {
            return extractValueInternal(discretizeInstances);
        }
        return 0;
    }

    protected boolean isNonClassNominalAttribute(Instances instances, int attributeIndex) {
        return isNonClassAttributeWithType(instances, attributeIndex, Attribute.NOMINAL);
    }

    protected abstract double extractValueInternal(Instances instances);
}
