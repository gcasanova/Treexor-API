package com.treexor.events.consumers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.treexor.events.enums.EventType;
import com.treexor.events.services.EventsService;

@Component
public class RabbitConsumer {

    private EventsService service;

    @Autowired
    public RabbitConsumer(EventsService service) {
        this.service = service;
    }

    @RabbitListener(queues = "${save.queue}")
    public void handleSave(JSONObject jPackage) {
        Long id = jPackage.optLong("id");
        String name = jPackage.optString("name");
        EventType type = EventType.findByValue(jPackage.optString("type"));
        Long createdAt = jPackage.optLong("createdAt");

        if (id != null && id > 0 && createdAt != null && createdAt > 0 && type != null && !Strings.isNullOrEmpty(name)) {
            service.save(id, name, type, createdAt);
        }
    }

    @RabbitListener(queues = "${query.queue}")
    public JSONObject handleQuery(JSONObject jPackage) {
        JSONObject response = new JSONObject();

        Integer page = jPackage.optInt("page");
        Long after = jPackage.optLong("after");
        Long before = jPackage.optLong("before");
        String name = jPackage.optString("name");
        EventType type = EventType.findByValue(jPackage.optString("type"));

        JSONArray jResponse = service.query(name, type, before, after, page);
        response.put("body", jResponse);
        response.put("status", HttpStatus.OK.value());
        return response;
    }
}
