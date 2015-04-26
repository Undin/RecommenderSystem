package com.ifmo.recommendersystem.metafeatures.decisiontree.pruned;

import com.ifmo.recommendersystem.metafeatures.decisiontree.TreeMinAttr;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeMinAttr extends TreeMinAttr {

    private static final String NAME = "pruned min attr";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeMinAttr() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
