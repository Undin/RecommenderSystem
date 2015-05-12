package com.ifmo.recommendersystem.metafeatures.decisiontree.unpruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMinClass;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeMinClass extends TreeMinClass {

    private static final String NAME = "unpruned min class";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeMinClass() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
