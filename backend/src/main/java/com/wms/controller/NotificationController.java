package com.wms.controller;

import com.wms.observer.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * NotificationController — exposes Observer event log to the frontend.
 * Thin controller (GRASP: Controller, SOLID: SRP) — just delegates to NotificationService.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** GET /notifications — returns the last 50 system notifications (newest first). */
    @GetMapping
    public ResponseEntity<List<String>> getNotifications() {
        return ResponseEntity.ok(notificationService.getRecentNotifications());
    }
}
