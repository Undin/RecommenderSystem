package com.ifmo.recommendersystem.metafeatures.decisiontree.unpruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMaxLevel;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeMaxLevel extends TreeMaxLevel {

    private static final String NAME = "unpruned max level";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeMaxLevel() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
