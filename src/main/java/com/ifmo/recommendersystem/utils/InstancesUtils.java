package com.ifmo.recommendersystem.utils;

import com.ifmo.recommendersystem.utils.StatisticalUtils;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
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

    public static final int REMOVE_STRING_ATTRIBUTES = 1;
    public static final int REMOVE_UNINFORMATIVE_ATTRIBUTES = 2; // variance == 0

    public static Instances createInstances(String filename, int modifiedFlags) throws Exception {
        Instances instances = ConverterUtils.DataSource.read(filename);
        Attribute attribute = instances.attribute(CLASS_ATTRIBUTE_NAME);
        if (attribute != null) {
            instances.setClassIndex(attribute.index());
        }
        boolean removeStringAttributes = (modifiedFlags & REMOVE_STRING_ATTRIBUTES) != 0;
        boolean removeUninformativeAttributes = (modifiedFlags & REMOVE_UNINFORMATIVE_ATTRIBUTES) != 0;
        if (removeStringAttributes || removeUninformativeAttributes) {
            TIntSet removingAttributes = new TIntHashSet();
            for (int i = 0; i < instances.numAttributes(); i++) {
                Attribute attr = instances.attribute(i);
                if (removeStringAttributes && attr.isString()) {
                    removingAttributes.add(i);
                }
                if (removeUninformativeAttributes && StatisticalUtils.variance(instances.attributeToDoubleArray(i)) == 0) {
                    removingAttributes.add(i);
                }
            }

            instances = removeAttribute(instances, removingAttributes.toArray());
        }
        return instances;
    }

    public static Instances createInstances(String filename) throws Exception {
        return createInstances(filename, 0);
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

    public static Instances removeAttributes(Instances instances, Instances pattern) {
        Set<String> attributes = new HashSet<>(pattern.numAttributes());
        for (int i = 0; i < pattern.numAttributes(); i++) {
            attributes.add(pattern.attribute(i).name());
        }
        TIntList removingAttributes = new TIntArrayList(instances.numAttributes() - pattern.numAttributes());
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (!attributes.contains(instances.attribute(i).name())) {
                removingAttributes.add(i);
            }
        }
        return removeAttribute(instances, removingAttributes.toArray());
    }

    private static Instances removeAttribute(Instances instances, int[] removingAttributes) {
        Remove remove = new Remove();
        try {
            remove.setAttributeIndicesArray(removingAttributes);
            remove.setInputFormat(instances);
            return Filter.useFilter(instances, remove);
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
