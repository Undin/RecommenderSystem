package com.ifmo.recommendersystem;

import com.ifmo.recommendersystem.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.ifmo.recommendersystem.utils.JSONUtils.*;

/**
 * Created by warrior on 11.04.15.
 */
public class Check {
    public static void main(String[] args) throws Exception {
        check("config.json");
    }

    private static void check(String configFilename) throws Exception {
        JSONObject jsonObject = JSONUtils.readJSONObject(configFilename);
        ClassifierWrapper classifier = ClassifierWrapper.JSON_CREATOR.fromJSON(jsonObject.getJSONObject(CLASSIFIER));
        List<FSSAlgorithm> algorithms = jsonArrayToObjectList(jsonObject.getJSONArray(ALGORITHMS), FSSAlgorithm.JSON_CREATOR);
        JSONObject datasetsObject = jsonObject.getJSONObject(DATA_SETS);

        List<String> dataSets = jsonArrayToStringList(datasetsObject.getJSONArray(LIST));
        String datasetDir = datasetsObject.getString(DIRECTORY);

        String dir = Utils.createPath(RecommenderSystemBuilder.PERFORMANCE_DIRECTORY, classifier.getName());
        List<String> unreadyDatasets = new ArrayList<>();
        for (String dataset : dataSets) {
            boolean ready = true;
            for (FSSAlgorithm algorithm : algorithms) {
                File d = new File(dir, Utils.createPath(dataset, algorithm.getName()));
                for (int i = 0; i < 50 && ready; i++) {
                    String name = Utils.createName(classifier.getName(), dataset, algorithm.getName(), String.valueOf(i)) + ".json";
                    File res = new File(d, name);
                    if (!res.exists()) {
                        ready = false;
                    }
                }
                if (!ready) {
                    break;
                }
            }
            if (!ready) {
                unreadyDatasets.add(dataset);
            }
        }
        System.out.println(String.join(",\n", unreadyDatasets));
    }

    private static List<String> createInstances(JSONObject jsonObject) throws Exception {
        String directory = jsonObject.getString(JSONUtils.DIRECTORY);
        JSONArray jsonArray = jsonObject.getJSONArray(JSONUtils.LIST);
        List<String> dataSets = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            dataSets.add(directory + File.separator + jsonArray.getString(i));
        }
        return dataSets;
    }
}
