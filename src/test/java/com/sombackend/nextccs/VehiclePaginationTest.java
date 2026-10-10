package com.sombackend.nextccs;

import com.sombackend.nextccs.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;

import com.sombackend.nextccs.entity.Vehicle;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;



@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VehiclePaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void firstPageReturnsCorrectMetadata() throws Exception {
        mockMvc.perform(get("/vehicles")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void secondPageReturnsCorrectMetadata() throws Exception {
        mockMvc.perform(get("/vehicles")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void sizeOverLimitReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/vehicles")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void negativePageReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/vehicles")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void zeroSizeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/vehicles")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void paginationReturnsCorrectDataAndOrder() throws Exception {

        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 1; i <= 25; i++) {
            Vehicle vehicle = new Vehicle(
                    "TEST-PAGE-" + i,
                    "Test Model"
            );

            vehicles.add(vehicle);
        }

        List<Vehicle> savedVehicles = vehicleRepository.saveAllAndFlush(vehicles);

        long firstId = savedVehicles.get(0).getId();
        long twentiethId = savedVehicles.get(19).getId();
        long twentyFirstId = savedVehicles.get(20).getId();
        long lastId = savedVehicles.get(24).getId();

        mockMvc.perform(get("/vehicles")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].id").value(firstId))
                .andExpect(jsonPath("$.content[19].id").value(twentiethId));

        mockMvc.perform(get("/vehicles")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].id").value(twentyFirstId))
                .andExpect(jsonPath("$.content[4].id").value(lastId));
    }
}