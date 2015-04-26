package com.ifmo.recommendersystem.metafeatures.decisiontree;

/**
 * Created by warrior on 21.04.15.
 */
public abstract class TreeNodeNumber extends AbstractTreeExtractor {

    public TreeNodeNumber(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::numNodes);
    }
}
