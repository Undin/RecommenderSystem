package com.ifmo.recommendersystem.metafeatures.decisiontree.unpruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMeanBranch;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeMeanBranch extends TreeMeanBranch {

    private static final String NAME = "unpruned mean branch";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeMeanBranch() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
