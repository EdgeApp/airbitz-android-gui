package com.airbitz.shared.utils;

import org.json.JSONObject;

public class JSONUtil {

    /**
     * @param json the JSON object.
     * @param key the key.
     * @return the boolean value of <code>key</code>, false if json is null or if the key
     *         does not exist.
     */
    public static boolean getBoolean(JSONObject json, String key) {
        return (json != null && json.optBoolean(key));
    }

    /**
     * @param json the JSON object.
     * @param key the key.
     * @return the long value of <code>key</code>, -1 if json is null or if the key
     *         does not exist.
     */
    public static long getLong(JSONObject json, String key) {
        return (json == null ? -1 : json.optLong(key, -1));
    }

    /**
     * @param json the JSON object.
     * @param key the key.
     * @return th String value of <code>key</code>, null otherwise.
     */
    public static String getString(JSONObject json, String key) {
        return ((json == null || json.isNull(key)) ? null : json.optString(key));
    }

}
