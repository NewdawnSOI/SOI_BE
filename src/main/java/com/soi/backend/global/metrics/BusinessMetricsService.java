package com.soi.backend.global.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessMetricsService {

    private final MeterRegistry meterRegistry;

    public void increment(String event) {
        increment(event, new String[0]);
    }

    public void increment(String event, String... tags) {
        Counter.builder("soi.business.event")
                .tag("event", event)
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }
}
