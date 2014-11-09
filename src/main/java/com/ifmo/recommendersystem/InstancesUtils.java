package com.ifmo.recommendersystem;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class InstancesUtils {

    private static final String CLASS_ATTRIBUTE_NAME = "class";

    public static Instances createInstances(String filename, boolean removeStringAttributes) throws Exception {
        Instances instances = ConverterUtils.DataSource.read(filename);
        if (removeStringAttributes) {
            instances = removeStringAttributes(instances, false);
        }
        Attribute attribute = instances.attribute(CLASS_ATTRIBUTE_NAME);
        if (attribute != null) {
            instances.setClassIndex(attribute.index());
        }
        return instances;
    }

    public static Instances createInstances(String filename) throws Exception {
        return createInstances(filename, false);
    }

    public static Instances removeStringAttributes(Instances instances, boolean copy) {
        if (instances.checkForStringAttributes()) {
            if (copy) {
                instances = new Instances(instances);
            }
            for (int i = instances.numAttributes() - 1; i >= 0; i--) {
                if (instances.attribute(i).isString()) {
                    instances.deleteAttributeAt(i);
                }
            }
        }
        return instances;
    }
}
