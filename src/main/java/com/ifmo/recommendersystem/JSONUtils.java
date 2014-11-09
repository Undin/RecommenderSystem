package com.ifmo.recommendersystem;

import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.matrix.Matrix;

import java.util.*;
import java.util.stream.Collectors;

class JSONUtils {

    public static final String ALPHA = "alpha";
    public static final String BETTA = "betta";
    public static final String CLASSIFIER = "classifier";
    public static final String ALGORITHMS = "algorithms";
    public static final String DATA_SETS = "dataSets";
    public static final String SEARCH = "search";
    public static final String EVALUATION = "evaluation";
    public static final String OPTIONS = "options";
    public static final String NAME = "name";
    public static final String META_FEATURES = "metaFeatures";
    public static final String EARR_MATRIX = "earrMatrix";

    public static String[] readOptions(JSONObject jsonObject) {
        if (jsonObject.has(OPTIONS)) {
            JSONArray optionsArray = jsonObject.getJSONArray(OPTIONS);
            String[] options = new String[optionsArray.length()];
            for (int i = 0; i < optionsArray.length(); i++) {
                options[i] = optionsArray.getString(i);
            }
            return options;
        }
        return null;
    }

    public static JSONObject objectToJSON(Object object, String[] options) {
        return new JSONObject().
                put(NAME, object.getClass().getCanonicalName()).
                put(OPTIONS, options == null ? Collections.emptyList() : Arrays.asList(options));
    }

    public static JSONArray collectionToJSONArray(Collection<? extends JSONConverted> collection) {
        List<JSONObject> objectList = new ArrayList<>(collection.size());
        objectList.addAll(collection.stream().map(JSONConverted::toJSON).collect(Collectors.toList()));
        return new JSONArray(objectList);
    }

    public static <T> List<T> jsonArrayToObjectList(JSONArray jsonArray, JSONConverted.JSONCreator<T> creator) {
        List<T> objects = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            objects.add(creator.fromJSON(jsonArray.getJSONObject(i)));
        }
        return objects;
    }

    public static JSONArray matrixToJSONArray(Matrix matrix) {
        List<JSONArray> rows = new ArrayList<>(matrix.getRowDimension());
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            List<Double> row = new ArrayList<>(matrix.getColumnDimension());
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                row.add(matrix.get(i, j));
            }
            rows.add(new JSONArray(row));
        }
        return new JSONArray(rows);
    }

    public static Matrix jsonArrayToMatrix(JSONArray jsonArray) {
        double[][] matrix = new double[jsonArray.length()][];
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray row = jsonArray.getJSONArray(i);
            matrix[i] = new double[row.length()];
            for (int j = 0; j < row.length(); j++) {
                matrix[i][j] = row.getDouble(j);
            }
        }
        return new Matrix(matrix);
    }
}
