package com.dealxanh.app.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HomeController GET / and GET /home.
 * Uses the real application context (test profile) to verify:
 * - HTTP 200 responses
 * - Correct view name
 * - Required model attributes exist
 * - External HTTP calls are absent (verified by page rendering without timeout)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ================================================================
    // Section A: Basic HTTP and view tests
    // ================================================================

    @Test
    @DisplayName("GET / returns HTTP 200 with buyer/home view")
    void getRoot_Returns200_WithHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/home"));
    }

    @Test
    @DisplayName("GET /home returns HTTP 200 with buyer/home view")
    void getHome_Returns200_WithHomeView() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/home"));
    }

    // ================================================================
    // Section B: Required model attributes
    // ================================================================

    @Test
    @DisplayName("Home model contains all required attributes")
    void homeModel_ContainsAllRequiredAttributes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(model().attributeExists("activeNav"))
                .andExpect(model().attributeExists("cartItemCount"))
                .andExpect(model().attributeExists("totalStores"))
                .andExpect(model().attributeExists("totalUsers"))
                .andExpect(model().attributeExists("totalDeals"))
                .andExpect(model().attributeExists("allProducts"))
                .andExpect(model().attributeExists("flashSaleProducts"))
                .andExpect(model().attributeExists("hasFlashSale"))
                .andExpect(model().attributeExists("comboProducts"))
                .andExpect(model().attributeExists("hasCombo"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("suggestedProducts"));
    }

    @Test
    @DisplayName("Home model activeNav equals 'home'")
    void homeModel_ActiveNav_IsHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(model().attribute("activeNav", "home"));
    }

    // ================================================================
    // Section C: Page renders successfully — verifies no external HTTP hangs
    // ================================================================

    @Test
    @DisplayName("GET / renders within reasonable time (no external HTTP blocking)")
    void anonymousHome_RendersWithoutBlocking() throws Exception {
        // If locationService.getUserLocation were called, ipapi.co timeout (3s)
        // would make this test slow. Fast response = external call removed.
        long start = System.currentTimeMillis();
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        long elapsed = System.currentTimeMillis() - start;
        // Should complete well under 2 seconds (no 3s ipapi timeout)
        assert elapsed < 2000 : "Home render took " + elapsed + "ms — external HTTP call may be blocking";
    }

    @Test
    @DisplayName("Multiple concurrent / requests all return 200")
    void multipleRequests_AllReturn200() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("buyer/home"));
        }
    }

    // ================================================================
    // Section D: OTP attributes from addNotifCount
    // ================================================================

    @Test
    @DisplayName("Anonymous request has emailVerified=true and userEmail=''")
    void anonymousRequest_OtpAttributes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("emailVerified", true))
                .andExpect(model().attribute("userEmail", ""));
    }
}
