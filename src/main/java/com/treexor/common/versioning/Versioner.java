package com.treexor.common.versioning;

import org.json.JSONException;
import org.json.JSONObject;

public interface Versioner {
    int getLastVersion(Class<?> clazz);

    boolean update(JSONObject json, Class<?> clazz) throws JSONException;
}
