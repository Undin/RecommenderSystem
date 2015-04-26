package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 21.04.15.
 */
public abstract class TreeLeavesNumber extends AbstractTreeExtractor {

    public TreeLeavesNumber(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::numLeaves);
    }
}
