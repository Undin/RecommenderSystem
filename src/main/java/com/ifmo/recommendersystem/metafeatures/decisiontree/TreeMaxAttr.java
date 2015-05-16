package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 12.05.15.
 */
public abstract class TreeMaxAttr extends AbstractTreeExtractor {

    public TreeMaxAttr(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::maxAttr);
    }
}
