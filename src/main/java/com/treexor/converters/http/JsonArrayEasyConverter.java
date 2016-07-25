package com.treexor.converters.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
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
public class JsonArrayEasyConverter implements HttpMessageConverter<JSONArray> {

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        if (clazz.equals(JSONArray.class)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        if (clazz.equals(JSONArray.class)) {
            return true;
        }
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(new MediaType("application", "json"), new MediaType("text", "json"));
    }

    @Override
    public JSONArray read(Class<? extends JSONArray> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

        try {
            return new JSONArray(new String(StreamUtils.copyToByteArray(inputMessage.getBody())));
        } catch (JSONException e1) {
            return new JSONArray();
        }
    }

    @Override
    public void write(JSONArray t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

        HttpHeaders headers = outputMessage.getHeaders();
        headers.add("Content-Length", String.valueOf(t.toString().getBytes().length));
        headers.add("Content-Type", "application/json; charset=UTF-8");

        outputMessage.getBody().write(t.toString().getBytes());
    }
}
