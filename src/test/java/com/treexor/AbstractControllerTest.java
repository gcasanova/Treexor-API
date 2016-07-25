package com.treexor;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.treexor.config.filters.AuthenticationFilter;
import com.treexor.converters.http.JsonArrayEasyConverter;
import com.treexor.converters.http.JsonObjectEasyConverter;
import com.treexor.web.BaseController;

import redis.embedded.RedisServer;

@WebAppConfiguration
public abstract class AbstractControllerTest extends AbstractTest {

    @Autowired
    protected WebApplicationContext context;

    protected MockMvc mvc;
    private static RedisServer redisServer;

    @BeforeClass
    public static void init() throws IOException {
        redisServer = new RedisServer(6379);
        redisServer.start();
    }

    @AfterClass
    public static void destroy() {
        redisServer.stop();
    }

    protected void setUpMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    protected void setUpMvc(BaseController controller, AuthenticationFilter filter) {
        mvc = MockMvcBuilders.standaloneSetup(controller).addFilter(filter, "*/logout", "*/events/save", "*/events/query").setMessageConverters(new JsonObjectEasyConverter(), new JsonArrayEasyConverter()).build();
    }
}
