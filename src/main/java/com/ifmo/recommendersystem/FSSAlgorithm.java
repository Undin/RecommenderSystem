package com.ifmo.recommendersystem;

import org.json.JSONObject;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.core.Instances;

import static com.ifmo.recommendersystem.JSONUtils.*;

public class FSSAlgorithm implements JSONConverted {

    public static class Result {
        public final Instances instances;
        public final long runtime;

        public Result(Instances instances, long runtime) {
            this.instances = instances;
            this.runtime = runtime;
        }
    }

    private final String name;
    private final ASSearch algorithm;
    private final String[] algorithmOptions;
    private final ASEvaluation evaluator;
    private final String[] evaluationOptions;

    private FSSAlgorithm(String name, ASSearch algorithm, String[] algorithmOptions,
                         ASEvaluation evaluator, String[] evaluationOptions) {
        this.name = name;
        this.algorithm = algorithm;
        this.algorithmOptions = algorithmOptions;
        this.evaluator = evaluator;
        this.evaluationOptions = evaluationOptions;
    }

    public Result subsetSelection(Instances instances) {
        try {
            ASSearch search = ASSearch.makeCopies(algorithm, 1)[0];
            ASEvaluation evaluation = ASEvaluation.makeCopies(evaluator, 1)[0];
            long start = System.currentTimeMillis();
            Instances resultInstances = InstancesUtils.selectAttributes(instances, search, evaluation);
            long end = System.currentTimeMillis();
            return new Result(resultInstances, end - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ASSearch getAlgorithm() {
        return algorithm;
    }

    public ASEvaluation getEvaluator() {
        return evaluator;
    }

    public String[] getAlgorithmOptions() {
        return algorithmOptions;
    }

    public String[] getEvaluationOptions() {
        return evaluationOptions;
    }

    public String getName() {
        return name;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject search = objectToJSON(algorithm, algorithmOptions);
        JSONObject evaluation = objectToJSON(evaluator, evaluationOptions);
        return new JSONObject().put(ALGORITHM_NAME, name).put(SEARCH, search).put(EVALUATION, evaluation);
    }

    public static final AbstractJSONCreator<FSSAlgorithm> JSON_CREATOR = new AbstractJSONCreator<FSSAlgorithm>() {
        @Override
        protected FSSAlgorithm throwableFromJSON(JSONObject jsonObject) throws Exception {
            String name = jsonObject.getString(ALGORITHM_NAME);
            JSONObject search = jsonObject.getJSONObject(SEARCH);
            JSONObject evaluation = jsonObject.getJSONObject(EVALUATION);
            String algorithmClassName = search.getString(CLASS_NAME);
            String evaluationClassName = evaluation.getString(CLASS_NAME);
            String[] algorithmOptions = readOptions(search);
            String[] evaluationOptions = readOptions(evaluation);
            ASSearch algorithm = ASSearch.forName(algorithmClassName, algorithmOptions);
            ASEvaluation evaluator = ASEvaluation.forName(evaluationClassName, evaluationOptions);
            return new FSSAlgorithm(name, algorithm, algorithmOptions, evaluator, evaluationOptions);
        }
    };
}
