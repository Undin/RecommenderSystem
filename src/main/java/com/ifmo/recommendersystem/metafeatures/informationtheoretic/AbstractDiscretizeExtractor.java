package com.ifmo.recommendersystem.metafeatures.informationtheoretic;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;

/**
 * Created by warrior on 23.03.15.
 */
public abstract class AbstractDiscretizeExtractor extends MetaFeatureExtractor {

    @Override
    public double extractValue(Instances instances) {
        Discretize discretize = new Discretize();
        discretize.setUseBetterEncoding(true);
        try {
            discretize.setInputFormat(instances);
            instances = Filter.useFilter(instances, discretize);
            return extractValueInternal(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected boolean isNonClassNominalAttribute(Instances instances, int attributeIndex) {
        return isNonClassAttributeWithType(instances, attributeIndex, Attribute.NOMINAL);
    }

    protected abstract double extractValueInternal(Instances instances);
}
