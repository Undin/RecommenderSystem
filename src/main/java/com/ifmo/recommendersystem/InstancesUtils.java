package com.ifmo.recommendersystem;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;

import java.util.*;

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
            instances.deleteStringAttributes();
        }
        return instances;
    }

    public static Instances selectAttributes(Instances instances, ASSearch search, ASEvaluation evaluation) {
        AttributeSelection filter = new AttributeSelection();
        filter.setSearch(search);
        filter.setEvaluator(evaluation);
        try {
            filter.setInputFormat(instances);
            return Filter.useFilter(instances, filter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Instances removeAttributes(Instances intstances, Instances pattern) {
        Set<String> attributes = new HashSet<>(pattern.numAttributes());
        for (int i = 0; i < pattern.numAttributes(); i++) {
            attributes.add(pattern.attribute(i).name());
        }
        TIntList removingAttributes = new TIntArrayList(intstances.numAttributes() - pattern.numAttributes());
        for (int i = 0; i < intstances.numAttributes(); i++) {
            if (!attributes.contains(intstances.attribute(i).name())) {
                removingAttributes.add(i);
            }
        }
        Remove remove = new Remove();
        try {
            remove.setAttributeIndicesArray(removingAttributes.toArray());
            remove.setInputFormat(intstances);
            return Filter.useFilter(intstances, remove);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static void printAttr(Instances instances) {
        for (int i = 0; i < instances.numAttributes(); i++) {
            System.out.println(instances.attribute(i).name());
        }
    }
}
