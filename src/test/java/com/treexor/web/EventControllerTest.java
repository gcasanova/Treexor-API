package com.treexor.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.treexor.AbstractControllerTest;
import com.treexor.config.filters.AuthenticationFilter;

public class EventControllerTest extends AbstractControllerTest {

    @Mock
    private RabbitTemplate mockRabbitTemplate;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StringRedisTemplate mockRedisTemplate;

    @InjectMocks
    private EventsController controller;

    @Override
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(controller, "lastVersion", 1);
        ReflectionTestUtils.setField(controller, "saveQueue", "register.queue");
        ReflectionTestUtils.setField(controller, "queryQueue", "logout.queue");
        setUpMvc(controller, new AuthenticationFilter(mockRedisTemplate));
    }

    @Override
    public void tearDown() {
        // TODO Auto-generated method stub
    }

    /**
     * QUERY
     */
    @Test
    public void given_A_Null_Response_From_The_MicroService_When_Querying_Then_Return_Internal_Server_Error_Http_Status() throws Exception {
        when(mockRabbitTemplate.convertSendAndReceive(anyString(), any(JSONObject.class))).thenReturn(null);

        String uri = "/api/v1/events/query";

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(new JSONObject().toString())).andReturn();

        int contentLength = result.getResponse().getContentLength();
        int status = result.getResponse().getStatus();

        assertEquals("Failure - we did not expect a response body", 0, contentLength);
        assertEquals("Failure - we expected response HTTP status to be " + HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), status);

        verify(mockRabbitTemplate, times(1)).convertSendAndReceive(anyString(), any(JSONObject.class));
    }

    @Test
    public void given_A_Response_With_Only_Status_From_The_MicroService_When_Registering_Then_Return_The_Http_Status_And_Empty_Body() throws Exception {
        int receivedStatus = HttpStatus.CONFLICT.value();
        when(mockRabbitTemplate.convertSendAndReceive(anyString(), any(JSONObject.class))).thenReturn(new JSONObject().put("status", receivedStatus));

        String uri = "/api/v1/events/query";

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(new JSONObject().toString())).andReturn();

        int contentLength = result.getResponse().getContentLength();
        int status = result.getResponse().getStatus();

        assertEquals("Failure - we did not expect a response body", 0, contentLength);
        assertEquals("Failure - we expected response HTTP status to be " + receivedStatus, receivedStatus, status);
        
        verify(mockRabbitTemplate, times(1)).convertSendAndReceive(anyString(), any(JSONObject.class));
    }

    @Test
    public void given_A_Full_Response_From_The_MicroService_When_Registering_Then_Return_The_Http_Status_And_The_Content() throws Exception {
        int receivedStatus = HttpStatus.CREATED.value();
        when(mockRabbitTemplate.convertSendAndReceive(anyString(), any(JSONObject.class))).thenReturn(new JSONObject().put("status", receivedStatus).put("body", new JSONArray()));

        String uri = "/api/v1/events/query";

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(new JSONObject().toString())).andReturn();

        int contentLength = result.getResponse().getContentLength();
        int status = result.getResponse().getStatus();

        assertTrue("Failure - we expected a response body", contentLength > 0);
        assertEquals("Failure - we expected response HTTP status to be " + receivedStatus, receivedStatus, status);
        
        verify(mockRabbitTemplate, times(1)).convertSendAndReceive(anyString(), any(JSONObject.class));
    }
}
