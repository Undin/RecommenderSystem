package com.ifmo.recommendersystem.config;

import org.json.JSONObject;

import static com.ifmo.recommendersystem.utils.JSONUtils.ALPHA;
import static com.ifmo.recommendersystem.utils.JSONUtils.BETTA;
import static com.ifmo.recommendersystem.utils.JSONUtils.readJSONObject;

/**
 * Created by warrior on 27.04.15.
 */
public class EvaluationConfig extends Config {

    private final double alpha;
    private final double betta;

    public EvaluationConfig(String configFilename) {
        this(readJSONObject(configFilename));
    }

    protected EvaluationConfig(JSONObject jsonObject) {
        super(jsonObject);
        alpha = jsonObject.getDouble(ALPHA);
        betta = jsonObject.getDouble(BETTA);
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBetta() {
        return betta;
    }
}
