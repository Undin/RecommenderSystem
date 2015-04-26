package com.ifmo.recommendersystem.metafeatures.decisiontree.unpruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeDevBranch;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeDevBranch extends TreeDevBranch {

    private static final String NAME = "unpruned dev branch";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeDevBranch() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
