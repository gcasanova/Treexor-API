package com.treexor.events.services;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.treexor.common.versioning.Versioner;
import com.treexor.events.entities.Event;
import com.treexor.events.enums.EventType;
import com.treexor.events.repositories.EventsRepository;

@Service
public class EventsServiceImpl implements EventsService {

    @Value("${elastic.page.size}")
    private int size;

    private Versioner versioner;
    private EventsRepository repository;

    @Autowired
    public EventsServiceImpl(Versioner versioner, EventsRepository repository) {
        this.versioner = versioner;
        this.repository = repository;
    }

    @Override
    public void save(long id, String name, EventType type, long createdAt) {
        // TODO - an alternative implementation to create event identifiers
        long eventId = Math.abs(UUID.randomUUID().getLeastSignificantBits());
        repository.save(new Event(eventId, id, name, type.getValue(), createdAt, versioner.getLastVersion(Event.class)));
    }

    @Override
    public JSONArray query(String name, EventType type, Long before, Long after, Integer page) {
        Pageable pageable = new PageRequest(page, size);

        // take events that happened before "now" if no param was provided for this
        if (before == 0 || before == null)
            before = Instant.now(Clock.systemUTC()).toEpochMilli();

        // take events that happened after a month ago (older events expire, ttl set manually in elastic mapping) if no param was provided for this
        if (after == 0 || after == null)
            after = ZonedDateTime.now(ZoneId.of("UTC")).minus(30, ChronoUnit.DAYS).toInstant().toEpochMilli();

        List<Event> events = null;
        if (!Strings.isNullOrEmpty(name) && type != null) {
            events = repository.findByUsernameAndTypeAndCreatedAtBetween(name, type.getValue(), after, before, pageable);
        } else if (!Strings.isNullOrEmpty(name)) {
            events = repository.findByUsernameAndCreatedAtBetween(name, after, before, pageable);
        } else if (type != null) {
            events = repository.findByTypeAndCreatedAtBetween(type.getValue(), after, before, pageable);
        } else {
            events = repository.findByCreatedAtBetween(after, before, pageable);
        }
        return new JSONArray(events);
    }
}
