package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 23.04.15.
 */
public abstract class TreeMinClass extends AbstractTreeExtractor {

    public TreeMinClass(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::minClass);
    }
}
