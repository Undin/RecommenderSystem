package com.ifmo.recommendersystem;

import org.json.JSONObject;

public interface JSONConverted {

    public JSONObject toJSON();

    public interface JSONCreator<T> {

        public T fromJSON(JSONObject jsonObject);

    }

    public static class IllegalJSONFormatException extends IllegalArgumentException {
        public IllegalJSONFormatException() {
            super();
        }

        public IllegalJSONFormatException(String s) {
            super(s);
        }

        public IllegalJSONFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        public IllegalJSONFormatException(Throwable cause) {
            super(cause);
        }
    }
}
