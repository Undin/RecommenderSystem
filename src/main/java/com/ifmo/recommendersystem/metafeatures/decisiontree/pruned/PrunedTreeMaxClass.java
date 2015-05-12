package com.ifmo.recommendersystem.metafeatures.decisiontree.pruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMaxClass;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeMaxClass extends TreeMaxClass {

    private static final String NAME = "pruned max class";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeMaxClass() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
