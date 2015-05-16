package com.ifmo.recommendersystem.metafeatures.decisiontree;

import weka.classifiers.trees.j48.C45ModelSelection;
import weka.core.Instances;

/**
 * Created by warrior on 20.04.15.
 */
public class WrappedC45ModelSelection extends C45ModelSelection {

    private static final int MIN_NO_OBJ = 2;
    private static final boolean USE_MDL_CORRECTION = true;
    private static final boolean DO_NOT_MAKE_SPLIT_POINT_ACTUAL_VALUE = false;

    private final int classNumber;
    private final int attributeNumber;
    /**
     * Initializes the split selection method with the given parameters.
     *
     * @param allData  FULL training dataset (necessary for
     * selection of split points).
     */
    public WrappedC45ModelSelection(Instances allData) {
        super(MIN_NO_OBJ, allData, USE_MDL_CORRECTION, DO_NOT_MAKE_SPLIT_POINT_ACTUAL_VALUE);
        classNumber = allData.numClasses();
        attributeNumber = allData.numAttributes() - 1;
    }

    public int getClassNumber() {
        return classNumber;
    }

    public int getAttributeNumber() {
        return attributeNumber;
    }
}
