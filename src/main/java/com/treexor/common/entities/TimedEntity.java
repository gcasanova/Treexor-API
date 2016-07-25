package com.treexor.common.entities;

public interface TimedEntity {

    long getCreatedAt();

    void setCreatedAt(long createdAt);

    long getUpdatedAt();

    void setUpdatedAt(long updatedAt);
}
