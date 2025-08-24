package com.knockbook.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EcoController {

    private final JdbcTemplate jdbc;

    public EcoController(JdbcTemplate jdbc) {
        this.jdbc= jdbc;
    }

    @GetMapping("/db-ping")
    public String dbPing() {
        final var one = jdbc.queryForObject("SELECT 1", Integer.class);
        return (one != null && one == 1) ? "db:ok" : "db:fail";
    }
}
