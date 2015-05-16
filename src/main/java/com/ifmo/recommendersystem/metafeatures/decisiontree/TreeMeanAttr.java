package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 12.05.15.
 */
public abstract class TreeMeanAttr extends AbstractTreeExtractor {

    public TreeMeanAttr(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::meanAttr);
    }
}
