package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.config.BuildConfig;
import com.ifmo.recommendersystem.config.EvaluationConfig;
import com.ifmo.recommendersystem.evaluation.RecommenderSystemEvaluation;
import com.ifmo.recommendersystem.evaluation.RecommenderSystemMeanEvaluator;
import com.ifmo.recommendersystem.metafeatures.MetaFeatureConverter;
import com.ifmo.recommendersystem.metafeatures.MetaFeatureExtractor;
import com.ifmo.recommendersystem.utils.InstancesUtils;
import com.ifmo.recommendersystem.utils.Pair;
import com.ifmo.recommendersystem.utils.PathUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by warrior on 23.05.15.
 */
public class Results {

    public static List<Pair<String, int[]>> subsets = new ArrayList<>();
    static {
        subsets.add(Pair.of("recsys", range(0, 13)));
        subsets.add(Pair.of("general", range(0, 4)));
        subsets.add(Pair.of("statistical", twoRange(4, 7, 13, 15)));
        subsets.add(Pair.of("informationtheoretic", range(7, 13)));
        subsets.add(Pair.of("standard", range(0, 15)));
        subsets.add(Pair.of("pruned", range(15, 34)));
        subsets.add(Pair.of("unpruned", range(34, 53)));
        subsets.add(Pair.of("tree", range(15, 53)));
        subsets.add(Pair.of("neural", range(66, 79)));
        subsets.add(Pair.of("knn", range(53, 66)));
        subsets.add(Pair.of("classifier base", range(15, 79)));
        subsets.add(Pair.of("all", range(0, 79)));

    }
    
    private static int[] range(int l, int r) {
        return IntStream.range(l, r).toArray();
    }

    private static int[] twoRange(int l1, int r1, int l2, int r2) {
        return IntStream.concat(IntStream.range(l1, r1), IntStream.range(l2, r2)).toArray();
    }

    public static void main(String[] args) throws Exception {
        printDataSetsTable("instances.xlsx");
        printAlgorithms("algorithms.xlsx");
        printFirstResults("firstResults.xlsx");
        printResultsForEachClassifier("results.xlsx");
        printResultForAllClassifiers("allResults.xlsx");
        printMutualInformation("mutualInformation.xlsx");
    }

    public static void check() {
        EvaluationConfig config = new EvaluationConfig("evaluationConfig.json");
        for (String dataset : config.getDatasets()) {
            for (MetaFeatureExtractor extractor : config.getExtractors()) {
                File file = new File(PathUtils.createPath(RecommenderSystemBuilder.META_FEATURES_DIRECTORY,
                        dataset,
                        extractor.getClass().getCanonicalName() + ".json"));
                if (!file.exists()) {
                    System.out.println(file.getName());
                }
            }
        }
    }

