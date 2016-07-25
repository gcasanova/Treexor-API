package com.treexor.auth.consumers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.treexor.auth.entities.Profile;
import com.treexor.auth.exceptions.LoginException;
import com.treexor.auth.exceptions.RegistrationException;
import com.treexor.auth.services.AuthService;
import com.treexor.auth.services.AuthServiceImpl;

@Component
public class RabbitConsumer {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private AuthService service;

    @Autowired
    public RabbitConsumer(AuthService service) {
        this.service = service;
    }

    @RabbitListener(queues = "${login.queue}")
    public JSONObject handleLogin(JSONObject jPackage) {
        JSONObject response = new JSONObject();

        try {
            String email = jPackage.optString("email");
            String password = jPackage.optString("password");

            if (!Strings.isNullOrEmpty(email) && !Strings.isNullOrEmpty(password) && areValidParameters(null, email, password)) {

                JSONObject jResponse = service.login(email, password);

                response.put("body", jResponse);
                response.put("status", HttpStatus.OK.value());
            } else {
                response.put("status", HttpStatus.BAD_REQUEST.value());
            }
        } catch (LoginException e) {
            response.put("status", e.getHttpStatus().value());
        } catch (JSONException e) {
            log.error("Profile deserialization failed: " + e.getMessage());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    @RabbitListener(queues = "${logout.queue}")
    public void handleLogout(JSONObject jPackage) {
        Long id = jPackage.optLong("id");

        if (id != null && id > 0) {
            service.logout(id);
        }
    }

    @RabbitListener(queues = "${register.queue}")
    public JSONObject handleRegister(JSONObject jPackage) {
        JSONObject response = new JSONObject();

        try {
            String name = jPackage.optString("name");
            String email = jPackage.optString("email");
            String password = jPackage.optString("password");

            if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(email) && !Strings.isNullOrEmpty(password) && areValidParameters(name, email, password)) {

                Profile profile = service.register(name, email, password);

                response.put("body", new JSONObject().put("id", profile.getId()));
                response.put("status", HttpStatus.CREATED.value());
            } else {
                response.put("status", HttpStatus.BAD_REQUEST.value());
            }
        } catch (RegistrationException e) {
            response.put("status", e.getHttpStatus().value());
        }
        return response;
    }

    private boolean areValidParameters(String name, String email, String password) {
        if (name != null && name.length() > 70)
            return false;

        return isValidEmail(email) && isValidPassword(password);
    }

    protected boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]{8,30}$");
    }

    protected boolean isValidEmail(String email) {
        Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(email);
        return m.find();
    }
}
