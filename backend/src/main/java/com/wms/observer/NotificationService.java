package com.wms.observer;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * NotificationService — the Subject in the Observer pattern (Behavioral, OOAD requirement h).
 *
 * Design principles demonstrated:
 *  - SOLID Open/Closed: new observers are added without changing this class.
 *  - SOLID Dependency Inversion: depends on WmsObserver interface, not concrete classes.
 *  - GRASP Low Coupling: observers are decoupled from the business services that trigger events.
 *  - GRASP High Cohesion: this class only manages observer registration and event dispatch.
 *
 * Spring auto-discovers all @Component beans implementing WmsObserver and injects them
 * as a List<WmsObserver> — zero manual registration needed.
 */
@Service
public class NotificationService {

    private static final int MAX_LOG_SIZE = 50;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** All registered observers (injected by Spring — DIP satisfied). */
    private final List<WmsObserver> observers;

    /** In-memory log for the frontend to poll via GET /notifications. */
    private final LinkedList<String> notificationLog = new LinkedList<>();

    public NotificationService(List<WmsObserver> observers) {
        this.observers = observers;
    }

    /**
     * Fire an event to all registered observers.
     *
     * @param event  the domain event
     * @param detail human-readable description
     */
    public void notify(WmsEvent event, String detail) {
        String entry = "[" + LocalDateTime.now().format(FORMATTER) + "] "
                + event.name() + ": " + detail;

        // Persist to in-memory log (thread-safe append)
        synchronized (notificationLog) {
            notificationLog.addFirst(entry);
            if (notificationLog.size() > MAX_LOG_SIZE) {
                notificationLog.removeLast();
            }
        }

        // Dispatch to all observers
        for (WmsObserver observer : observers) {
            observer.onEvent(event, detail);
        }
    }

    /** Returns up to the last 50 notification entries (newest first). */
    public List<String> getRecentNotifications() {
        synchronized (notificationLog) {
            return Collections.unmodifiableList(new ArrayList<>(notificationLog));
        }
    }
}
