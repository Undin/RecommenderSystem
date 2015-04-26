package com.ifmo.recommendersystem.metafeatures.decisiontree;

import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import weka.classifiers.trees.j48.ModelSelection;
import weka.core.Instances;

import java.util.function.ToDoubleFunction;

/**
 * Created by warrior on 21.04.15.
 */
public abstract class AbstractTreeExtractor extends MetaFeatureExtractor {

    private final boolean pruneTree;
    private final ToDoubleFunction<WrappedC45DecisionTree> function;

    public AbstractTreeExtractor(boolean pruneTree, ToDoubleFunction<WrappedC45DecisionTree> function) {
        this.pruneTree = pruneTree;
        this.function = function;
    }

    @Override
    protected double extractValue(Instances instances) {
        ModelSelection modelSelection = new WrappedC45ModelSelection(instances);
        try {
            WrappedC45DecisionTree tree = new WrappedC45DecisionTree(modelSelection, pruneTree);
            tree.buildClassifier(instances);
            return function.applyAsDouble(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Double.NaN;
    }
}
