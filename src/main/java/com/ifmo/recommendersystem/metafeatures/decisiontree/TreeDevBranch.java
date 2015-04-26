package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 23.04.15.
 */
public abstract class TreeDevBranch extends AbstractTreeExtractor {

    public TreeDevBranch(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::devBranch);
    }
}


