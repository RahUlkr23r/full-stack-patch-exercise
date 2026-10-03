package com.internal.tasktracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        if (page < 1) {
            return ResponseEntity.badRequest().body(Map.of("error", "Parameter 'page' must be at least 1"));
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Parameter 'pageSize' must be between 1 and 100"));
        }

        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + query.toLowerCase(Locale.ROOT) + "%";

        String normalizedStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            String statusValue = status.trim();
            try {
                normalizedStatus = TaskStatus.valueOf(statusValue.toUpperCase(Locale.ROOT)).name();
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + statusValue));
            }
        }

        log.info("[TaskController] q={} status={} page={} pageSize={}", query, normalizedStatus, page, pageSize);

        List<Task> allResults = taskRepository.searchTasks(searchTerm, normalizedStatus);

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allResults.size());
        List<Task> pageResults = (start < allResults.size())
                ? allResults.subList(start, end)
                : Collections.emptyList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", pageResults);
        response.put("total", allResults.size());
        response.put("page", page);
        response.put("pageSize", pageSize);

        return ResponseEntity.ok(response);
    }
}
