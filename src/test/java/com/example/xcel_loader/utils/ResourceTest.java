package com.example.xcel_loader.utils;

import com.example.xcel_loader.model.User;
import com.example.xcel_loader.store.Store;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@ActiveProfiles(value = {"integration-test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.main.allow-bean-definition-overriding=true")
public class ResourceTest {

    public ObjectMapper mapper;

    public User testUser;
    @Autowired
    private ApplicationContext context;

    @LocalServerPort
    private int port;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        RestAssured.port = Integer.valueOf(port);
        testUser = TestHelper.generateFakeUser();
    }

    @After
    public void afterTest() {
        clearStores();
    }

    @Test
    public void can_initialise() {

    }

    private void clearStores() {
        context.getBean(Store.class).clear();


    }

    public static <T> T fromJSON(ObjectMapper mapper, final TypeReference<T> type, final String jsonPacket) {
        T data = null;
        try {
            data = mapper.readValue(jsonPacket, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
