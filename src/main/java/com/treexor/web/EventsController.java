package com.treexor.web;

import java.time.Clock;
import java.time.Instant;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v{version}/events")
public class EventsController extends BaseController {

    @Value("${events.version}")
    private Integer lastVersion;

    @Value("${save.queue}")
    private String saveQueue;

    @Value("${query.queue}")
    private String queryQueue;

    private RabbitTemplate template;

    @Autowired
    public EventsController(RabbitTemplate template) {
        this.template = template;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<?> save(@RequestHeader("Id") Long accountId, @PathVariable("version") int version, @RequestBody JSONObject json) {
        int finalVersion = version > lastVersion ? lastVersion : version;
        String versionedQueue = saveQueue + "." + finalVersion;

        // insert createdAt and id fields
        json.put("id", accountId);
        json.put("createdAt", Instant.now(Clock.systemUTC()).toEpochMilli());
        template.convertAndSend(versionedQueue, json);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public ResponseEntity<?> query(@PathVariable("version") int version, @RequestBody JSONObject json) {
        int finalVersion = version > lastVersion ? lastVersion : version;
        return handleRequest(json, queryQueue, finalVersion);
    }

    private ResponseEntity<?> handleRequest(JSONObject json, String queue, int version) {
        String versionedQueue = queue + "." + version;
        JSONObject jResponse = (JSONObject) template.convertSendAndReceive(versionedQueue, json);

        ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        if (jResponse != null) {
            Integer responseStatus = jResponse.optInt("status");
            JSONArray responseBody = jResponse.optJSONArray("body");
            if (responseStatus > 0 && responseBody != null) {
                responseEntity = new ResponseEntity<>(responseBody, HttpStatus.valueOf(responseStatus));
            } else if (responseStatus > 0) {
                responseEntity = new ResponseEntity<>(HttpStatus.valueOf(responseStatus));
            }
        }
        return responseEntity;
    }
}
