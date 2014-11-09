package com.ifmo.recommendersystem;

import org.json.JSONObject;

public abstract class AbstractJSONCreator<T> implements JSONConverted.JSONCreator<T>  {

    @Override
    public T fromJSON(JSONObject jsonObject) {
        try {
            return throwableFromJSON(jsonObject);
        } catch (Exception e) {
            throw new JSONConverted.IllegalJSONFormatException(e);
        }
    }

    protected abstract T throwableFromJSON(JSONObject jsonObject) throws Exception;
}
