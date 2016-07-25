package com.treexor.common.versioning;

import org.json.JSONException;
import org.json.JSONObject;

public interface EntityMigration<T extends Versionable> {

    int getVersion();

    Class<T> getMigrationClass();

    JSONObject upgrade(JSONObject json) throws JSONException;
}
