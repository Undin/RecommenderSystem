package com.ifmo.recommendersystem.metafeatures.decisiontree;

import com.ifmo.recommendersystem.utils.StatisticalUtils;
import weka.classifiers.trees.j48.C45PruneableClassifierTree;
import weka.classifiers.trees.j48.C45Split;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.classifiers.trees.j48.ModelSelection;
import weka.core.Instances;

import java.util.stream.DoubleStream;

/**
 * Created by warrior on 19.04.15.
 */
public class WrappedC45DecisionTree extends C45PruneableClassifierTree {

    private static final float CF = 0.25F;
    private static final boolean RAISE_TREE = true;
    private static final boolean CLEANUP = true;
    private static final boolean COLLAPSE_TREE = true;

    private final boolean pruneTree;

    /**
     * Constructor for pruneable tree structure. Stores reference
     * to associated training data at each node.
     *
     * @param toSelectLocModel selection method for local splitting model
     * @param pruneTree        true if the tree is to be pruned
     * @throws Exception if something goes wrong
     */
    public WrappedC45DecisionTree(ModelSelection toSelectLocModel, boolean pruneTree) throws Exception {
        super(toSelectLocModel, pruneTree, CF, RAISE_TREE, CLEANUP, COLLAPSE_TREE);
        this.pruneTree = pruneTree;
    }

    @Override
    protected WrappedC45DecisionTree getNewTree(Instances data) throws Exception {
        WrappedC45DecisionTree newTree = new WrappedC45DecisionTree(m_toSelectModel, pruneTree);
        newTree.buildTree(data, RAISE_TREE || !CLEANUP);
        return newTree;
    }

    public int getHeight() {
        if (m_isLeaf) {
            return 0;
        }
        int childrenMaxHeight = 0;
        for (ClassifierTree m_son : m_sons) {
            WrappedC45DecisionTree child = (WrappedC45DecisionTree) m_son;
            childrenMaxHeight = Math.max(childrenMaxHeight, child.getHeight());
        }

        return childrenMaxHeight + 1;
    }

    public int getWidth() {
        int height = getHeight();
        int width = 0;
        for (int level = 0; level <= height; level++) {
            width = Math.max(width, getLevelWidth(level));
        }
        return width;
    }

    protected int getLevelWidth(int level) {
        if (level == 0) {
            return 1;
        }
        if (m_isLeaf) {
            return 0;
        }
        int levelWidth = 0;
        for (ClassifierTree son : m_sons) {
            WrappedC45DecisionTree child = (WrappedC45DecisionTree) son;
            levelWidth += child.getLevelWidth(level - 1);
        }
        return levelWidth;
    }

    public double maxLevel() {
        return max(countLevels());
    }

    public double meanLevel() {
        return mean(countLevels());
    }

    public double devLevel() {
        return dev(countLevels());
    }

    protected double[] countLevels() {
        int height = getHeight();
        double[] levels = new double[height + 1];
        for (int i = 0; i <= height; i++) {
            countLevels(levels, i, i);
        }
        return levels;
    }

    private void countLevels(double[] levels, int level, int height) {
        if (m_isLeaf) {
            return;
        }
        if (height == 0) {
            levels[level]++;
        }
        for (ClassifierTree son : m_sons) {
            WrappedC45DecisionTree child = (WrappedC45DecisionTree) son;
            child.countLevels(levels, level, height - 1);
        }
    }

    public double maxClass() {
        return max(countClass());
    }

    public double minClass() {
        return min(countClass());
    }

    public double meanClass() {
        return mean(countClass());
    }

    public double devClass() {
        return dev(countClass());
    }

    private double[] countClass() {
        double[] attrs = new double[((WrappedC45ModelSelection) m_toSelectModel).getClassNumber()];
        countClass(attrs);
        return attrs;
    }

    private void countClass(double[] attrs) {
        if (m_isLeaf) {
            attrs[m_localModel.distribution().maxClass()]++;
        } else {
            for (ClassifierTree son : m_sons) {
                WrappedC45DecisionTree child = (WrappedC45DecisionTree) son;
                child.countClass(attrs);
            }
        }
    }

    public double maxBranch() {
        return max(countBranches());
    }

    public double minBranch() {
        return min(countBranches());
    }

    public double meanBranch() {
        return mean(countBranches());
    }

    public double devBranch() {
        return dev(countBranches());
    }

    private double[] countBranches() {
        double[] branches = new double[numLeaves()];
        countBranches(branches, 0, 0);
        return branches;
    }

    private int countBranches(double[] branches, int length, int index) {
        if (m_isLeaf) {
            branches[index] = length;
            return index + 1;
        } else {
            for (ClassifierTree son : m_sons) {
                WrappedC45DecisionTree child = (WrappedC45DecisionTree) son;
                index = child.countBranches(branches, length + 1, index);
            }
            return index;
        }
    }

    public double maxAttr() {
        return max(countAttrs());
    }

    public double minAttr() {
        return min(countAttrs());
    }

    public double meanAttr() {
        return mean(countAttrs());
    }

    public double devAttr() {
        return dev(countAttrs());
    }

    private double[] countAttrs() {
        double[] attrs = new double[((WrappedC45ModelSelection) m_toSelectModel).getAttributeNumber()];
        countAttrs(attrs);
        return attrs;
    }

    private void countAttrs(double[] attrs) {
        if (!m_isLeaf) {
            C45Split splitModel = (C45Split) m_localModel;
            attrs[splitModel.attIndex()]++;
            for (ClassifierTree son : m_sons) {
                WrappedC45DecisionTree child = (WrappedC45DecisionTree) son;
                child.countAttrs(attrs);
            }
        }
    }

    private static double max(double[] values) {
        return DoubleStream.of(values)
                .max()
                .getAsDouble();
    }

    private static double min(double[] values) {
        return DoubleStream.of(values)
                .min()
                .getAsDouble();
    }

    private static double mean(double[] values) {
        return StatisticalUtils.mean(values);
    }

    private static double dev(double[] values) {
        if (values.length == 1) {
            return 0;
        }
        double mean = mean(values);
        double sum = DoubleStream.of(values)
                .map(d -> Math.pow(d - mean, 2))
                .sum();
        return Math.sqrt(sum) / (values.length - 1);
    }

}