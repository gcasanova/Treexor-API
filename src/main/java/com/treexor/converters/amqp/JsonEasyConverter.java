package com.treexor.converters.amqp;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

public class JsonEasyConverter implements MessageConverter {
    private static final Logger log = LoggerFactory.getLogger(JsonEasyConverter.class);

    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        return new Message(object.toString().getBytes(), messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        try {
            return new JSONObject(new String(message.getBody(), "UTF-8"));
        } catch (JSONException | UnsupportedEncodingException e) {
            log.error("AMQP message conversion failed: %s", e.getMessage());
            return new JSONObject();
        }
    }
}
