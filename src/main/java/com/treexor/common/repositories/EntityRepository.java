package com.treexor.common.repositories;

public interface EntityRepository<T> {
    void delete(String id);

    T find(String id);

    boolean exists(String id);

    void update(T entity);

    void save(T entity);
}
