package com.treexor.events.entities;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.treexor.common.versioning.Versionable;

@Document(indexName = "treexor", type = "event")
public class Event implements Versionable, Serializable {
    private static final long serialVersionUID = -6466059071602047467L;

    @Id
    private Long id;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String username;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String)
    private String type;

    private Long userId;
    private Long createdAt;

    private int version;

    public Event() {
        // TODO Auto-generated constructor stub
    }

    public Event(Long id, Long userId, String username, String type, Long createdAt, int version) {
        this.id = id;
        this.type = type;
        this.userId = userId;
        this.version = version;
        this.username = username;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
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