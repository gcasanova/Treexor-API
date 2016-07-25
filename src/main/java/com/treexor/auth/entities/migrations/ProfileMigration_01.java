package com.treexor.auth.entities.migrations;

import java.time.Clock;
import java.time.Instant;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.treexor.auth.entities.Profile;
import com.treexor.common.versioning.EntityMigration;

/**
 * Migration for entity version 2: Adds createdAt / updatedAt fields
 * 
 * @author gcasanova
 *
 */
@Component
public class ProfileMigration_01 implements EntityMigration<Profile> {

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public Class<Profile> getMigrationClass() {
        return Profile.class;
    }

    @Override
    public JSONObject upgrade(JSONObject json) throws JSONException {
        long now = Instant.now(Clock.systemUTC()).toEpochMilli();
        json.put("createdAt", now);
        json.put("updatedAt", now);

        json.put("version", getVersion());
        return json;
    }
}
