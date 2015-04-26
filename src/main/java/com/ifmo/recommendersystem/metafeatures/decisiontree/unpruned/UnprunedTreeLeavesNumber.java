package com.ifmo.recommendersystem.metafeatures.decisiontree.unpruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeLeavesNumber;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeLeavesNumber extends TreeLeavesNumber {

    private static final String NAME = "unpruned leaves number";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeLeavesNumber() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
