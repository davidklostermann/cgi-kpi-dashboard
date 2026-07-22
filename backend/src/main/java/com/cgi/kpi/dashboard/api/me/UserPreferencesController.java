package com.cgi.kpi.dashboard.api.me;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.kpi.dashboard.api.me.dto.UpdateUserPreferencesRequestDto;
import com.cgi.kpi.dashboard.api.me.dto.UserPreferencesResponseDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/me/preferences")
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    public UserPreferencesController(UserPreferencesService userPreferencesService) {
        this.userPreferencesService = userPreferencesService;
    }

    @GetMapping
    public UserPreferencesResponseDto getPreferences() {
        return userPreferencesService.getPreferences();
    }

    @PutMapping
    public UserPreferencesResponseDto putPreferences(@Valid @RequestBody UpdateUserPreferencesRequestDto request) {
        return userPreferencesService.savePreferences(request);
    }
}
