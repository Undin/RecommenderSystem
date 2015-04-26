package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 23.04.15.
 */
public abstract class TreeMinBranch extends AbstractTreeExtractor {

    public TreeMinBranch(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::minBranch);
    }
}
