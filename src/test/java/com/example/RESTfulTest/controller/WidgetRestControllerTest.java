package com.example.RESTfulTest.controller;

import com.example.RESTfulTest.model.Widget;
import com.example.RESTfulTest.service.WidgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WidgetRestControllerTest {

    @MockBean
    private WidgetService service;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("GET /widgets success")
    void testGetWidgetsSuccess() throws Exception {
        // Setup our mocked service
        Widget widget1 = new Widget(1l, "Widget Name", "Description", 1);
        Widget widget2 = new Widget(2l, "Widget 2 Name", "Description 2", 4);
        doReturn(Lists.newArrayList(widget1, widget2)).when(service).findAll();

        // Execute the GET request
        mockMvc.perform(get("/rest/widgets"))
                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widgets"))

                // Validate the returned fields
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Widget Name")))
                .andExpect(jsonPath("$[0].description", is("Description")))
                .andExpect(jsonPath("$[0].version", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Widget 2 Name")))
                .andExpect(jsonPath("$[1].description", is("Description 2")))
                .andExpect(jsonPath("$[1].version", is(4)));
    }



    @Test
    @DisplayName("GET /rest/widget/1 - Not Found")
    void testGetWidgetByIdNotFound() throws Exception {
        // Setup our mocked service
        doReturn(Optional.empty()).when(service).findById(1l);

        // Execute the GET request
        mockMvc.perform(get("/rest/widget/{id}", 1L))
                // Validate the response code
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /rest/widget")
    void testCreateWidget() throws Exception {
        // Setup our mocked service
        Widget widgetToPost = new Widget("New Widget", "This is my widget");
        Widget widgetToReturn = new Widget(1L, "New Widget", "This is my widget", 1);
        doReturn(widgetToReturn).when(service).save(any());

        // Execute the POST request
        mockMvc.perform(post("/rest/widget")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(widgetToPost)))

                // Validate the response code and content type
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Widget")))
                .andExpect(jsonPath("$.description", is("This is my widget")))
                .andExpect(jsonPath("$.version", is(1)));
    }

    //Metodo PUT
    @Test
    @DisplayName("PUT /rest/widget/1")
    void testModifyWidget() throws Exception {
        //Mock servide
        Widget widgetToPut = new Widget(1L, "Computer", "laptop", 1);
        Widget widgetToUpdate = new Widget(1L, "Computer update", "Gamer laptop", 1);

        doReturn(Optional.of(widgetToPut)).when(service).findById(1L);
        doReturn(widgetToUpdate).when(service).save(any());

        //Execute the PUT method
        mockMvc.perform(put("/rest/widget/{id}", 1L)
                .header(HttpHeaders.IF_MATCH, "1") //arreglar
                .content(asJsonString(widgetToPut))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))


                //Validate response and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))


                //Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))

                //validate the returned content
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Computer update")))
                .andExpect(jsonPath("$.description", is("Gamer laptop")))
                .andExpect(jsonPath("$.version", is(1)));
    }

    //PUT para modificar un elemento que no se encuentra en la base de datos (not found)
    @Test
    @DisplayName("PUT /rest/widget/1 - not found")
    void testPutWidgetNotFound() throws Exception {
        //setup mock
        Widget widgetToPut = new Widget(1L, "Computer", "laptop", 1);
        doReturn(Optional.empty()).when(service).findById(1L);

        //Execute put method
        mockMvc.perform(put("/rest/widget/{id}", 1L)
                .header(HttpHeaders.IF_MATCH, "1")
                .content(asJsonString(widgetToPut))
                .contentType(MediaType.APPLICATION_JSON))

                //Validate the method
                .andExpect(status().isNotFound());


    }

    //Test gets the widget by id
    @Test
    @DisplayName("GET /rest/widget/1")
    void testGetWidgetById() throws Exception{
        // setup mock
        Widget widgetToReturn = new Widget(1L, "Computer", "Gamer laptop", 1);
        doReturn(Optional.of(widgetToReturn)).when(service).findById(1L);

        //Execute the GET request
        mockMvc.perform(get("/rest/widget/{id}", 1L))
                .andDo(print())

                //Validate response
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                //validate content
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Computer")))
                .andExpect(jsonPath("$.description", is("Gamer laptop")))
                .andExpect(jsonPath("$.version", is(1)));

    }


    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}