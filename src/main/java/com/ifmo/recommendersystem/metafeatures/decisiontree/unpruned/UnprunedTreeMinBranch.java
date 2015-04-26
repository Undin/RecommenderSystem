package com.ifmo.recommendersystem.metafeatures.decisiontree.unpruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMinBranch;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeMinBranch extends TreeMinBranch {

    private static final String NAME = "unpruned min branch";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeMinBranch() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
