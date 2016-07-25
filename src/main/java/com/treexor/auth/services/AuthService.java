package com.treexor.auth.services;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.validation.annotation.Validated;

import com.treexor.auth.entities.Profile;
import com.treexor.auth.exceptions.LoginException;
import com.treexor.auth.exceptions.RegistrationException;

@Validated
public interface AuthService {

    @Valid
    Profile register(@NotNull(message = "{validate.registrationService.register.country}") String name, @NotNull(message = "{validate.registrationService.register.email}") String email,
            @NotNull(message = "{validate.registrationService.register.password}") String password) throws RegistrationException;

    JSONObject login(@NotNull(message = "{validate.registrationService.login.email}") String email, @NotNull(message = "{validate.registrationService.login.password}") String password) throws LoginException, JSONException;

    void logout(@Min(value = 1, message = "{validate.registrationService.logout.id}") long id);
}
