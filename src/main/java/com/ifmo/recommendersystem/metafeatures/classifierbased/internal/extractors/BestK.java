package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.extractors;

import com.ifmo.recommendersystem.metafeatures.classifierbased.Extractor;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by warrior on 04.06.15.
 */
public class BestK implements Extractor {

    private static final int MAX_K = 20;
    private static final int FOLDS = 5;

    @Override
    public double extract(Instances instances) {
        Random random = new Random();
        int bestK = 0;
        double maxFMeasure = -Double.MAX_VALUE;
        try {
            for (int k = 1; k <= MAX_K; k++) {
                Classifier classifier = new IBk(k);
                Evaluation evaluation = new Evaluation(instances);
                evaluation.crossValidateModel(classifier, instances, FOLDS, random);
                double fMeasure = calculateF1Measure(evaluation);
                if (fMeasure > maxFMeasure) {
                    maxFMeasure = fMeasure;
                    bestK = k;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("bestK: " + bestK);
        return bestK;
    }

    private double calculateF1Measure(Evaluation evaluation) {
        int numClasses = evaluation.confusionMatrix().length;
        double precision = IntStream.range(0, numClasses)
                .mapToDouble(evaluation::precision)
                .average()
                .getAsDouble();
        double recall = IntStream.range(0, numClasses)
                .mapToDouble(evaluation::recall)
                .average()
                .getAsDouble();
        if (precision + recall == 0) {
            return 0;
        }
        return 2 * precision * recall / (precision + recall);
    }
}
