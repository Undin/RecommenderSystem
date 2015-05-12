package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 23.04.15.
 */
public abstract class TreeMaxClass extends AbstractTreeExtractor {

    public TreeMaxClass(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::maxClass);
    }
}
