package com.phobo.management.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void anonymousCanReadMenu() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
                
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void customerCannotCreateCategory() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryName\":\"New Category\", \"displayOrder\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void staffCannotEditProduct() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productName\":\"New Name\", \"categoryId\":\"123\", \"basePrice\": 50000, \"preparationTimeMinutes\": 15}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    public void managerCannotEditProduct() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productName\":\"New Name\", \"categoryId\":\"123\", \"basePrice\": 50000, \"preparationTimeMinutes\": 15}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void adminCanCreateCategory() throws Exception {
        // Expect 400 Bad Request (duplicate category) instead of 403 Forbidden
        mockMvc.perform(post("/api/v1/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryName\":\"Phở nước\", \"displayOrder\": 1}"))
                .andExpect(status().isBadRequest());
    }
}
