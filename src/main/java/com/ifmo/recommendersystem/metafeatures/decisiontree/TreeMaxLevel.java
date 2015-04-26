package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 21.04.15.
 */
public abstract class TreeMaxLevel extends AbstractTreeExtractor {

    public TreeMaxLevel(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::maxLevel);
    }
}
