package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.config.EvaluationConfig;
import com.ifmo.recommendersystem.evaluation.RecommenderSystemEvaluation;
import com.ifmo.recommendersystem.evaluation.RecommenderSystemMeanEvaluator;
import com.ifmo.recommendersystem.metafeatures.MetaFeatureConverter;
import com.ifmo.recommendersystem.utils.Pair;
import weka.attributeSelection.GeneticSearch;
import weka.core.Instances;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by warrior on 08.05.15.
 */
public class RecommenderSystemTest {

    private static final String EVALUATION_CONFIG = "evaluationConfig.json";
    private static final String EVALUATION_RESULT_DIRECTORY = "evaluationResults/geneticSearchSeparatedGeneral";

    private static final int ROUNDS = 200;

    public static void main(String[] args) throws IOException {
        EvaluationConfig config = new EvaluationConfig(EVALUATION_CONFIG);
        List<double[][]> matrices = config.getClassifiers().stream()
                .map(config::createEarrMatrix)
                .collect(Collectors.toList());
        Instances metaFeaturesList = MetaFeatureConverter.createInstances(config, RecommenderSystemBuilder.META_FEATURES_DIRECTORY, "metaFeatures");
        Pair<int[], Double> result = selectMetaFeatures(matrices, metaFeaturesList, config);

        System.out.println("RPR: " + result.second);
        printSelectedMetaFeatures(config, result.first);

        for (int i = 0; i < matrices.size(); i++) {
            double[][] matrix = matrices.get(i);
            RecommenderSystemEvaluation evaluation = new RecommenderSystemEvaluation(matrix, metaFeaturesList, config.getAlgorithms(), config.getDatasets());
            try {
                double rpr = evaluation.evaluateSubset(result.first);
                System.out.format("%s %f\n", config.getClassifiers().get(i).getName(), rpr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void printSelectedMetaFeatures(EvaluationConfig config, int[] subset) {
        IntStream.of(subset)
                .mapToObj(i -> config.getExtractors().get(i).getClass().getCanonicalName())
                .forEach(System.out::println);
    }

    private static Pair<int[], Double> selectMetaFeatures(List<double[][]> matrices, Instances metaFeaturesList, EvaluationConfig config) {
        EvaluateResult result = evaluateMetaFeatures(matrices, metaFeaturesList, config);
        System.out.println("maxRPR: " + result.maxRPR);
        printSelectedMetaFeatures(config, result.bestSubset);

        System.out.println();
        for (int i = 0; i < metaFeaturesList.numAttributes(); i++) {
            System.out.format("%s: %f\n", metaFeaturesList.attribute(i).name(), result.coef[i]);
        }

        int[] bestSubset = result.bestSubset;
        RecommenderSystemMeanEvaluator evaluator = new RecommenderSystemMeanEvaluator(matrices, metaFeaturesList, config.getAlgorithms(), config.getDatasets());
        double maxRPR = -Double.MAX_VALUE;

        try {
            maxRPR = evaluator.evaluateSubset(bestSubset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] sortedIndexes = IntStream.range(0, metaFeaturesList.numAttributes())
                .boxed()
                .sorted((o1, o2) -> Double.compare(result.coef[o2], result.coef[o1]))
                .mapToInt(i -> i)
                .toArray();

        for (int i = 0; i < sortedIndexes.length; i++) {
            int[] indexes = Arrays.copyOfRange(sortedIndexes, 0, i + 1);
            try {
                double rpr = evaluator.evaluateSubset(indexes);
                if (rpr > maxRPR) {
                    maxRPR = rpr;
                    bestSubset = indexes;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Pair.of(bestSubset, maxRPR);
    }

    private static EvaluateResult evaluateMetaFeatures(List<double[][]> matrices, Instances metaFeaturesList, EvaluationConfig config) {
        ForkJoinPool pool = new ForkJoinPool();
        return pool.invoke(new RecursiveTask<EvaluateResult>() {
            @Override
            protected EvaluateResult compute() {
                List<ForkJoinTask<Pair<Double, int[]>>> tasks = IntStream.range(0, ROUNDS)
                        .mapToObj(i -> new EvaluationTask(matrices, metaFeaturesList, config.getAlgorithms(), config.getDatasets()).fork())
                        .collect(Collectors.toList());

                double maxRPR = 0;
                int[] bestSubset = new int[0];
                double sumRPR = 0;
                double[] coef = new double[metaFeaturesList.numAttributes()];

                int number = 0;
                for (ForkJoinTask<Pair<Double, int[]>> task : tasks) {
                    Pair<Double, int[]> result = task.join();
                    System.out.println(number++);
                    if (result.first > maxRPR) {
                        maxRPR = result.first;
                        bestSubset = result.second;
                    }
                    sumRPR += result.first;
                    IntStream.of(result.second).forEach(i -> coef[i] += result.first);
                }
                return new EvaluateResult(maxRPR, bestSubset, sumRPR / ROUNDS, coef);
            }
        });
    }

    private static class EvaluateResult {
        public final double maxRPR;
        public final int[] bestSubset;
        public final double meanRPR;
        public final double[] coef;

        public EvaluateResult(double maxRPR, int[] bestSubset, double meanRPR, double[] coef) {
            this.maxRPR = maxRPR;
            this.bestSubset = bestSubset;
            this.meanRPR = meanRPR;
            this.coef = coef;
        }
    }

    private static class EvaluationTask extends RecursiveTask<Pair<Double, int[]>> {

        private final List<double[][]> matrices;
        private final Instances metaFeaturesList;
        private final List<FSSAlgorithm> algorithms;
        private final List<String> datasets;

        public EvaluationTask(List<double[][]> matrices, Instances metaFeaturesList, List<FSSAlgorithm> algorithms, List<String> datasets) {
            this.matrices = matrices;
            this.metaFeaturesList = metaFeaturesList;
            this.algorithms = algorithms;
            this.datasets = datasets;
        }

        @Override
        protected Pair<Double, int[]> compute() {
            GeneticSearch search = new GeneticSearch();
            search.setSeed(ThreadLocalRandom.current().nextInt());
            search.setMaxGenerations(50);
            RecommenderSystemMeanEvaluator evaluation = new RecommenderSystemMeanEvaluator(matrices, metaFeaturesList, algorithms, datasets);
            try {
                int[] indexes = search.search(evaluation, metaFeaturesList);
                BitSet subset = new BitSet(metaFeaturesList.numAttributes());
                IntStream.of(indexes).forEach(subset::set);
                double rpr = evaluation.evaluateSubset(subset);
                return Pair.of(rpr, indexes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Pair.of(0D, new int[]{});
        }
    }
}
