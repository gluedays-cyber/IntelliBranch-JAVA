package com.intellibranch.routing;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe, bounded ring buffer for asynchronous telemetry feedback and drift monitoring.
 */
public class TelemetryRingBuffer {
    private final int capacity;
    private final TelemetryEvent[] events;
    private int head;
    private int tail;
    private int count;

    public TelemetryRingBuffer(int capacity) {
        this.capacity = capacity > 0 ? capacity : 1024;
        this.events = new TelemetryEvent[this.capacity];
    }

    public synchronized void push(TelemetryEvent event) {
        events[head] = event;
        head = (head + 1) % capacity;
        if (count < capacity) {
            count++;
        } else {
            tail = (tail + 1) % capacity;
        }
    }

    public synchronized List<TelemetryEvent> drain() {
        if (count == 0) {
            return List.of();
        }
        List<TelemetryEvent> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int idx = (tail + i) % capacity;
            result.add(events[idx]);
            events[idx] = null;
        }
        head = 0;
        tail = 0;
        count = 0;
        return result;
    }

    public synchronized int count() {
        return count;
    }

    public int capacity() {
        return capacity;
    }
}
