package com.ifmo.recommendersystem.metafeatures.decisiontree.pruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMeanAttr;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeMeanAttr extends TreeMeanAttr {

    private static final String NAME = "pruned mean attr";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeMeanAttr() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
