package com.ifmo.recommendersystem.metafeatures.decisiontree.pruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMinClass;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeMinClass extends TreeMinClass {

    private static final String NAME = "pruned min class";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeMinClass() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
