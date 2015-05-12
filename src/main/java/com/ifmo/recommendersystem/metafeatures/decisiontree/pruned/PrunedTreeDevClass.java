package com.ifmo.recommendersystem.metafeatures.decisiontree.pruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeDevClass;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeDevClass extends TreeDevClass {

    private static final String NAME = "pruned dev class";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeDevClass() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
