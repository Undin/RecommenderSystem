package com.ifmo.recommendersystem.metafeatures.decisiontree.unpruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMeanClass;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeMeanClass extends TreeMeanClass {

    private static final String NAME = "unpruned mean class";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeMeanClass() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
