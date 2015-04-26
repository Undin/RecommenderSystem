package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 20.04.15.
 */
public abstract class TreeHeight extends AbstractTreeExtractor {

    public TreeHeight(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::getHeight);
    }
}
