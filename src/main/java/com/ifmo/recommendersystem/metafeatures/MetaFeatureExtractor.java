package com.ifmo.recommendersystem.metafeatures;

import weka.core.Attribute;
import weka.core.Instances;

import java.util.Arrays;


public abstract class MetaFeatureExtractor {
    public abstract String getName();

    /**
     * This method should return the result of computing value for meta feature with name.
     * Any specific arguments?
     */
    public MetaFeature extract(Instances instances) {
        double value = extractValue(instances);
        return new MetaFeature(this, value);
    }

    protected abstract double extractValue(Instances instances);

    protected boolean isNonClassAttributeWithType(Instances instances, int attributeIndex, int... types) {
        Attribute attribute = instances.attribute(attributeIndex);
        int type = attribute.type();
        return attributeIndex != instances.classIndex() && Arrays.stream(types).anyMatch(t -> t == type);
    }

    /**
     * Creates a new instance of a meta feature extractor given it's class name.
     *
     * @param className the fully qualified class name of the meta feature extractor
     * @return new instance of the meta feature extractor. If the extractor name is invalid return null
     */
    public static MetaFeatureExtractor forName(String className) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        if (!MetaFeatureExtractor.class.isAssignableFrom(clazz)) {
            return null;
        }
        Object newInstance;
        try {
            newInstance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return (MetaFeatureExtractor) newInstance;
    }
}
