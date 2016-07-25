package com.treexor.events.consumers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import com.treexor.events.AbstractTest;
import com.treexor.events.enums.EventType;
import com.treexor.events.services.EventsService;

public class RabbitConsumerTest extends AbstractTest {

    @Mock
    private EventsService mockService;

    @InjectMocks
    private RabbitConsumer consumer;

    @Override
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    public void tearDown() {
        // TODO Auto-generated method stub
    }

    /**
     * SAVE
     */
    @Test
    public void given_Missing_Parameters_When_Saving_Then_Do_Not_Invoke_Service() {
        consumer.handleSave(new JSONObject());
        verify(mockService, times(0)).save(anyLong(), anyString(), any(EventType.class), anyLong());

        consumer.handleSave(new JSONObject().put("id", Math.abs(new Random().nextLong())).put("name", "random_name").put("type", EventType.PURCHASE.getValue()));
        verify(mockService, times(0)).save(anyLong(), anyString(), any(EventType.class), anyLong());
    }

    @Test
    public void given_All_Parameters_When_Saving_Then_Invoke_Service() throws Exception {
        consumer.handleSave(new JSONObject().put("id", Math.abs(new Random().nextLong())).put("name", "random_name").put("type", EventType.PURCHASE.getValue()).put("createdAt", Math.abs(new Random().nextLong())));
        verify(mockService, times(1)).save(anyLong(), anyString(), any(EventType.class), anyLong());
    }

    /**
     * QUERY
     */
    @Test
    public void given_Missing_Parameters_When_Querying_Then_Invoke_Service_Anyways() {
        when(mockService.query(anyString(), any(EventType.class), anyLong(), anyLong(), anyInt())).thenReturn(new JSONArray());

        JSONObject jResponse = consumer.handleQuery(new JSONObject());
        assertNotNull("Failure - we were expecting a JSONObject response", jResponse);
        assertEquals("Failure - we were expecting an HTTP status " + HttpStatus.OK.value(), HttpStatus.OK.value(), jResponse.optInt("status"));

        verify(mockService, times(1)).query(anyString(), any(EventType.class), anyLong(), anyLong(), anyInt());
    }
}
