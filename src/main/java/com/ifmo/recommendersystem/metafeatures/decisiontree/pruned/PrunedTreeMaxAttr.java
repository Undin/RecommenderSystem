package com.ifmo.recommendersystem.metafeatures.decisiontree.pruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMaxAttr;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeMaxAttr extends TreeMaxAttr {

    private static final String NAME = "pruned max attr";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeMaxAttr() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
