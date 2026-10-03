package com.internal.tasktracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTasksReturnsVisibleTasks() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(47))
                .andExpect(jsonPath("$.items.length()").value(10));
    }

    @Test
    void searchAndStatusFilterWorkTogether() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .param("q", "api")
                .param("status", "OPEN")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(6))
                .andExpect(jsonPath("$.items[0].title").value("Migrate to structured logging"));
    }

    @Test
    void invalidPaginationParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .param("page", "0")
                .param("pageSize", "10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/tasks")
                .param("page", "1")
                .param("pageSize", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidStatusIsRejected() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .param("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void archivedTasksAreExcludedEvenIfSearchMatchesDescription() throws Exception {
        // Task 59 ('Decommission legacy search endpoint') has archived=TRUE, status='DONE', and description contains 'endpoint'.
        // With correct SQL precedence, it must not appear in search results.
        mockMvc.perform(get("/api/tasks")
                .param("q", "endpoint")
                .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Add health check endpoint"));
    }

    @Test
    void searchWithTitleMatchRespectsStatusFilter() throws Exception {
        // Tasks with 'rate' in title (e.g., 'Update API rate limiting' - IN_PROGRESS)
        // must not leak when filtering by status=DONE.
        mockMvc.perform(get("/api/tasks")
                .param("q", "rate")
                .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void pageSizeExceedingMaxIsRejected() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .param("pageSize", "101"))
                .andExpect(status().isBadRequest());
    }
}
