package com.treexor.converters.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class JsonObjectEasyConverter implements HttpMessageConverter<JSONObject> {

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        if (clazz.equals(JSONObject.class)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        if (clazz.equals(JSONObject.class)) {
            return true;
        }
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(new MediaType("application", "json"), new MediaType("text", "json"));
    }

    @Override
    public JSONObject read(Class<? extends JSONObject> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

        try {
            return new JSONObject(new String(StreamUtils.copyToByteArray(inputMessage.getBody())));
        } catch (JSONException e1) {
            return new JSONObject();
        }
    }

    @Override
    public void write(JSONObject t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

        HttpHeaders headers = outputMessage.getHeaders();
        headers.add("Content-Length", String.valueOf(t.toString().getBytes().length));
        headers.add("Content-Type", "application/json; charset=UTF-8");

        outputMessage.getBody().write(t.toString().getBytes());
    }
}
