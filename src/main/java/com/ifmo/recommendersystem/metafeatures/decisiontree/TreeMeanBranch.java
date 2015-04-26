package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 23.04.15.
 */
public abstract class TreeMeanBranch extends AbstractTreeExtractor {

    public TreeMeanBranch(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::meanBranch);
    }
}