    public static void printMutualInformation(String filename) throws Exception {
        EvaluationConfig config = new EvaluationConfig("evaluationConfig.json");
        Instances metaFeaturesList = MetaFeatureConverter.createInstances(config, RecommenderSystemBuilder.META_FEATURES_DIRECTORY, "metaFeatures");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow firstRow = sheet.createRow(0);
        for (int i = 0; i < metaFeaturesList.numAttributes(); i++) {
            sheet.createRow(i + 1).createCell(0).setCellValue(simplify(metaFeaturesList.attribute(i).name()));
        }
        for (int i = 0; i < config.getClassifiers().size(); i++) {
            ClassifierWrapper classifier = config.getClassifiers().get(i);
            firstRow.createCell(i + 1).setCellValue(classifier.getName());
            metaFeaturesList = MetaFeatureConverter.createInstances(config, RecommenderSystemBuilder.META_FEATURES_DIRECTORY, "metaFeatures", classifier);
            InfoGainAttributeEval infoGain = new InfoGainAttributeEval();
            infoGain.buildEvaluator(metaFeaturesList);
            for (int j = 0; j < metaFeaturesList.numAttributes() - 1; j++) {
                sheet.getRow(j + 1).createCell(i + 1).setCellValue(infoGain.evaluateAttribute(j));
            }
        }
        try (FileOutputStream output = new FileOutputStream(filename)) {
            workbook.write(output);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static void printResultForAllClassifiers(String filename) {
        EvaluationConfig config = new EvaluationConfig("evaluationConfig.json");
        Instances metaFeaturesList = MetaFeatureConverter.createInstances(config, RecommenderSystemBuilder.META_FEATURES_DIRECTORY, "metaFeatures");
        List<double[][]> matrices = config.getClassifiers().stream()
                .map(config::createEarrMatrix)
                .collect(Collectors.toList());
        File root = new File("results/all");
        root.mkdirs();
        RecommenderSystemMeanEvaluator evaluation = new RecommenderSystemMeanEvaluator(matrices, metaFeaturesList, config.getAlgorithms(), config.getDatasets());
        RecommenderSystemTest.EvaluateResult result = RecommenderSystemTest.evaluateMetaFeatures(evaluation, metaFeaturesList);
        try (PrintWriter writer = new PrintWriter(new File(root, "all.txt"))) {
            IntStream.of(result.bestSubset)
                    .mapToObj(k -> simplify(metaFeaturesList.attribute(k).name()))
                    .forEach(writer::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow firstRow = sheet.createRow(0);
        for (int i = 0; i < config.getClassifiers().size(); i++) {
            firstRow.createCell(i).setCellValue(config.getClassifiers().get(i).getName());
        }
        XSSFRow secondRow = sheet.createRow(1);
        for (int j = 0; j < config.getClassifiers().size(); j++) {
            RecommenderSystemEvaluation ev = new RecommenderSystemEvaluation(matrices.get(j), metaFeaturesList, config.getAlgorithms(), config.getDatasets());
            try {
                secondRow.createCell(j).setCellValue(ev.evaluateSubset(result.bestSubset));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sheet = workbook.createSheet();
        for (int i = 0; i < metaFeaturesList.numAttributes(); i++) {
            XSSFRow row = sheet.createRow(i);
            row.createCell(0).setCellValue(simplify(metaFeaturesList.attribute(i).name()));
            row.createCell(1).setCellValue(result.coef[i] / 100);
        }
        try (FileOutputStream output = new FileOutputStream(new File(root, filename))) {
            workbook.write(output);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static void printResultsForEachClassifier(String filename) {
        EvaluationConfig config = new EvaluationConfig("evaluationConfig.json");
        Instances metaFeaturesList = MetaFeatureConverter.createInstances(config, RecommenderSystemBuilder.META_FEATURES_DIRECTORY, "metaFeatures");
        List<double[][]> matrices = config.getClassifiers().stream()
                .map(config::createEarrMatrix)
                .collect(Collectors.toList());
        File root = new File("results/subsets");
        root.mkdirs();
        List<RecommenderSystemTest.EvaluateResult> evaluateResults = new ArrayList<>();
        for (int i = 0; i < matrices.size(); i++) {
            System.out.println(config.getClassifiers().get(i).getName());
            File classifierResult = new File(root, config.getClassifiers().get(i).getName() + ".txt");
            RecommenderSystemEvaluation evaluation = new RecommenderSystemEvaluation(matrices.get(i), metaFeaturesList, config.getAlgorithms(), config.getDatasets());
            RecommenderSystemTest.EvaluateResult result = RecommenderSystemTest.evaluateMetaFeatures(evaluation, metaFeaturesList);
            evaluateResults.add(result);
            try (PrintWriter writer = new PrintWriter(classifierResult)) {
                IntStream.of(result.bestSubset)
                        .mapToObj(k -> simplify(metaFeaturesList.attribute(k).name()))
                        .forEach(writer::println);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow firstRow = sheet.createRow(0);
        for (int i = 0; i < config.getClassifiers().size(); i++) {
            firstRow.createCell(i + 1).setCellValue(config.getClassifiers().get(i).getName());
        }
        for (int i = 0; i < evaluateResults.size(); i++) {
            XSSFRow row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(config.getClassifiers().get(i).getName());
            for (int j = 0; j < evaluateResults.size(); j++) {
                RecommenderSystemEvaluation evaluation = new RecommenderSystemEvaluation(matrices.get(j), metaFeaturesList, config.getAlgorithms(), config.getDatasets());
                try {
                    row.createCell(j + 1).setCellValue(evaluation.evaluateSubset(evaluateResults.get(i).bestSubset));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        sheet = workbook.createSheet();
        firstRow = sheet.createRow(0);
        for (int i = 0; i < config.getClassifiers().size(); i++) {
            firstRow.createCell(i + 1).setCellValue(config.getClassifiers().get(i).getName());
        }
        for (int i = 0; i < metaFeaturesList.numAttributes(); i++) {
            XSSFRow row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(simplify(metaFeaturesList.attribute(i).name()));
            for (int j = 0; j < evaluateResults.size(); j++) {
                row.createCell(j + 1).setCellValue(evaluateResults.get(j).coef[i] / 100);
            }
        }
        try (FileOutputStream output = new FileOutputStream(new File(root, filename))){
            workbook.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String simplify(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static void printFirstResults(String filename) throws Exception {
        EvaluationConfig config = new EvaluationConfig("evaluationConfig.json");
        Instances metaFeaturesList = MetaFeatureConverter.createInstances(config, RecommenderSystemBuilder.META_FEATURES_DIRECTORY, "metaFeatures");
        List<double[][]> matrices = config.getClassifiers().stream()
                .map(config::createEarrMatrix)
                .collect(Collectors.toList());
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow firstRow = sheet.createRow(0);
        for (int i = 0; i < config.getClassifiers().size(); i++) {
            firstRow.createCell(i + 1).setCellValue(config.getClassifiers().get(i).getName());
        }
        for (int i = 0; i < subsets.size(); i++) {
            Pair<String, int[]> subset = subsets.get(i);
            XSSFRow row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(subset.first);
            for (int j = 0; j < matrices.size(); j++) {
                RecommenderSystemEvaluation evaluation = new RecommenderSystemEvaluation(matrices.get(j), metaFeaturesList, config.getAlgorithms(), config.getDatasets());
                row.createCell(j + 1).setCellValue(evaluation.evaluateSubset(subset.second));
            }
        }
        try (FileOutputStream out = new FileOutputStream(filename)) {
            workbook.write(out);
        }

    }

    public static void printDataSetsTable(String filename) throws Exception {
        BuildConfig config = new BuildConfig("config.json");
        System.out.println("dataset number: " + config.getDatasets().size());
        Collections.sort(config.getDatasets());
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        sheet.createRow(0);

        for (int i = 0; i < config.getDatasets().size(); i++) {
            String datasetName = config.getDatasets().get(i);
            String path = config.createPath(datasetName);
            XSSFRow tableRow = sheet.createRow(i + 1);
            try {
                Instances instances = InstancesUtils.createInstances(path);
                tableRow.createCell(0).setCellValue(datasetName);
                tableRow.createCell(1).setCellValue(instances.numInstances());
                tableRow.createCell(2).setCellValue(instances.numAttributes() - 1);
                tableRow.createCell(3).setCellValue(instances.numClasses());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try (FileOutputStream out = new FileOutputStream(filename)) {
            workbook.write(out);
        }
    }

    public static void printAlgorithms(String filename) {
        BuildConfig config = new BuildConfig("config.json");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        sheet.createRow(0);

        for (int i = 0; i < config.getAlgorithms().size(); i++) {
            FSSAlgorithm algorithm = config.getAlgorithms().get(i);
            XSSFRow tableRow = sheet.createRow(i + 1);
            tableRow.createCell(0).setCellValue(i + 1);
            tableRow.createCell(1).setCellValue(algorithm.getName());
            tableRow.createCell(2).setCellValue(algorithm.getAlgorithm().getClass().getSimpleName());
            tableRow.createCell(3).setCellValue(algorithm.getEvaluator().getClass().getSimpleName());
        }

        try (FileOutputStream out = new FileOutputStream(filename)) {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
