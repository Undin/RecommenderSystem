package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 23.04.15.
 */
public abstract class TreeMeanClass extends AbstractTreeExtractor {

    public TreeMeanClass(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::meanClass);
    }
}
