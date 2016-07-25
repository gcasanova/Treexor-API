package com.treexor.web;

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
@RequestMapping("/api/v{version}/auth")
public class AuthController extends BaseController {

    @Value("${auth.version}")
    private Integer lastVersion;

    @Value("${register.queue}")
    private String registerQueue;

    @Value("${logout.queue}")
    private String logoutQueue;

    @Value("${login.queue}")
    private String loginQueue;

    private RabbitTemplate template;

    @Autowired
    public AuthController(RabbitTemplate template) {
        this.template = template;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@PathVariable("version") int version, @RequestBody JSONObject json) {
        int finalVersion = version > lastVersion ? lastVersion : version;
        return handleRequest(json, registerQueue, finalVersion);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@PathVariable("version") int version, @RequestBody JSONObject json) {
        int finalVersion = version > lastVersion ? lastVersion : version;
        return handleRequest(json, loginQueue, finalVersion);
    }

    @RequestMapping(value = "/logout", method = { RequestMethod.POST, RequestMethod.GET })
    public ResponseEntity<?> logout(@RequestHeader("Id") Long accountId, @PathVariable("version") int version) {
        int finalVersion = version > lastVersion ? lastVersion : version;
        String versionedQueue = logoutQueue + "." + finalVersion;
        template.convertAndSend(versionedQueue, new JSONObject().put("id", accountId));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<?> handleRequest(JSONObject json, String queue, int version) {
        String versionedQueue = queue + "." + version;
        JSONObject jResponse = (JSONObject) template.convertSendAndReceive(versionedQueue, json);

        ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        if (jResponse != null) {
            Integer responseStatus = jResponse.optInt("status");
            JSONObject responseBody = jResponse.optJSONObject("body");
            if (responseStatus > 0 && responseBody != null) {
                responseEntity = new ResponseEntity<>(responseBody, HttpStatus.valueOf(responseStatus));
            } else if (responseStatus > 0) {
                responseEntity = new ResponseEntity<>(HttpStatus.valueOf(responseStatus));
            }
        }
        return responseEntity;
    }
}
