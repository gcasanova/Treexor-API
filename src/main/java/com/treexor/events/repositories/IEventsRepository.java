package com.treexor.events.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.treexor.events.entities.Event;

@Repository
public interface IEventsRepository extends CrudRepository<Event, Long> {

    List<Event> findByCreatedAtBetween(long min, long max, Pageable pageable);

    List<Event> findByUsernameAndCreatedAtBetween(String username, long min, long max, Pageable pageable);

    List<Event> findByTypeAndCreatedAtBetween(String type, long min, long max, Pageable pageable);

    List<Event> findByUsernameAndTypeAndCreatedAtBetween(String username, String type, long min, long max, Pageable pageable);
}
