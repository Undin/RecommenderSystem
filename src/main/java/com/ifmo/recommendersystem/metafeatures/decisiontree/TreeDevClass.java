package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 23.04.15.
 */
public abstract class TreeDevClass extends AbstractTreeExtractor {

    public TreeDevClass(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::devClass);
    }
}
