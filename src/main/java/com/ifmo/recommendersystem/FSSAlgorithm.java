package com.ifmo.recommendersystem;

import org.json.JSONObject;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

public class FSSAlgorithm implements JSONConverted {

    public static class Result {
        public final Instances instances;
        public final long runtime;

        public Result(Instances instances, long runtime) {
            this.instances = instances;
            this.runtime = runtime;
        }
    }

    public final ASSearch algorithm;
    public final String[] algorithmOptions;
    public final ASEvaluation evaluator;
    public final String[] evaluationOptions;

    private FSSAlgorithm(ASSearch algorithm, String[] algorithmOptions,
                         ASEvaluation evaluator, String[] evaluationOptions) {
        this.algorithm = algorithm;
        this.algorithmOptions = algorithmOptions;
        this.evaluator = evaluator;
        this.evaluationOptions = evaluationOptions;
    }

    public Result subsetSelection(Instances instances) {
        try {
            ASSearch search = ASSearch.makeCopies(algorithm, 1)[0];
            ASEvaluation localEvaluator = ASEvaluation.makeCopies(evaluator, 1)[0];
            AttributeSelection filter = new AttributeSelection();
            filter.setSearch(search);
            filter.setEvaluator(localEvaluator);
            filter.setInputFormat(instances);
            long start = System.currentTimeMillis();
            Instances resultInstances = Filter.useFilter(instances, filter);
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

    @Override
    public JSONObject toJSON() {
        JSONObject search = JSONUtils.objectToJSON(algorithm, algorithmOptions);
        JSONObject evaluation = JSONUtils.objectToJSON(evaluator, evaluationOptions);
        return new JSONObject().put(JSONUtils.SEARCH, search).put(JSONUtils.EVALUATION, evaluation);
    }

    public static final AbstractJSONCreator<FSSAlgorithm> JSON_CREATOR = new AbstractJSONCreator<FSSAlgorithm>() {
        @Override
        protected FSSAlgorithm throwableFromJSON(JSONObject jsonObject) throws Exception {
            JSONObject search = jsonObject.getJSONObject(JSONUtils.SEARCH);
            JSONObject evaluation = jsonObject.getJSONObject(JSONUtils.EVALUATION);
            String algorithmClassName = search.getString(JSONUtils.NAME);
            String evaluationClassName = evaluation.getString(JSONUtils.NAME);
            String[] algorithmOptions = JSONUtils.readOptions(search);
            String[] evaluationOptions = JSONUtils.readOptions(evaluation);
            ASSearch algorithm = ASSearch.forName(algorithmClassName, algorithmOptions);
            ASEvaluation evaluator = ASEvaluation.forName(evaluationClassName, evaluationOptions);
            return new FSSAlgorithm(algorithm, algorithmOptions, evaluator, evaluationOptions);
        }
    };
}
