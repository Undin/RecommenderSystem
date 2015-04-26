package com.ifmo.recommendersystem.metafeatures.decisiontree;

import weka.classifiers.trees.j48.C45ModelSelection;
import weka.core.Instances;

/**
 * Created by warrior on 20.04.15.
 */
public class WrappedC45ModelSelection extends C45ModelSelection {

    private static final int MIN_NO_OBJ = 2;

    private final int classNumber;
    /**
     * Initializes the split selection method with the given parameters.
     *
     * @param allData  FULL training dataset (necessary for
     * selection of split points).
     */
    public WrappedC45ModelSelection(Instances allData) {
        super(MIN_NO_OBJ, allData);
        classNumber = allData.numClasses();
    }

    public int getClassNumber() {
        return classNumber;
    }
}
