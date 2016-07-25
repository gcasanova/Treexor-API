package com.treexor.auth.entities;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;

import javax.validation.constraints.NotNull;

import com.treexor.auth.utils.EncryptionMD5;
import com.treexor.common.entities.TimedEntity;
import com.treexor.common.versioning.Versionable;

public class Profile implements Serializable, Versionable, TimedEntity {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private String email;
    private String password;

    private int version;
    private long createdAt;
    private long updatedAt;

    public Profile() {
        // TODO Auto-generated constructor stub
    }

    public Profile(long id, String name, String email, String password, int version) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = EncryptionMD5.encrypt(password, id);

        this.version = version;

        long now = Instant.now(Clock.systemUTC()).toEpochMilli();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @NotNull
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public long getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }
}
