package com.ifmo.recommendersystem.metafeatures.decisiontree.unpruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeNodeNumber;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeNodeNumber extends TreeNodeNumber {

    private static final String NAME = "unpruned node number";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeNodeNumber() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
