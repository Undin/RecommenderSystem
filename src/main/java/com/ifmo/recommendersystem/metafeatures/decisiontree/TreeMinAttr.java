package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 12.05.15.
 */
public abstract class TreeMinAttr extends AbstractTreeExtractor {

    public TreeMinAttr(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::minAttr);
    }
}
