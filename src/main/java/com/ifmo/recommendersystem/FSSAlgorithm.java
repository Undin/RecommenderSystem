package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.tasks.time.TimeManager;
import com.ifmo.recommendersystem.tasks.time.Timestamp;
import com.ifmo.recommendersystem.utils.InstancesUtils;
import org.json.JSONObject;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.core.Instances;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.ifmo.recommendersystem.utils.JSONUtils.*;

public class FSSAlgorithm implements JSONConverted {

    public static class Result {
        public final String algorithmName;
        public final Instances instances;
        public final long runtime;

        public Result(String algorithmName, Instances instances, long runtime) {
            this.algorithmName = algorithmName;
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
            Timestamp start = TimeManager.getInstance().getTimestamp();
            Instances resultInstances = InstancesUtils.selectAttributes(instances, search, evaluation);
            Timestamp end = TimeManager.getInstance().getTimestamp();
            return new Result(name, resultInstances, TimeManager.between(start, end));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FSSAlgorithm)) return false;

        FSSAlgorithm algorithm = (FSSAlgorithm) o;

        if (!name.equals(algorithm.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private static final Map<String, FSSAlgorithm> ALGORITHM_MAP = new HashMap<>();

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
            ASSearch algorithm = ASSearch.forName(algorithmClassName, Arrays.copyOf(algorithmOptions, algorithmOptions.length));
            ASEvaluation evaluator = ASEvaluation.forName(evaluationClassName, Arrays.copyOf(evaluationOptions, evaluationOptions.length));
            return new FSSAlgorithm(name, algorithm, algorithmOptions, evaluator, evaluationOptions);
        }
    };

    public static void addAlgorithm(JSONObject jsonObject) {
        FSSAlgorithm algorithm = JSON_CREATOR.fromJSON(jsonObject);
        ALGORITHM_MAP.put(algorithm.getName(), algorithm);
    }

    public static FSSAlgorithm getAlgorithm(String name) {
        return ALGORITHM_MAP.get(name);
    }
}
