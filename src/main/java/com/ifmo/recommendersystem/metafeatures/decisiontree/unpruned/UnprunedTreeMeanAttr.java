package com.ifmo.recommendersystem.metafeatures.decisiontree.unpruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMeanAttr;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeMeanAttr extends TreeMeanAttr {

    private static final String NAME = "unpruned mean attr";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeMeanAttr() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
