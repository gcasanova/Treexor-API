package com.treexor.auth.versioning;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.treexor.auth.entities.Profile;
import com.treexor.common.locker.LockManager;
import com.treexor.common.versioning.EntityMigration;
import com.treexor.common.versioning.Versionable;
import com.treexor.common.versioning.Versioner;

@Service
public class AuthVersioner implements Versioner {

    @Value("${entity.profile.version}")
    private Integer profileEntityVersion;

    @Autowired(required = false)
    private List<EntityMigration<? extends Versionable>> migrationsList;

    private Map<Class<?>, List<EntityMigration<? extends Versionable>>> migrations;

    @Autowired
    public AuthVersioner(LockManager lock) {
        migrations = Maps.newHashMap();

        // migrations could be null since we might have no entity migrations just yet
        if (migrationsList != null) {
            List<EntityMigration<? extends Versionable>> migrationListHolder;
            for (EntityMigration<? extends Versionable> migration : migrationsList) {
                if (!migrations.containsKey(migration.getMigrationClass()))
                    migrations.put(migration.getMigrationClass(), Lists.newArrayList());

                migrationListHolder = migrations.get(migration.getMigrationClass());
                migrationListHolder.add(migration);
                migrations.put(migration.getMigrationClass(), migrationListHolder);
            }

            // sort migration lists by version
            for (List<EntityMigration<? extends Versionable>> unsortedMigrationsList : migrations.values()) {
                unsortedMigrationsList.sort(Comparator.comparing(EntityMigration::getVersion));
            }
        }
    }

    @Override
    public int getLastVersion(Class<?> clazz) {
        if (clazz.equals(Profile.class))
            return profileEntityVersion;

        return 0;
    }

    @Override
    public boolean update(JSONObject json, Class<?> clazz) throws JSONException {
        int lastVersion = getLastVersion(clazz);
        int currentVersion = json.optInt("version");

        if (lastVersion > currentVersion) {
            // update resource
            List<EntityMigration<? extends Versionable>> migrationsList = migrations.get(clazz);
            for (int i = currentVersion - 1; i < lastVersion - 1; i++) {
                json = migrationsList.get(i).upgrade(json);
            }
            return true;
        }
        return false;
    }
}
