package com.treexor.events.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.treexor.common.locker.LockManager;
import com.treexor.common.versioning.Versioner;
import com.treexor.common.versioning.elastic.ElasticVersionableEntityUpdater;
import com.treexor.events.entities.Event;

@Repository
public class EventsRepository extends ElasticVersionableEntityUpdater<Event> {

    private IEventsRepository repository;

    @Autowired
    public EventsRepository(IEventsRepository repository, LockManager lock, Versioner versioner) {
        super(lock, versioner, Event.class);
        this.repository = repository;
    }

    public List<Event> findByCreatedAtBetween(long min, long max, Pageable pageable) {
        List<Event> events = repository.findByCreatedAtBetween(min, max, pageable);
        return upgrade(events);
    }

    public List<Event> findByUsernameAndCreatedAtBetween(String username, long min, long max, Pageable pageable) {
        List<Event> events = repository.findByUsernameAndCreatedAtBetween(username, min, max, pageable);
        return upgrade(events);
    }

    public List<Event> findByTypeAndCreatedAtBetween(String type, long min, long max, Pageable pageable) {
        List<Event> events = repository.findByTypeAndCreatedAtBetween(type, min, max, pageable);
        return upgrade(events);
    }

    public List<Event> findByUsernameAndTypeAndCreatedAtBetween(String username, String type, long min, long max, Pageable pageable) {
        List<Event> events = repository.findByUsernameAndTypeAndCreatedAtBetween(username, type, min, max, pageable);
        return upgrade(events);
    }

    public void save(Event entity) {
        repository.save(entity);
    }

    @Override
    public void update(Event entity) {
        repository.save(entity);
    }

    @Override
    public Event findOne(long id) {
        return repository.findOne(id);
    }
}
