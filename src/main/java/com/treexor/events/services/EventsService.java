package com.treexor.events.services;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.json.JSONArray;
import org.springframework.validation.annotation.Validated;

import com.treexor.events.enums.EventType;

@Validated
public interface EventsService {

    @Valid
    void save(@Min(value = 1, message = "{validate.eventsService.save.id}") long id, @NotNull(message = "{validate.eventsService.save.name}") String name, @NotNull(message = "{validate.eventsService.save.type}") EventType type,
            @Min(value = 1, message = "{validate.eventsService.save.createdAt}") long createdAt);

    JSONArray query(String name, EventType type, Long before, Long after, Integer page);
}
