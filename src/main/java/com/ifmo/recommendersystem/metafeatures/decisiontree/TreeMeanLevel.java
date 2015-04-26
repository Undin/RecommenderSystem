package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 21.04.15.
 */
public abstract class TreeMeanLevel extends AbstractTreeExtractor {

    public TreeMeanLevel(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::meanLevel);
    }
}
