package com.atmira.asteroids.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AsteroidsControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(AsteroidsControllerTest.class);
    
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    ObjectMapper objectmapper;
	
    @Test(expected = Exception.class)
    public void testGetSpainKO() throws Exception {
        String response = mockMvc.perform(get("/asteroids")
        		.param("days", "0"))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
 
        logger.info("response: " + response);
    }
    
    @Test
    public void testGetSpainOK1() throws Exception {
        String response = mockMvc.perform(get("/asteroids")
        		.param("days", "1"))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
 
        logger.info("response: " + response);
    }
    
    @Test
    public void testGetSpainOK2() throws Exception {
        String response = mockMvc.perform(get("/asteroids")
        		.param("days", "2"))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
 
        logger.info("response: " + response);
    }
    
}
