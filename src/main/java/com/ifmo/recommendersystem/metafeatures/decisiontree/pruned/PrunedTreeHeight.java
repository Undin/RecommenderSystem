package com.ifmo.recommendersystem.metafeatures.decisiontree.pruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeHeight;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeHeight extends TreeHeight {

    private static final String NAME = "pruned dev height";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeHeight() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
