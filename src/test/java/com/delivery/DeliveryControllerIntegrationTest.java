package com.delivery;

import com.delivery.model.Delivery;
import com.delivery.model.DeliveryStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Delivery Controller Integration Tests")
class DeliveryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TRACKING = "INTG-001";

    @Test
    @Order(1)
    @DisplayName("Health check returns UP")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/deliveries/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @Order(2)
    @DisplayName("Create delivery returns 201")
    void testCreateDelivery() throws Exception {
        Delivery d = new Delivery(TRACKING, "Integration Test User", "42 Test Lane");

        mockMvc.perform(post("/api/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(d)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.trackingNumber").value(TRACKING))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @Order(3)
    @DisplayName("Get delivery by tracking number")
    void testGetDelivery() throws Exception {
        mockMvc.perform(get("/api/deliveries/" + TRACKING))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.trackingNumber").value(TRACKING));
    }

    @Test
    @Order(4)
    @DisplayName("Valid status update PENDING → PICKED_UP")
    void testValidStatusUpdate() throws Exception {
        Map<String, Object> body = Map.of("status", "PICKED_UP", "remarks", "Picked up at warehouse");

        mockMvc.perform(put("/api/deliveries/" + TRACKING + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PICKED_UP"));
    }

    @Test
    @Order(5)
    @DisplayName("Invalid transition returns 400")
    void testInvalidStatusTransition() throws Exception {
        // Currently PICKED_UP, trying to go back to PENDING (invalid)
        Map<String, Object> body = Map.of("status", "PENDING");

        mockMvc.perform(put("/api/deliveries/" + TRACKING + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("Invalid status transition")));
    }

    @Test
    @Order(6)
    @DisplayName("Get all deliveries returns list")
    void testGetAllDeliveries() throws Exception {
        mockMvc.perform(get("/api/deliveries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(7)
    @DisplayName("Get by status returns matching deliveries")
    void testGetByStatus() throws Exception {
        mockMvc.perform(get("/api/deliveries/status/PICKED_UP"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("PICKED_UP"));
    }

    @Test
    @Order(8)
    @DisplayName("404 for unknown tracking number")
    void testNotFound() throws Exception {
        mockMvc.perform(get("/api/deliveries/NONEXISTENT"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(9)
    @DisplayName("Cannot delete an active delivery")
    void testCannotDeleteActiveDelivery() throws Exception {
        mockMvc.perform(delete("/api/deliveries/" + TRACKING))
            .andExpect(status().isBadRequest());
    }
}
